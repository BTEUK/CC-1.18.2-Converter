package me.bteuk.converterplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

        //Get world.
        String worldName = instance.getConfig().getString("world");

        if (worldName == null) {
            sender.sendMessage("Set the world in config.");
            return true;
        }

        World world = Bukkit.getWorld(instance.getConfig().getString("world"));

        if (world == null) {
            sender.sendMessage("The world " + worldName + " does not exist.");
            return true;
        }

        //Get datafolder.
        Path folder = Path.of(instance.getDataFolder().getAbsolutePath()).resolve("post-processing");
        String[] files = new File(folder.toString()).list();

        JSONParser parser = new JSONParser();

        for (String file : files) {

            try (Reader reader = new FileReader(folder + "/" + file)) {

                //Get the array of json objects.
                JSONArray jsonArray = (JSONArray) parser.parse(reader);

                //Iterate through array.
                for (Object object : jsonArray) {

                    JSONObject jObject = (JSONObject) object;

                    //Get the location of the block.
                    Location l = new Location(world, (int) jObject.get("x"), (int) jObject.get("y"), (int) jObject.get("z"));

                    //Get the details of the block.


                }


            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

        }




        return false;
    }

    //Check if block has properties.
    private boolean hasProperties(String block) {

        switch (block) {

            case

        }

    }
}
