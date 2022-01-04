package com.slimelaunch.commands;

import com.slimelaunch.Launchpad;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class CommandSetConfig implements CommandExecutor {

  /**
   * Allow an operator to change any config data with a command, so that the
   * whole plugin doesn't need to be reloaded to tweak numbers.
   * 
   * Example usage:
   * /setconfig block-multipliers.COPPER_BLOCK 0.1
   * 
   * @param sender A Player, ConsoleCommandSender, or BlockCommandSender
   */
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.isOp()) {
      sender.sendMessage("Insufficient permissions.");
      return false;
    }

    if (args.length != 2) {
      sender.sendMessage("Invalid arguments: " + args.toString());
      return false;
    }

    FileConfiguration config = Launchpad.instance.getConfig();
    try {
      double doubleVal = Double.parseDouble(args[1]);
      config.set(args[0], doubleVal);
      sender.sendMessage("Set config value \"" + args[0] + "\" to " + doubleVal);
    } catch (NumberFormatException error) {
      config.set(args[0], args[1]);
      sender.sendMessage("Set config value \"" + args[0] + "\" to " + args[1]);
    }

    // Return true if the player used this command correctly
    return true;
  }
}
