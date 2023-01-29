package me.bteuk.converterplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

public class ChunkLoadEvent implements Listener {

    private final Plugin instance;

    private Path folder;
    private Converter converter;

    private final boolean enabled;

    private JSONParser parser;

    public ChunkLoadEvent(Plugin instance) {

        this.instance = instance;

        //Register listener.
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        //Get world.
        String worldName = instance.getConfig().getString("world");

        if (worldName == null) {
            instance.getLogger().warning("Set the world in config.");
            enabled = false;
            return;
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            instance.getLogger().warning("The world " + worldName + " does not exist.");
            enabled = false;
        } else {
            enabled = true;
        }

        //Get datafolder.
        folder = Path.of(instance.getDataFolder().getAbsolutePath()).resolve("post-processing");

        //Get json parser.
        parser = new JSONParser();

        //Create converter.
        converter = new Converter(instance, world);

        instance.getLogger().info("Converter activated.");

    }

    @EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent e) {

        //Check if enabled.
        if (!enabled) {
            return;
        }

        //Check if there is a json file in the post-processing folder.
        //Get region file.
        String regionFile = ((e.getChunk().getX() * 16) / 512) + "." + ((e.getChunk().getZ() * 16) / 512) + ".json";
        File file = new File(folder + "/" + regionFile);

        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {

                instance.getLogger().info("Starting conversion of " + file);

                //Get the array of json objects.
                JSONArray jsonArray = (JSONArray) parser.parse(reader);

                //Delete file once it's loaded. So other chunks don't reuse it before the converter is done.
                file.delete();

                converter.convert(jsonArray);

                instance.getLogger().info("Converter file " + file);

            } catch(IOException | ParseException ex) {
                ex.printStackTrace();
            }
        }
    }
}
