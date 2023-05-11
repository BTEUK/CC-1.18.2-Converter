package me.bteuk.converterplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.LinkedHashSet;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {

        saveDefaultConfig();

        //Get world.
        String worldName = getConfig().getString("world");

        //If worldName is null stop plugin from enabling.
        if (worldName == null) {
            this.getLogger().severe("World name has not been set in the config, disabling the plugin!");
            return;
        }

        World world = Bukkit.getWorld(worldName);

        Converter converter = new Converter(this, world);
        LinkedHashSet<File> converterQueue = new LinkedHashSet<>();

        //Get datafolder.
        Path folder = Path.of(getDataFolder().getAbsolutePath()).resolve("post-processing");

        //Get json parser.
        JSONParser parser = new JSONParser();
        getLogger().info("Converter activated.");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

            //Add the region where the player is in to the queue if not yet added.
            for (Player p : Bukkit.getOnlinePlayers()) {
                String regionFile = (((int) p.getLocation().getX() / 512) < 0 ? ((int) p.getLocation().getX() / 512) - 1 : ((int) p.getLocation().getX() / 512)) + "." +
                        (((int) p.getLocation().getZ() / 512) < 0 ? ((int) p.getLocation().getZ() / 512) - 1 : ((int) p.getLocation().getZ() / 512)) + ".json";
                File file = new File(folder + "/" + regionFile);

                if (file.exists()) {
                    converterQueue.add(file);
                }
            }

            if (!converter.isRunning() && !converterQueue.isEmpty()) {

                //Set converter to running, this prevent multiple conversion running at once.
                converter.setRunning(true);

                //Get the first region in the set.
                Object[] files = converterQueue.toArray();
                File newFile = (File) files[0];

                boolean success = false;

                try (Reader reader = new FileReader(newFile)) {

                    getLogger().info("Starting conversion of " + newFile);

                    //Get the array of json objects.
                    JSONArray jsonArray = (JSONArray) parser.parse(reader);

                    success = converter.convert(jsonArray);

                    getLogger().info("Converted file " + newFile);

                } catch(IOException | ParseException ex) {
                    ex.printStackTrace();
                }

                //If the converter has converted the region successfully, remove the file from the queue and set isRunning to false to unlock the converter.
                //Additionally delete the file, so it can't be converted multiple times.
                if (success) {
                    if (newFile.delete()) {
                        this.getLogger().info("Deleted " + newFile.getName());
                    } else {
                        this.getLogger().info("Failed to delete " + newFile.getName());
                    }
                } else {

                    this.getLogger().severe(newFile.getName() + " was not converted successfully, check the logs above this to see potential causes.");

                }

                converterQueue.remove(newFile);
                converter.setRunning(false);

            }
        }, 0L, 80L);

    }
}
