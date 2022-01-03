package com.slimelaunch;

import org.bukkit.Material;
import org.bukkit.block.Block;
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
    // getLogger().info("on player move");

    Player player = event.getPlayer();
    Block block = player.getLocation().getBlock();
    if (checkLaunchpadStructure(block)){
    // getLogger().info("launchpad structure found");
    player.setVelocity(new Vector(0, 10, 0));
    }
  }

  private boolean checkLaunchpadStructure(Block block){
    //TODO: check for complete structure
    if (block.getType().equals(Material.SLIME_BLOCK)){
      return true;
    }
    return false;
  }
}
