package me.bteuk.converterplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.getCommand("convert").setExecutor(new Converter(this));
        this.getCommand("placeblock").setExecutor(new BlockUpdateTest(this));

    }
}
