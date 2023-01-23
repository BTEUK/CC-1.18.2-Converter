package me.bteuk.converterplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.nio.file.Path;

public class Converter implements CommandExecutor {

    private Plugin instance;

    public Converter(Plugin instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        //Check if the sender is the console.
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can only be sent from the console.");
        }

        int max = Integer.MAX_VALUE;

        //If args length is 0 just start converting.
        if (args.length > 0) {
            try {
                max = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("The parameter must be an integer!");
            }
        }

        //Get datafolder.
        Path folder = Path.of(instance.getDataFolder().getAbsolutePath()).resolve("post-processing");
        folder.


        return false;
    }
}
