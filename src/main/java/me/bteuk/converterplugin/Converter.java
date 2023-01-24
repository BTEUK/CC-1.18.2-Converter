package me.bteuk.converterplugin;

import me.bteuk.converterplugin.utils.blocks.stairs.StairData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
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
    private World world;

    private StairData stair;
    private StairData[] stairs;

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

        world = Bukkit.getWorld(instance.getConfig().getString("world"));

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
                    Location l = new Location(world, (int)(long)jObject.get("x"), (int)(long)jObject.get("y"), (int)(long)jObject.get("z"));

                    //Set the block to its correct state.
                    setBlockData(jObject, l);

                }

                //Delete file when done.
                File fFile = new File(folder + "/" + file);
                fFile.delete();

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }


        }

        return true;
    }

    //Set the blockData of the block.
    private void setBlockData(JSONObject object, Location l) {

        switch ((String) object.get("block")) {

            case "minecraft:sunflower" -> {

                //Check the block below:
                Location lYMin = new Location(world, l.getX(), l.getY() - 1, l.getZ());
                BlockData bYMin = world.getBlockAt(lYMin).getBlockData();

                if (bYMin.getMaterial() == Material.SUNFLOWER) {
                    setTopFlower(Material.SUNFLOWER, l);
                } else if (bYMin.getMaterial() == Material.LILAC) {
                    setTopFlower(Material.LILAC, l);
                } else if (bYMin.getMaterial() == Material.ROSE_BUSH) {
                    setTopFlower(Material.ROSE_BUSH, l);
                } else if (bYMin.getMaterial() == Material.PEONY) {
                    setTopFlower(Material.PEONY, l);
                } else if (bYMin.getMaterial() == Material.TALL_GRASS) {
                    setTopFlower(Material.TALL_GRASS, l);
                } else if (bYMin.getMaterial() == Material.LARGE_FERN) {
                    setTopFlower(Material.LARGE_FERN, l);
                }
            }

            case "minecraft:oak_stairs", "minecraft:cobblestone_stairs", "minecraft:brick_stairs", "minecraft:stone_brick_stairs", "minecraft:nether_brick_stairs",
                    "minecraft:sandstone_stairs", "minecraft:spruce_stairs", "minecraft:birch_stairs", "minecraft:jungle_stairs", "minecraft:quartz_stairs", "minecraft:acacia_stairs",
                    "minecraft:dark_oak_stairs", "minecraft:red_sandstone_stairs", "minecraft:purpur_stairs", "minecraft:iron_bars" -> {

                /*

                Stair direction = direction 0
                Stair facing away on left = direction 1
                Stair facing away behind = direction 2
                Stair facing away on right = direction 3

                Order = Below-Left-Above-Right

                Formations:
                    Straight has priority
                    Stair always prioritises itself
                    Small corner over large corner

                Each of the 4 directions has:
                    Straight (Block is stair and attaches with a straight connection)
                    CornerI (Block is stair and attaches with an inner corner)
                    CornerO (Block is stair and attaches with an outer corner)
                    None (Block is stair without connections or a non-stair)

                Calculation:

                    1. If contains Straight
                        - Check for 2nd Straight ->
                            return Straight
                        - Check for CornerI ->
                            if CornerI[facing] != Straight[direction] ->
                                return CornerI in Straight[direction]
                        - Check for CornerO ->
                            if CornerO[facing] == Straight[direction] ->
                                return CornerO in !Straight[direction]
                        - Else return Straight

                    2. If contains CornerI return CornerI in !CornerI[direction]
                    3. If contains CornerO return CornerO in CornerO[direction]

                 */

                //Get main stair.
                /*
                BlockData bd = world.getBlockData(l);
                if (!(bd instanceof Stairs)) {
                    instance.getLogger().info(bd.getMaterial().name());
                    return;
                }
                Stairs stair = (Stairs) bd;
                StairData[] stairs = new StairData[4];
                StairData mainStair = new StairData(stair, l);

                //Get 4 adjacent stairs, if they are stairs.
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (world.getBlockData(lXMin) instanceof Stairs) {
                    Stairs xMin = (Stairs) world.getBlockData(lXMin);
                    if (mainStair.half == xMin.getHalf()) {
                        //Add it to the array at index 0.
                        stairs[0] = new StairData(xMin, lXMin, mainStair);
                    }
                }
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (world.getBlockData(lXMax) instanceof Stairs) {
                    Stairs xMax = (Stairs) world.getBlockData(lXMax);
                    if (mainStair.half == xMax.getHalf()) {
                        //Add it to the array at index 1.
                        stairs[1] = new StairData(xMax, lXMax, mainStair);
                    }
                }
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (world.getBlockData(lZMin) instanceof Stairs) {
                    Stairs zMin = (Stairs) world.getBlockData(lZMin);
                    if (mainStair.half == zMin.getHalf()) {
                        //Add it to the array at index 2.
                        stairs[2] = new StairData(zMin, lZMin, mainStair);
                    }
                }
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (world.getBlockData(lZMax) instanceof Stairs) {
                    Stairs zMax = (Stairs) world.getBlockData(lXMax);
                    if (mainStair.half == zMax.getHalf()) {
                        //Add it to the array at index 3.
                        stairs[3] = new StairData(zMax, lZMax, mainStair);
                    }
                }

                //Set the stair shape.
                stair.setShape(mainStair.getShape(stairs));

                //Update the block.
                world.setBlockData(l, stair);

                 */

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:oak_fence", "minecraft:birch_fence", "minecraft:spruce_fence", "minecraft:jungle_fence", "minecraft:acacia_fence", "minecraft:dark_oak_fence" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:cobblestone_wall", "minecraft:mossy_cobblestone_wall" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:glass_pane", "minecraft:red_stained_glass_pane", "minecraft:lime_stained_glass_pane", "minecraft:pink_stained_glass_pane", "minecraft:gray_stained_glass_pane",
                    "minecraft:cyan_stained_glass_pane", "minecraft:blue_stained_glass_pane", "minecraft:white_stained_glass_pane", "minecraft:brown_stained_glass_pane",
                    "minecraft:green_stained_glass_pane", "minecraft:black_stained_glass_pane", "minecraft:orange_stained_glass_pane", "minecraft:yellow_stained_glass_pane",
                    "minecraft:purple_stained_glass_pane", "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane", "minecraft:light_gray_stained_glass_pane" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:chest" -> {

                //Set connection.

            }

            case "minecraft:redstone_wire" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:chorus_plant" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:red_bed" -> {

                //Set colour.

            }

            case "minecraft:white_banner" -> {

                //Set colour and pattern.

            }

            case "minecraft:white_wall_banner" -> {

                //Set colour and pattern.

            }

            case "minecraft:melon_stem", "minecraft:pumpkin_stem" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:flower_pot" -> {

                //Set flower pot type.

            }

            case "minecraft:skeleton_skull" -> {

                //Set skull types and if playerhead set texture.

            }

            case "minecraft:note_block" -> {

                //Set note of note block.

            }

            case "minecraft:repeater" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:tripwire" -> {

                Block b = world.getBlockAt(l);
                b.getState().update(true, false);

            }

            case "minecraft:vine" -> {

                //Check if block above, if true, make it a vine.

            }


        }

    }

    private void setTopFlower(Material mat, Location l) {
        BlockData newBD = mat.createBlockData();
        world.setBlockData(l, newBD);
        Bisected bisected = (Bisected) newBD;
        bisected.setHalf(Bisected.Half.TOP);
        world.setBlockData(l, bisected);
    }
}
