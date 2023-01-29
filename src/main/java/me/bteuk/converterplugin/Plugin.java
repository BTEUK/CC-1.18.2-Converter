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

                Object[] files = converterQueue.toArray();
                File newFile = (File) files[0];
                converterQueue.remove(newFile);

                try (Reader reader = new FileReader(newFile)) {

                    getLogger().info("Starting conversion of " + newFile);

                    //Get the array of json objects.
                    JSONArray jsonArray = (JSONArray) parser.parse(reader);

                    //Delete file once it's loaded. So other chunks don't reuse it before the converter is done.
                    newFile.delete();

                    converter.convert(jsonArray);

                    getLogger().info("Converted file " + newFile);

                } catch(IOException | ParseException ex) {
                    ex.printStackTrace();
                }
            }
        }, 0L, 80L);

    }
}
