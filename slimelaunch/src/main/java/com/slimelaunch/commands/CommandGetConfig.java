package com.slimelaunch.commands;

import com.slimelaunch.Launchpad;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

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
    if (!sender.isOp()) {
      sender.sendMessage("Insufficient permissions.");
      return false;
    }

    FileConfiguration config = Launchpad.instance.getConfig();
    var configMap = config.getValues(true);
    configMap.forEach((key, val) -> sender.sendMessage("\"" + key + "\": " + val));

    // Return true if the sender used this command correctly
    return true;
  }
}
