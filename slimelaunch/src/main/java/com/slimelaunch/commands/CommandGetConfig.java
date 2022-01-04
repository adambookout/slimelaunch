package com.slimelaunch.commands;

import com.slimelaunch.Launchpad;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandGetConfig implements CommandExecutor {

  /**
   * Get all kvps in the config file.
   * 
   * Example usage:
   * /getconfig
   * 
   * @param sender A Player, ConsoleCommandSender, or BlockCommandSender
   */
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("getconfig is only usable by players.");
      return false;
    }

    Player player = (Player) sender;
    if (!player.isOp()) {
      player.sendMessage("Insufficient permissions.");
      return false;
    }

    FileConfiguration config = Launchpad.instance.getConfig();
    var configMap = config.getValues(true);
    configMap.forEach((key, val) -> player.sendMessage("\"" + key + "\": " + val));

    // Return true if the player used this command correctly
    return true;
  }
}
