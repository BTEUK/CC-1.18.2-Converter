package me.bteuk.converterplugin;

import org.bukkit.World;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WorldAnalyser implements CommandExecutor {

    Plugin plugin;

    Path folder;
    Path world_folder;

    public WorldAnalyser(Plugin plugin, World world, Path converter_folder) {

        this.plugin = plugin;

        PluginCommand command = plugin.getCommand("worldanalyser");

        if (command == null) {
            plugin.getLogger().warning("WorldAnalyser not added to plugin.yml");
            return;
        }

        //Get the location of the world folder and post-processing.
        world_folder = Path.of((world.getWorldFolder().getAbsolutePath()) + "/region");
        folder = converter_folder;

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

        //Try to get the region min and region max as integers.
        String[] regionMin = args[1].split(",");
        String[] regionMax = args[2].split(",");

        if (regionMin.length != 2 || regionMax.length != 2) {

            sender.sendMessage("/wa search [regionMin] [regionMax]");
            sender.sendMessage("Regions must be in x,z format.");
            return;

        }

        int rMinX;
        int rMinZ;
        int rMaxX;
        int rMaxZ;

        try {

            rMinX = Integer.parseInt(regionMin[0]);
            rMinZ = Integer.parseInt(regionMin[1]);
            rMaxX = Integer.parseInt(regionMax[0]);
            rMaxZ = Integer.parseInt(regionMax[1]);


        } catch (NumberFormatException e) {

            sender.sendMessage("/wa search [regionMin] [regionMax]");
            sender.sendMessage("Regions must be in x,z format.");
            return;

        }

        //Iterate through the regions and make a summary.
        HashMap<String, Double> regions = new HashMap<>();

        for (int i = rMinX; i <= rMaxX; i++) {
            for (int j = rMinZ; j <= rMaxZ; j++) {

                File file = new File(world_folder + "/" + "r." + i + "." + j + ".mca");

                if (file.exists()) {

                    regions.put(i + "," + j, ((file.length() / 1024) / 1024.0));

                }
            }
        }

        //Print the list in chat.
        if (regions.isEmpty()) {

            sender.sendMessage("No regions exist in this area!");

        } else {
            for (Map.Entry<String, Double> entry : regions.entrySet()) {

                sender.sendMessage("Region: " + entry.getKey() + " - Size: " + String.format("%.2f", entry.getValue()) + "MB");

            }
        }


    }

    private void delete(CommandSender sender, String[] args) {

        if (args.length != 3) {

            sender.sendMessage("/wa delete [regionMin] [regionMax]");
            return;

        }
    }
}
