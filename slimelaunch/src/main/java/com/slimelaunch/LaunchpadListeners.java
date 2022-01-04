package com.slimelaunch;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class LaunchpadListeners implements Listener {

  @EventHandler
  public void onBlockPlaced(BlockPlaceEvent event) {

  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    // Check if player stepped on the launchpad, and launch them if they did
    Player player = event.getPlayer();

    // Don't launch player if sneaking
    if (player.isSneaking() /* || player.isInsideVehicle() */)
      return;

    Block blockAtFeet = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
    Launchpad plugin = Launchpad.instance;
    if (!plugin.isLaunchpad(blockAtFeet))
      return;

    Vector launchVector = plugin.getLaunchVector(blockAtFeet);
    if (launchVector != null)
      player.setVelocity(launchVector);
  }
}
