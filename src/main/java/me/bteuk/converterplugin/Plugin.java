package me.bteuk.converterplugin;

import me.bteuk.converterplugin.utils.exceptions.FolderEmptyException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

public class Plugin extends JavaPlugin {

    ArrayList<Integer> tasks;

    @Override
    public void onEnable() {

        //Get datafolder.
        Path folder = Path.of(getDataFolder().getAbsolutePath()).resolve("post-processing");

        //If no files exist in the folder, disable the plugin as it is no longer necessary.
        if (isFolderEmpty(folder)) {
            getLogger().info("The post-processing folder is empty, the plugin is no longer necessary, disabling the plugin!");
            return;
        }

        saveDefaultConfig();

        //Get world.
        String worldName = getConfig().getString("world");

        //If worldName is null stop plugin from enabling.
        if (worldName == null) {
            getLogger().severe("World name has not been set in the config, disabling the plugin!");
            return;
        }

        World world = Bukkit.getWorld(worldName);

        Converter converter = new Converter(this, world);
        LinkedHashSet<File> converterQueue = new LinkedHashSet<>();

        //Get json parser.
        JSONParser parser = new JSONParser();
        getLogger().info("Converter activated.");

        //List of repeating tasks, they are stored, so they can be cancelled if not needed.
        tasks = new ArrayList<>();

        //If automated_conversion is enabled in config then add a region to the queue on an interval.
        //The speed is also defined in config by HIGH = 1m, NORMAL = 2.5m or LOW = 5m.
        boolean automated_conversion = getConfig().getBoolean("automated_conversion.enabled");

        if (automated_conversion) {
            String interval_speed = getConfig().getString("automated_conversion.speed");
            long interval = defaultInterval();
            if (interval_speed != null) {
                interval = intervalToTicks(interval_speed);
            }

            //Create task.
            tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

                //Check if the server is empty.
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    //Add a region to the queue.
                    if (!isFolderEmpty(folder)) {
                        try {
                            File file = getRandomRegion(folder);
                            if (file.exists()) {
                                getLogger().info("Added file " + file.getName() + " to the queue.");
                                converterQueue.add(file);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }, 0, interval));
        }

        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

            //If there are no files in the post-processing folder, disable the tasks.
            if (isFolderEmpty(folder)) {

                getLogger().info("The post-processing folder has been cleared, disabling converter!");

                for (int task : tasks) {
                    Bukkit.getScheduler().cancelTask(task);
                }

                tasks.clear();
                return;
            }

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
                        getLogger().info("Deleted " + newFile.getName());
                    } else {
                        getLogger().info("Failed to delete " + newFile.getName());
                    }
                } else {

                    getLogger().severe(newFile.getName() + " was not converted successfully, check the logs above this to see potential causes.");

                }

                converterQueue.remove(newFile);
                converter.setRunning(false);

            }
        }, 0L, 80L));

    }

    private boolean isFolderEmpty(Path path) {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        } else {
            return true;
        }
    }

    private File getRandomRegion(Path path) throws IOException, FolderEmptyException {
        Stream<Path> entries = Files.list(path);
        if (entries.findFirst().isPresent()) {
            return entries.findFirst().get().toFile();
        } else {
            throw new FolderEmptyException("The directory " + path.getFileName() + " is empty.");
        }
    }

    private long defaultInterval() {
        //Default is 2.5 minutes = 2.5 * 60 * 20 = 3000 ticks.
        return 3000L;
    }

    private long intervalToTicks(String interval) {

        //If the string is not in the correct format, use the default.
        switch (interval) {

            case "HIGH" -> {
                //1 minute = 60 * 20 = 1200 ticks.
                return 1200L;
            }

            case "NORMAL" -> {
                //2.5 minutes = 2.5 * 60 * 20 = 3000 ticks.
                return 3000L;
            }

            case "LOW" -> {
                //5 minutes = 5 * 60 * 20 = 6000 ticks.
                return 6000L;
            }

            default -> {
                //Default is 2.5 minutes = 2.5 * 60 * 20 = 3000 ticks.
                getLogger().info("No valid automated conversion speed has been specified, defaulting to NORMAL which is 2.5 minutes.");
                return defaultInterval();
            }

        }
    }
}
