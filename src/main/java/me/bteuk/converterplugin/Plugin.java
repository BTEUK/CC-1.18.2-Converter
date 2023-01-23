package me.bteuk.converterplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getCommand("convert").setExecutor(new Converter(this));

    }
}
