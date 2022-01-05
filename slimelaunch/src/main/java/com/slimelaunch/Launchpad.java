package com.slimelaunch;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.slimelaunch.commands.CommandGetConfig;
import com.slimelaunch.commands.CommandSetConfig;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Launchpad extends JavaPlugin {
    public static Launchpad instance;

    // Keep track of when each player was last launched, for the launch cooldown
    private HashMap<UUID, Long> playersLastLaunched = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Hello, SpigotMC!");
        Launchpad.instance = this;

        // Copy the default plugin config to make it usable (if it does not
        // already exist).
        this.saveDefaultConfig();

        // Register events
        this.getServer().getPluginManager().registerEvents(new LaunchpadListeners(), this);

        // Register commands
        this.getCommand("setconfig").setExecutor(new CommandSetConfig());
        this.getCommand("getconfig").setExecutor(new CommandGetConfig());
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }

    /**
     * Get the multiplier for a specified block type from config.
     * 
     * @param material
     * @return The value from config if found, or -1 if not found.
     */
    public double getMaterialMultiplier(Material material) {
        return this.getConfig().getDouble("block-multipliers." + material.toString(), -1);
    }

    /**
     * @returns a value for how much influence the number of blocks in the
     *          launchpad structure should have on the final launch strength.
     */
    public double getBlockCountFactor() {
        return this.getConfig().getDouble("block-count-factor", 0.0);
    }

    /**
     * @returns the minimum time in seconds between launches for a player
     */
    public double getLaunchCooldown() {
        return this.getConfig().getDouble("launch-cooldown", 0.0);
    }

    /**
     * @returns the maximum launch vector magnitude
     */
    public double getMaxLaunchSpeed() {
        return this.getConfig().getDouble("max-launch-speed", 0.0);
    }

    /**
     * @returns All block names that can be used to modify a launchpad's
     *          velocity vector, from config.
     */
    public Set<String> getValidBlockTypes() {
        return this.getConfig().getConfigurationSection("block-multipliers").getKeys(false);
    }

    /**
     * Pass the block that the player is standing on to check if it is a
     * launchpad.
     *
     * @returns true if this is a launchpad, false if not
     */
    public boolean isLaunchpad(Block block) {
        // Immediately return false if we know this isn't a slime block so we
        // don't need to do expensive checks every onPlayerMove
        if (!block.getType().equals(Material.SLIME_BLOCK))
            return false;

        Set<String> possibleBlocks = this.getValidBlockTypes();
        String blockBelowSlimeType = block.getRelative(BlockFace.DOWN).getType().toString();
        return possibleBlocks.contains(blockBelowSlimeType);
    }

    /**
     * Check if the given player is able to launch, based on how long it's been
     * since their last launch.
     */
    public boolean checkCanLaunch(Player player) {
        Long playerLastLaunchTime = playersLastLaunched.get(player.getUniqueId());

        if (playerLastLaunchTime == null) {
            return true;
        }

        Long timeElapsed = System.currentTimeMillis() - playerLastLaunchTime;
        return TimeUnit.MILLISECONDS.toSeconds(timeElapsed) >= this.getLaunchCooldown();
    }

    /**
     * Assumes the given block is the slimeblock on top of a valid launchpad, as
     * checked by checkLaunchpadStructure.
     *
     * @returns The final vector to set the player to be launched, based on the
     *          launchpad's configuration (shape of blocks beneath slime
     *          blocks), or null if the block configuration was bad.
     */
    public Vector getLaunchVector(Block slimeBlock) {
        getLogger().info("Get launch vector");

        Material material = slimeBlock.getRelative(BlockFace.DOWN).getType();

        double checkMultiplier = this.getMaterialMultiplier(material);
        if (checkMultiplier == -1)
            return null; // Bad launchpad, don't apply force to player

        Block toBlock = slimeBlock.getRelative(BlockFace.DOWN);

        // Launch vector initialized with value of block immediately below slime
        // block, pointing straight up
        Vector launchVector = new Vector(0, checkMultiplier, 0);
        int blockCount = 1;

        // Look at the 5x3x5 cube of blocks below the slime block and compare
        // their position with the block below the slime block to get the
        // direction vector.
        for (int y = -2; y <= 0; y++) {
            for (int z = -2; z <= 2; z++) {
                for (int x = -2; x <= 2; x++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    Block fromBlock = toBlock.getRelative(x, y, z);
                    double multiplier = getMaterialMultiplier(fromBlock.getType());
                    if (multiplier == -1)
                        continue; // Skip invalid blocks

                    Vector directionVector = toBlock.getLocation().toVector()
                            .subtract(fromBlock.getLocation().toVector());

                    Vector addVector = directionVector.normalize().multiply(multiplier);
                    // Each block contributes its direction * its material
                    // multiplier, plus incrementing the block count
                    launchVector.add(addVector);
                    blockCount += 1;
                }
            }
        }

        double blockCountFactor = this.getBlockCountFactor();

        getLogger().info("> vector from materials & positions: " + launchVector);
        getLogger().info("> magnitude from block count factor (" + blockCount + "^" + blockCountFactor + "): "
                + Math.pow(blockCount, blockCountFactor));

        // Increase final launch vector by a factor relating to the number of
        // blocks in the structure
        launchVector.multiply(Math.pow(blockCount, blockCountFactor));
        double maxLaunchSpeed = this.getMaxLaunchSpeed();
        if (launchVector.length() > maxLaunchSpeed) {
            // Cap the launch vector magnitude
            launchVector = launchVector.clone().normalize().multiply(maxLaunchSpeed);
            getLogger().info("> capped launch vector: " + launchVector);
        }
        return launchVector;
    }

    public void launchPlayer(Player player, Vector launchVector) {
        player.sendMessage("Launching " + launchVector);
        Pig pig = player.getWorld().spawn(player.getLocation(), Pig.class);
        pig.setAware(false);
        pig.setLootTable(null);
        pig.setInvisible(true);
        pig.setSilent(true);
        pig.setFallDistance(100);
        // TODO: pig doesn't die if it hits the slime block
        // pig.setMetadata("isLaunchPig", new FixedMetadataValue(this, true));
        pig.addPassenger(player);
        pig.setVelocity(launchVector);

        // Set time that this player was launched (for launch cooldown)
        playersLastLaunched.put(player.getUniqueId(), System.currentTimeMillis());
    }
}