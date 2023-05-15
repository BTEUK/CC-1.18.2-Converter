package me.bteuk.converterplugin;

import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public class WorldAnalyser implements CommandExecutor {

    Plugin plugin;

    public WorldAnalyser(Plugin plugin) {

        this.plugin = plugin;

        PluginCommand command = plugin.getCommand("worldanalyser");

        if (command == null) {
            plugin.getLogger().warning("WorldAnalyser not added to plugin.yml");
            return;
        }

        command.setExecutor(this);

        plugin.getLogger().info("Enabled World Analyser");

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("This command can only be run through the console!");
            return true;
        }

        if (args.length == 0) {
            help(sender);
            return true;
        }

        //Check first arg.
        switch (args[0]) {

            case "search" -> {

                search(sender, args);

            }

            case "delete" -> {

                delete(sender, args);

            }
        }


        return true;

    }

    private void help(CommandSender sender) {

        sender.sendMessage("/wa search [regionMin] [regionMax]");
        sender.sendMessage("/wa delete [regionMin] [regionMax]");

    }

    private void search(CommandSender sender, String[] args) {

        if (args.length != 3) {

            sender.sendMessage("/wa search [regionMin] [regionMax]");
            return;

        }



    }

    private void delete(CommandSender sender, String[] args) {

        if (args.length != 3) {

            sender.sendMessage("/wa delete [regionMin] [regionMax]");
            return;

        }
    }
}
