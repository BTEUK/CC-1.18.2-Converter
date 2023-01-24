package me.bteuk.converterplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockUpdateTest implements CommandExecutor {

    Plugin instance;

    public BlockUpdateTest(Plugin instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        World world = Bukkit.getWorld(instance.getConfig().getString("world"));

        Location l = ((Player) sender).getLocation();

        world.setBlockData(l, Material.OAK_STAIRS.createBlockData());

        world.getBlockAt(l).getState().update(true, false);

        return true;

    }
}
