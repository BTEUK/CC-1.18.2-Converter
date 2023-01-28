package me.bteuk.converterplugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.bteuk.converterplugin.utils.blocks.stairs.StairData;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.*;
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
import java.util.UUID;

public class Converter implements CommandExecutor {

    private Plugin instance;
    private World world;

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
                    Location l = new Location(world, (int) (long) jObject.get("x"), (int) (long) jObject.get("y"), (int) (long) jObject.get("z"));

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
                BlockData bYMin = world.getBlockData(lYMin);

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
                    "minecraft:dark_oak_stairs", "minecraft:red_sandstone_stairs", "minecraft:purpur_stairs" -> {

                BlockData bd = world.getBlockData(l);
                if (!(bd instanceof Stairs)) {
                    instance.getLogger().info("Not a stair at " + l.getX() + ", " + l.getY() + ", " + l.getZ());
                    return;
                }

                Stairs stair = getStair(l);

                //Update the block.
                world.setBlockData(l, stair);

            }

            //Fences, Iron bar and Glass panes.
            case "minecraft:oak_fence", "minecraft:birch_fence", "minecraft:spruce_fence", "minecraft:jungle_fence", "minecraft:acacia_fence", "minecraft:dark_oak_fence",
                    "minecraft:nether_brick_fence", "minecraft:glass_pane", "minecraft:iron_bars" -> {

                //Check if the fence can connect to adjacent blocks.
                BlockData block = world.getBlockData(l);
                if (!(block instanceof Fence)) {
                    instance.getLogger().info("Not a fence at " + l.getX() + ", " + l.getY() + ", " + l.getZ());
                }
                Fence fence = (Fence) block;

                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (canConnect(block.getMaterial(), lZMin, BlockFace.NORTH)) {
                    fence.setFace(BlockFace.NORTH, true);
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (canConnect(block.getMaterial(), lXMax, BlockFace.EAST)) {
                    fence.setFace(BlockFace.EAST, true);
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (canConnect(block.getMaterial(), lZMax, BlockFace.SOUTH)) {
                    fence.setFace(BlockFace.SOUTH, true);
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (canConnect(block.getMaterial(), lXMin, BlockFace.WEST)) {
                    fence.setFace(BlockFace.WEST, true);
                }

                world.setBlockData(l, fence);
            }

            //Stained glass (for some reason it's a different data type from normal glass panes, even though the data is the exact same)
            case "minecraft:red_stained_glass_pane", "minecraft:lime_stained_glass_pane",
                    "minecraft:pink_stained_glass_pane", "minecraft:gray_stained_glass_pane", "minecraft:cyan_stained_glass_pane", "minecraft:blue_stained_glass_pane",
                    "minecraft:white_stained_glass_pane", "minecraft:brown_stained_glass_pane", "minecraft:green_stained_glass_pane", "minecraft:black_stained_glass_pane",
                    "minecraft:orange_stained_glass_pane", "minecraft:yellow_stained_glass_pane", "minecraft:purple_stained_glass_pane",
                    "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane", "minecraft:light_gray_stained_glass_pane" -> {

                //Check if the fence can connect to adjacent blocks.
                BlockData block = world.getBlockData(l);
                if (!(block instanceof GlassPane)) {
                    instance.getLogger().info("Not a glass pane at " + l.getX() + ", " + l.getY() + ", " + l.getZ());
                }
                GlassPane fence = (GlassPane) block;

                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (canConnect(block.getMaterial(), lZMin, BlockFace.NORTH)) {
                    fence.setFace(BlockFace.NORTH, true);
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (canConnect(block.getMaterial(), lXMax, BlockFace.EAST)) {
                    fence.setFace(BlockFace.EAST, true);
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (canConnect(block.getMaterial(), lZMax, BlockFace.SOUTH)) {
                    fence.setFace(BlockFace.SOUTH, true);
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (canConnect(block.getMaterial(), lXMin, BlockFace.WEST)) {
                    fence.setFace(BlockFace.WEST, true);
                }

                world.setBlockData(l, fence);

            }

            case "minecraft:cobblestone_wall", "minecraft:mossy_cobblestone_wall" -> {

                //Check if the fence can connect to adjacent blocks.
                BlockData block = world.getBlockData(l);
                if (!(block instanceof Wall)) {
                    instance.getLogger().info("Not a wall at " + l.getX() + ", " + l.getY() + ", " + l.getZ());
                    return;
                }

                world.setBlockData(l, getWall(l));

            }

            case "minecraft:chest", "minecraft:trapped_chest" -> {

                BlockData blockData = world.getBlockData(l);

                //Check adjacent blocks.
                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());

                //Check if types are the same and they face the same direction.
                BlockData bZMin = world.getBlockData(lZMin);
                BlockData bXMax = world.getBlockData(lXMax);
                BlockData bZMax = world.getBlockData(lZMax);
                BlockData bXMin = world.getBlockData(lXMin);
                Chest chest = (Chest) blockData;
                if (bZMin.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bZMin;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be West of East otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.WEST) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.EAST) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                } else if (bXMax.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bXMax;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be North or South otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.NORTH) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.SOUTH) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                } else if (bZMax.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bZMax;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be West of East otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.EAST) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.WEST) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                } else if (bXMin.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bXMin;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be North or South otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.SOUTH) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.NORTH) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                }

                world.setBlockData(l, chest);

            }

            case "minecraft:redstone_wire" -> {

                BlockData blockData = world.getBlockData(l);
                RedstoneWire redstoneWire = (RedstoneWire) blockData;

                //Check if adjacent blocks can connect to the redstone.
                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (redstoneConnects(world.getBlockAt(lZMin), BlockFace.NORTH)) {
                    redstoneWire.setFace(BlockFace.NORTH, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lZMin, BlockFace.NORTH)) {
                        redstoneWire.setFace(BlockFace.NORTH, RedstoneWire.Connection.UP);
                    }
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (redstoneConnects(world.getBlockAt(lXMax), BlockFace.EAST)) {
                    redstoneWire.setFace(BlockFace.EAST, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lXMax, BlockFace.EAST)) {
                        redstoneWire.setFace(BlockFace.EAST, RedstoneWire.Connection.UP);
                    }
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (redstoneConnects(world.getBlockAt(lZMax), BlockFace.SOUTH)) {
                    redstoneWire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lZMax, BlockFace.SOUTH)) {
                        redstoneWire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.UP);
                    }
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (redstoneConnects(world.getBlockAt(lXMin), BlockFace.WEST)) {
                    redstoneWire.setFace(BlockFace.WEST, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lXMin, BlockFace.WEST)) {
                        redstoneWire.setFace(BlockFace.WEST, RedstoneWire.Connection.UP);
                    }
                }

                //If only 1 face is set then the opposite side needs to be set to SIDE.
                int sum = 0;
                if (redstoneWire.getFace(BlockFace.NORTH) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (redstoneWire.getFace(BlockFace.EAST) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (redstoneWire.getFace(BlockFace.SOUTH) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (redstoneWire.getFace(BlockFace.WEST) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (sum == 1) {
                    if (redstoneWire.getFace(BlockFace.NORTH) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.SIDE);
                    }
                    if (redstoneWire.getFace(BlockFace.EAST) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.WEST, RedstoneWire.Connection.SIDE);
                    }
                    if (redstoneWire.getFace(BlockFace.SOUTH) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.NORTH, RedstoneWire.Connection.SIDE);
                    }
                    if (redstoneWire.getFace(BlockFace.WEST) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.EAST, RedstoneWire.Connection.SIDE);
                    }
                }

                world.setBlockData(l, redstoneWire);

            }

            case "minecraft:chorus_plant" -> {

                BlockData blockData = world.getBlockData(l);
                MultipleFacing facing = (MultipleFacing) blockData;

                //Check if all adjacent blocks are chorus plants.
                //If true they connect.
                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (world.getType(lZMin) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.NORTH, true);
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (world.getType(lXMax) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.EAST, true);
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (world.getType(lZMax) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.SOUTH, true);
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (world.getType(lXMin) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.WEST, true);
                }

                //Down
                Location lYMin = new Location(world, l.getX(), l.getY() - 1, l.getZ());
                if (world.getType(lYMin) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER || world.getType(lZMin) == Material.END_STONE) {
                    facing.setFace(BlockFace.DOWN, true);
                }

                //Up
                Location lYMax = new Location(world, l.getX(), l.getY() + 1, l.getZ());
                if (world.getType(lYMax) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.UP, true);
                }
            }

            case "minecraft:red_bed" -> {

                //Get properties.
                JSONObject properties = (JSONObject) object.get("properties");

                //Set colour.
                Block block = world.getBlockAt(l);

                switch ((String) properties.get("colour")) {
                    case "white" -> block.setType(Material.WHITE_BED);
                    case "orange" -> block.setType(Material.ORANGE_BED);
                    case "magenta" -> block.setType(Material.MAGENTA_BED);
                    case "light_blue" -> block.setType(Material.LIGHT_BLUE_BED);
                    case "yellow" -> block.setType(Material.YELLOW_BED);
                    case "lime" -> block.setType(Material.LIME_BED);
                    case "pink" -> block.setType(Material.PINK_BED);
                    case "gray" -> block.setType(Material.GRAY_BED);
                    case "light_gray" -> block.setType(Material.LIGHT_GRAY_BED);
                    case "cyan" -> block.setType(Material.CYAN_BED);
                    case "purple" -> block.setType(Material.PURPLE_BED);
                    case "blue" -> block.setType(Material.BLUE_BED);
                    case "brown" -> block.setType(Material.BROWN_BED);
                    case "green" -> block.setType(Material.GREEN_BED);
                    case "red" -> block.setType(Material.RED_BED);
                    case "black" -> block.setType(Material.BLACK_BED);
                }

                //Set part and facing.
                Bed bed = (Bed) block.getBlockData();

                if (properties.get("part").equals("head")) {
                    bed.setPart(Bed.Part.HEAD);
                } else {
                    bed.setPart(Bed.Part.FOOT);
                }

                switch ((String) properties.get("facing")) {
                    case "north" -> bed.setFacing(BlockFace.NORTH);
                    case "east" -> bed.setFacing(BlockFace.EAST);
                    case "south" -> bed.setFacing(BlockFace.SOUTH);
                    case "west" -> bed.setFacing(BlockFace.WEST);
                }

                block.setBlockData(bed);

            }

            case "minecraft:white_banner" -> {

                //Get properties.
                JSONObject properties = (JSONObject) object.get("properties");

                //Set banner base colour.
                Block block = world.getBlockAt(l);

                switch ((String) properties.get("colour")) {
                    case "white" -> block.setType(Material.WHITE_BANNER);
                    case "orange" -> block.setType(Material.ORANGE_BANNER);
                    case "magenta" -> block.setType(Material.MAGENTA_BANNER);
                    case "light_blue" -> block.setType(Material.LIGHT_BLUE_BANNER);
                    case "yellow" -> block.setType(Material.YELLOW_BANNER);
                    case "lime" -> block.setType(Material.LIME_BANNER);
                    case "pink" -> block.setType(Material.PINK_BANNER);
                    case "gray" -> block.setType(Material.GRAY_BANNER);
                    case "light_gray" -> block.setType(Material.LIGHT_GRAY_BANNER);
                    case "cyan" -> block.setType(Material.CYAN_BANNER);
                    case "purple" -> block.setType(Material.PURPLE_BANNER);
                    case "blue" -> block.setType(Material.BLUE_BANNER);
                    case "brown" -> block.setType(Material.BROWN_BANNER);
                    case "green" -> block.setType(Material.GREEN_BANNER);
                    case "red" -> block.setType(Material.RED_BANNER);
                    case "black" -> block.setType(Material.BLACK_BANNER);
                }

                //Set rotation.
                Rotatable rot = (Rotatable) block.getBlockData();
                rot = setRotation(rot, Byte.parseByte((String) properties.get("rotation")));
                block.setBlockData(rot);

                //Set patterns
                setBannerPatterns(block, (JSONArray) properties.get("patterns"));

            }

            case "minecraft:white_wall_banner" -> {

                //Get properties.
                JSONObject properties = (JSONObject) object.get("properties");

                //Set banner base colour.
                Block block = world.getBlockAt(l);

                switch ((String) properties.get("colour")) {
                    case "white" -> block.setType(Material.WHITE_WALL_BANNER);
                    case "orange" -> block.setType(Material.ORANGE_WALL_BANNER);
                    case "magenta" -> block.setType(Material.MAGENTA_WALL_BANNER);
                    case "light_blue" -> block.setType(Material.LIGHT_BLUE_WALL_BANNER);
                    case "yellow" -> block.setType(Material.YELLOW_WALL_BANNER);
                    case "lime" -> block.setType(Material.LIME_WALL_BANNER);
                    case "pink" -> block.setType(Material.PINK_WALL_BANNER);
                    case "gray" -> block.setType(Material.GRAY_WALL_BANNER);
                    case "light_gray" -> block.setType(Material.LIGHT_GRAY_WALL_BANNER);
                    case "cyan" -> block.setType(Material.CYAN_WALL_BANNER);
                    case "purple" -> block.setType(Material.PURPLE_WALL_BANNER);
                    case "blue" -> block.setType(Material.BLUE_WALL_BANNER);
                    case "brown" -> block.setType(Material.BROWN_WALL_BANNER);
                    case "green" -> block.setType(Material.GREEN_WALL_BANNER);
                    case "red" -> block.setType(Material.RED_WALL_BANNER);
                    case "black" -> block.setType(Material.BLACK_WALL_BANNER);
                }

                //Set facing direction.
                Directional direction = (Directional) block.getBlockData();

                switch ((String) properties.get("facing")) {
                    case "north" -> direction.setFacing(BlockFace.NORTH);
                    case "east" -> direction.setFacing(BlockFace.EAST);
                    case "south" -> direction.setFacing(BlockFace.SOUTH);
                    case "west" -> direction.setFacing(BlockFace.WEST);
                }

                block.setBlockData(direction);

                //Set patterns
                setBannerPatterns(block, (JSONArray) properties.get("patterns"));

            }

            case "minecraft:flower_pot" -> {

                //Set flower pot type.
                Block block = world.getBlockAt(l);

                //Set flower pot type.
                JSONObject properties = (JSONObject) object.get("properties");
                switch ((String) properties.get("type")) {

                    case "potted_dandelion" -> block.setType(Material.POTTED_DANDELION);
                    case "potted_poppy" -> block.setType(Material.POTTED_POPPY);
                    case "potted_blue_orchid" -> block.setType(Material.POTTED_BLUE_ORCHID);
                    case "potted_allium" -> block.setType(Material.POTTED_ALLIUM);
                    case "potted_azure_bluet" -> block.setType(Material.POTTED_AZURE_BLUET);
                    case "potted_red_tulip" -> block.setType(Material.POTTED_RED_TULIP);
                    case "potted_orange_tulip" -> block.setType(Material.POTTED_ORANGE_TULIP);
                    case "potted_white_tulip" -> block.setType(Material.POTTED_WHITE_TULIP);
                    case "potted_pink_tulip" -> block.setType(Material.POTTED_PINK_TULIP);
                    case "potted_oxeye_daisy" -> block.setType(Material.POTTED_OXEYE_DAISY);
                    case "potted_oak_sapling" -> block.setType(Material.POTTED_OAK_SAPLING);
                    case "potted_spruce_sapling" -> block.setType(Material.POTTED_SPRUCE_SAPLING);
                    case "potted_birch_sapling" -> block.setType(Material.POTTED_BIRCH_SAPLING);
                    case "potted_jungle_sapling" -> block.setType(Material.POTTED_JUNGLE_SAPLING);
                    case "potted_acacia_sapling" -> block.setType(Material.POTTED_ACACIA_SAPLING);
                    case "potted_dark_oak_sapling" -> block.setType(Material.POTTED_DARK_OAK_SAPLING);
                    case "potted_red_mushroom" -> block.setType(Material.POTTED_RED_MUSHROOM);
                    case "potted_brown_mushroom" -> block.setType(Material.POTTED_BROWN_MUSHROOM);
                    case "potted_fern" -> block.setType(Material.POTTED_FERN);
                    case "potted_dead_bush" -> block.setType(Material.POTTED_DEAD_BUSH);
                    case "potted_cactus" -> block.setType(Material.POTTED_CACTUS);
                    default -> block.setType(Material.FLOWER_POT);

                }
            }

            case "minecraft:skeleton_skull" -> {

                JSONObject properties = (JSONObject) object.get("properties");

                //Get the current skull at the location.
                Block block = world.getBlockAt(l);
                BlockData bd = block.getBlockData();

                //Set skull types and if playerhead set texture.
                String type = (String) properties.get("type");
                String facing = (String) properties.get("facing");

                if (facing.equals("floor")) {

                    //Skull
                    //Set material.
                    switch (type) {

                        case "skeleton_skull" -> block.setType(Material.SKELETON_SKULL);
                        case "wither_skeleton_skull" -> block.setType(Material.WITHER_SKELETON_SKULL);
                        case "zombie_head" -> block.setType(Material.ZOMBIE_HEAD);
                        case "player_head" -> block.setType(Material.PLAYER_HEAD);
                        case "creeper_head" -> block.setType(Material.CREEPER_HEAD);
                        case "dragon_head" -> block.setType(Material.DRAGON_HEAD);

                    }

                    Rotatable rot = (Rotatable) block.getBlockData();
                    rot = setRotation(rot, (byte) (long) properties.get("rotation"));

                    block.setBlockData(rot);

                } else if (facing.equals("north") || facing.equals("west") || facing.equals("south") || facing.equals("east")) {

                    //Wall Skull
                    //Set material.
                    switch (type) {

                        case "skeleton_skull" -> block.setType(Material.SKELETON_WALL_SKULL);
                        case "wither_skeleton_skull" -> block.setType(Material.WITHER_SKELETON_WALL_SKULL);
                        case "zombie_head" -> block.setType(Material.ZOMBIE_WALL_HEAD);
                        case "player_head" -> block.setType(Material.PLAYER_WALL_HEAD);
                        case "creeper_head" -> block.setType(Material.CREEPER_WALL_HEAD);
                        case "dragon_head" -> block.setType(Material.DRAGON_WALL_HEAD);

                    }

                    Directional dir = (Directional) block.getBlockData();

                    switch ((String) properties.get("facing")) {

                        case "north" -> dir.setFacing(BlockFace.NORTH);
                        case "south" -> dir.setFacing(BlockFace.SOUTH);
                        case "west" -> dir.setFacing(BlockFace.WEST);
                        case "east" -> dir.setFacing(BlockFace.EAST);

                    }

                    block.setBlockData(dir);


                } else {
                    instance.getLogger().info("Not a skull at " + l.getX() + ", " + l.getY() + ", " + l.getZ());
                    return;
                }

                //If type is a player head, set the texture, ect.
                if (type.equals("player_head")) {

                    Skull skull = (Skull) block.getState();
                    skull.setType(block.getType());

                    PlayerProfile profile = Bukkit.createProfile(UUID.fromString((String) properties.get("id")));
                    profile.getProperties().add(new ProfileProperty("textures", (String) properties.get("texture")));

                    skull.setPlayerProfile(profile);

                    skull.update(); // so that the result can be seen

                }
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

    //Check if the material is of a fence.
    private boolean isFence(Material mat) {
        return (mat == Material.OAK_FENCE || mat == Material.SPRUCE_FENCE || mat == Material.BIRCH_FENCE || mat == Material.JUNGLE_FENCE || mat == Material.ACACIA_FENCE || mat == Material.DARK_OAK_FENCE || mat == Material.NETHER_BRICK_FENCE);
    }

    //Check if the material is of a fence or wall type.
    private boolean isFenceOrWall(BlockData bd) {
        return (bd instanceof Fence || bd instanceof GlassPane || bd instanceof Wall);
    }

    //Check if the material is of a fence gate.
    private boolean isFenceGate(Material mat) {
        return (mat == Material.OAK_FENCE_GATE || mat == Material.SPRUCE_FENCE_GATE || mat == Material.BIRCH_FENCE_GATE || mat == Material.JUNGLE_FENCE_GATE || mat == Material.ACACIA_FENCE_GATE || mat == Material.DARK_OAK_FENCE_GATE);
    }

    //Set the connections for walls with low or high.
    private Wall setConnections(Location l, BlockData block, Wall wall, Wall.Height height) {
        //North (Negative Z)
        Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
        if (canConnect(block.getMaterial(), lZMin, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.NORTH, height);
        }

        //East (Positive X)
        Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
        if (canConnect(block.getMaterial(), lXMax, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.EAST, height);
        }

        //South (Positive Z)
        Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
        if (canConnect(block.getMaterial(), lZMax, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.SOUTH, height);
        }

        //West (Negative X)
        Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
        if (canConnect(block.getMaterial(), lXMin, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.WEST, height);
        }

        return wall;
    }

    private boolean canConnect(Material mat, Location l, BlockFace face) {

        BlockData bd = world.getBlockData(l);

        //First check if the block is solid, return if true.
        switch (bd.getMaterial()) {
            case STONE, GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ANDESITE,
                    GRASS_BLOCK, DIRT, COARSE_DIRT, PODZOL, COBBLESTONE,
                    OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS,
                    BEDROCK, SAND, RED_SAND, GRAVEL,
                    COAL_ORE, IRON_ORE, GOLD_ORE, REDSTONE_ORE, EMERALD_ORE, LAPIS_ORE, DIAMOND_ORE, NETHER_QUARTZ_ORE,
                    COAL_BLOCK, IRON_BLOCK, GOLD_BLOCK, DIAMOND_BLOCK,
                    OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                    OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD,
                    SPONGE, WET_SPONGE, GLASS, LAPIS_BLOCK, SANDSTONE, CHISELED_SANDSTONE, CUT_SANDSTONE,
                    WHITE_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIGHT_BLUE_WOOL, YELLOW_WOOL, LIME_WOOL, PINK_WOOL, GRAY_WOOL,
                    LIGHT_GRAY_WOOL, CYAN_WOOL, PURPLE_WOOL, BLUE_WOOL, BROWN_WOOL, GREEN_WOOL, RED_WOOL, BLACK_WOOL,
                    SMOOTH_QUARTZ, SMOOTH_RED_SANDSTONE, SMOOTH_SANDSTONE, SMOOTH_STONE, BRICKS, BOOKSHELF,
                    MOSSY_COBBLESTONE, OBSIDIAN, PURPUR_BLOCK, PURPUR_PILLAR, CRAFTING_TABLE, FURNACE,
                    ICE, SNOW_BLOCK, CLAY, JUKEBOX, NETHERRACK, SOUL_SAND, GLOWSTONE,
                    INFESTED_STONE, INFESTED_COBBLESTONE, INFESTED_STONE_BRICKS, INFESTED_MOSSY_STONE_BRICKS, INFESTED_CRACKED_STONE_BRICKS, INFESTED_CHISELED_STONE_BRICKS,
                    STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS, CHISELED_STONE_BRICKS,
                    BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK, MUSHROOM_STEM, MYCELIUM, NETHER_BRICK, END_STONE, END_STONE_BRICKS,
                    EMERALD_BLOCK, BEACON, CHISELED_QUARTZ_BLOCK, QUARTZ_BLOCK, QUARTZ_PILLAR,
                    WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                    YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA,
                    LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA,
                    BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA,
                    HAY_BLOCK, TERRACOTTA, PACKED_ICE, PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE, SEA_LANTERN,
                    RED_SANDSTONE, CHISELED_RED_SANDSTONE, CUT_RED_SANDSTONE, MAGMA_BLOCK, RED_NETHER_BRICKS, BONE_BLOCK,
                    WHITE_GLAZED_TERRACOTTA, ORANGE_GLAZED_TERRACOTTA, MAGENTA_GLAZED_TERRACOTTA, LIGHT_BLUE_GLAZED_TERRACOTTA,
                    YELLOW_GLAZED_TERRACOTTA, LIME_GLAZED_TERRACOTTA, PINK_GLAZED_TERRACOTTA, GRAY_GLAZED_TERRACOTTA,
                    LIGHT_GRAY_GLAZED_TERRACOTTA, CYAN_GLAZED_TERRACOTTA, PURPLE_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA,
                    BROWN_GLAZED_TERRACOTTA, GREEN_GLAZED_TERRACOTTA, RED_GLAZED_TERRACOTTA, BLACK_GLAZED_TERRACOTTA,
                    WHITE_CONCRETE, ORANGE_CONCRETE, MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE, YELLOW_CONCRETE, LIME_CONCRETE, PINK_CONCRETE, GRAY_CONCRETE,
                    LIGHT_GRAY_CONCRETE, CYAN_CONCRETE, PURPLE_CONCRETE, BLUE_CONCRETE, BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE,
                    WHITE_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER,
                    YELLOW_CONCRETE_POWDER, LIME_CONCRETE_POWDER, PINK_CONCRETE_POWDER, GRAY_CONCRETE_POWDER,
                    LIGHT_GRAY_CONCRETE_POWDER, CYAN_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, BLUE_CONCRETE_POWDER,
                    BROWN_CONCRETE_POWDER, GREEN_CONCRETE_POWDER, RED_CONCRETE_POWDER, BLACK_CONCRETE_POWDER,
                    REDSTONE_BLOCK, SLIME_BLOCK, PISTON, STICKY_PISTON, OBSERVER, DISPENSER, DROPPER,
                    TNT, REDSTONE_LAMP, NOTE_BLOCK -> {
                return true;
            }
        }

        //Stair case
        if (bd instanceof Stairs) {

            Stairs stair = getStair(l);

            switch (face) {
                case NORTH -> {
                    if (stair.getFacing() == face) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.WEST && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.EAST && stair.getShape() == Stairs.Shape.INNER_RIGHT) {
                        return true;
                    } else {
                        return false;
                    }
                }
                case WEST -> {
                    if (stair.getFacing() == face) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.SOUTH && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.NORTH && stair.getShape() == Stairs.Shape.INNER_RIGHT) {
                        return true;
                    } else {
                        return false;
                    }
                }
                case SOUTH -> {
                    if (stair.getFacing() == face) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.EAST && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.WEST && stair.getShape() == Stairs.Shape.INNER_RIGHT) {
                        return true;
                    } else {
                        return false;
                    }
                }
                case EAST -> {
                    if (stair.getFacing() == face) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.NORTH && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.SOUTH && stair.getShape() == Stairs.Shape.INNER_RIGHT) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }

        //Slab Case
        if (bd instanceof Slab) {
            Slab slab = (Slab) bd;
            return (slab.getType() == Slab.Type.DOUBLE);
        }

        //Fences
        if (bd.getMaterial() == Material.NETHER_BRICK_FENCE) {
            return (bd.getMaterial() == mat);
        } else if (bd.getMaterial() == Material.OAK_FENCE || bd.getMaterial() == Material.SPRUCE_FENCE || bd.getMaterial() == Material.BIRCH_FENCE || bd.getMaterial() == Material.JUNGLE_FENCE || bd.getMaterial() == Material.ACACIA_FENCE || bd.getMaterial() == Material.DARK_OAK_FENCE) {
            if (mat == Material.OAK_FENCE || mat == Material.SPRUCE_FENCE || mat == Material.BIRCH_FENCE || mat == Material.JUNGLE_FENCE || mat == Material.ACACIA_FENCE || mat == Material.DARK_OAK_FENCE) {
                return true;
            } else {
                return false;
            }
        } else if (bd.getMaterial() == Material.GLASS_PANE || bd.getMaterial() == Material.IRON_BARS || bd.getMaterial() == Material.COBBLESTONE_WALL || bd.getMaterial() == Material.MOSSY_COBBLESTONE_WALL || (bd instanceof GlassPane)) {
            if (mat == Material.GLASS_PANE || mat == Material.IRON_BARS || mat == Material.COBBLESTONE_WALL || mat == Material.MOSSY_COBBLESTONE_WALL ||
                    mat == Material.WHITE_STAINED_GLASS_PANE || mat == Material.ORANGE_STAINED_GLASS_PANE || mat == Material.MAGENTA_STAINED_GLASS_PANE ||
                    mat == Material.LIGHT_BLUE_STAINED_GLASS_PANE || mat == Material.YELLOW_STAINED_GLASS_PANE || mat == Material.LIME_STAINED_GLASS_PANE ||
                    mat == Material.PINK_STAINED_GLASS_PANE || mat == Material.GRAY_STAINED_GLASS_PANE || mat == Material.LIGHT_GRAY_STAINED_GLASS_PANE ||
                    mat == Material.CYAN_STAINED_GLASS_PANE || mat == Material.PURPLE_STAINED_GLASS_PANE || mat == Material.BLUE_STAINED_GLASS_PANE ||
                    mat == Material.BROWN_STAINED_GLASS_PANE || mat == Material.GREEN_STAINED_GLASS_PANE || mat == Material.RED_STAINED_GLASS_PANE || mat == Material.BLACK_STAINED_GLASS_PANE) {
                return true;
            } else {
                return false;
            }
        }

        //Snow
        if (bd.getMaterial() == Material.SNOW) {
            Snow snow = (Snow) bd;
            return (snow.getLayers() == 8 && face != BlockFace.UP);
        }

        //Trapdoors
        if (bd instanceof TrapDoor) {
            TrapDoor trapDoor = (TrapDoor) bd;
            return (trapDoor.getFacing() == face && trapDoor.isOpen());
        }

        //Fence gates
        if (bd instanceof Gate) {
            Gate gate = (Gate) bd;
            if (isFence(mat)) {
                if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    return (gate.getFacing() == BlockFace.WEST || gate.getFacing() == BlockFace.EAST);
                } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                    return (gate.getFacing() == BlockFace.NORTH || gate.getFacing() == BlockFace.SOUTH);
                }
            }
            return false;
        }

        //Doors
        if (bd instanceof Door) {
            Door door = (Door) bd;
            if (door.getFacing() == face && !door.isOpen()) {
                return true;
            }

            if (door.isOpen()) {
                switch (face) {
                    case NORTH -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.WEST);
                        } else {
                            return (door.getFacing() == BlockFace.EAST);
                        }
                    }
                    case WEST -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.SOUTH);
                        } else {
                            return (door.getFacing() == BlockFace.NORTH);
                        }
                    }
                    case SOUTH -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.EAST);
                        } else {
                            return (door.getFacing() == BlockFace.WEST);
                        }
                    }
                    case EAST -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.NORTH);
                        } else {
                            return (door.getFacing() == BlockFace.SOUTH);
                        }
                    }
                }
            }
        }

        //All other cases.
        return false;
    }

    //Check if the wall can connect to this block above.
    private boolean canConnectAbove(BlockData bd) {

        //First check if the block is solid, return if true.
        switch (bd.getMaterial()) {
            case STONE, GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ANDESITE,
                    GRASS_BLOCK, DIRT, COARSE_DIRT, PODZOL, COBBLESTONE,
                    OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS,
                    BEDROCK, SAND, RED_SAND, GRAVEL,
                    COAL_ORE, IRON_ORE, GOLD_ORE, REDSTONE_ORE, EMERALD_ORE, LAPIS_ORE, DIAMOND_ORE, NETHER_QUARTZ_ORE,
                    COAL_BLOCK, IRON_BLOCK, GOLD_BLOCK, DIAMOND_BLOCK,
                    OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                    OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD,
                    SPONGE, WET_SPONGE, GLASS, LAPIS_BLOCK, SANDSTONE, CHISELED_SANDSTONE, CUT_SANDSTONE,
                    WHITE_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIGHT_BLUE_WOOL, YELLOW_WOOL, LIME_WOOL, PINK_WOOL, GRAY_WOOL,
                    LIGHT_GRAY_WOOL, CYAN_WOOL, PURPLE_WOOL, BLUE_WOOL, BROWN_WOOL, GREEN_WOOL, RED_WOOL, BLACK_WOOL,
                    SMOOTH_QUARTZ, SMOOTH_RED_SANDSTONE, SMOOTH_SANDSTONE, SMOOTH_STONE, BRICKS, BOOKSHELF,
                    MOSSY_COBBLESTONE, OBSIDIAN, PURPUR_BLOCK, PURPUR_PILLAR, CRAFTING_TABLE, FURNACE,
                    ICE, SNOW_BLOCK, CLAY, JUKEBOX, NETHERRACK, SOUL_SAND, GLOWSTONE,
                    INFESTED_STONE, INFESTED_COBBLESTONE, INFESTED_STONE_BRICKS, INFESTED_MOSSY_STONE_BRICKS, INFESTED_CRACKED_STONE_BRICKS, INFESTED_CHISELED_STONE_BRICKS,
                    STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS, CHISELED_STONE_BRICKS,
                    BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK, MUSHROOM_STEM, MYCELIUM, NETHER_BRICK, END_STONE, END_STONE_BRICKS,
                    EMERALD_BLOCK, BEACON, CHISELED_QUARTZ_BLOCK, QUARTZ_BLOCK, QUARTZ_PILLAR,
                    WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                    YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA,
                    LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA,
                    BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA,
                    HAY_BLOCK, TERRACOTTA, PACKED_ICE, PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE, SEA_LANTERN,
                    RED_SANDSTONE, CHISELED_RED_SANDSTONE, CUT_RED_SANDSTONE, MAGMA_BLOCK, RED_NETHER_BRICKS, BONE_BLOCK,
                    WHITE_GLAZED_TERRACOTTA, ORANGE_GLAZED_TERRACOTTA, MAGENTA_GLAZED_TERRACOTTA, LIGHT_BLUE_GLAZED_TERRACOTTA,
                    YELLOW_GLAZED_TERRACOTTA, LIME_GLAZED_TERRACOTTA, PINK_GLAZED_TERRACOTTA, GRAY_GLAZED_TERRACOTTA,
                    LIGHT_GRAY_GLAZED_TERRACOTTA, CYAN_GLAZED_TERRACOTTA, PURPLE_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA,
                    BROWN_GLAZED_TERRACOTTA, GREEN_GLAZED_TERRACOTTA, RED_GLAZED_TERRACOTTA, BLACK_GLAZED_TERRACOTTA,
                    WHITE_CONCRETE, ORANGE_CONCRETE, MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE, YELLOW_CONCRETE, LIME_CONCRETE, PINK_CONCRETE, GRAY_CONCRETE,
                    LIGHT_GRAY_CONCRETE, CYAN_CONCRETE, PURPLE_CONCRETE, BLUE_CONCRETE, BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE,
                    WHITE_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER,
                    YELLOW_CONCRETE_POWDER, LIME_CONCRETE_POWDER, PINK_CONCRETE_POWDER, GRAY_CONCRETE_POWDER,
                    LIGHT_GRAY_CONCRETE_POWDER, CYAN_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, BLUE_CONCRETE_POWDER,
                    BROWN_CONCRETE_POWDER, GREEN_CONCRETE_POWDER, RED_CONCRETE_POWDER, BLACK_CONCRETE_POWDER,
                    REDSTONE_BLOCK, SLIME_BLOCK, PISTON, STICKY_PISTON, OBSERVER, DISPENSER, DROPPER,
                    TNT, REDSTONE_LAMP, NOTE_BLOCK,

                    OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES,
                    FARMLAND, DIRT_PATH, CARVED_PUMPKIN, JACK_O_LANTERN, MELON, ENCHANTING_TABLE, END_PORTAL_FRAME,
                    WHITE_CARPET, ORANGE_CARPET, MAGENTA_CARPET, LIGHT_BLUE_CARPET, YELLOW_CARPET, LIME_CARPET,
                    PINK_CARPET, GRAY_CARPET, LIGHT_GRAY_CARPET, CYAN_CARPET, PURPLE_CARPET, BLUE_CARPET,
                    BROWN_CARPET, GREEN_CARPET, RED_CARPET, BLACK_CARPET,
                    SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX,
                    YELLOW_SHULKER_BOX, LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX,
                    CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX,
                    RED_SHULKER_BOX, BLACK_SHULKER_BOX, DAYLIGHT_DETECTOR -> {
                return true;
            }
        }

        //Stair case
        if (bd instanceof Stairs) {

            Stairs stair = (Stairs) bd;
            return (stair.getHalf() == Bisected.Half.BOTTOM);
        }

        //Slab Case
        if (bd instanceof Slab) {
            Slab slab = (Slab) bd;
            return (slab.getType() == Slab.Type.DOUBLE || slab.getType() == Slab.Type.BOTTOM);
        }

        //Trapdoors
        if (bd instanceof TrapDoor) {
            TrapDoor trapDoor = (TrapDoor) bd;
            return (trapDoor.getHalf() == Bisected.Half.BOTTOM && !trapDoor.isOpen());
        }

        //All other cases.
        return false;
    }

    private Stairs getStair(Location l) {
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
        BlockData bd = world.getBlockData(l);
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
            Stairs zMax = (Stairs) world.getBlockData(lZMax);
            if (mainStair.half == zMax.getHalf()) {
                //Add it to the array at index 3.
                stairs[3] = new StairData(zMax, lZMax, mainStair);
            }
        }

        //Set the stair shape.
        stair.setShape(mainStair.getShape(stairs));

        return stair;
    }

    private Wall getWall(Location l) {

        //Check if the fence can connect to adjacent blocks.
        BlockData block = world.getBlockData(l);
        Wall wall = (Wall) block;

        //First check which directions are connected for the wall.
        //Only then check which should be tall/low.
        wall = setConnections(l, block, wall, Wall.Height.LOW);

        //Get the blocks above
        //If the block above is a fence or wall more checks are needed.
        Location lAbove = new Location(world, l.getX(), (l.getY() + 1), l.getZ());
        BlockData bAbove = world.getBlockData(lAbove);

        if (isFenceOrWall(bAbove)) {

            //Check in which directions it is connected.
            //North (Negative Z)
            Location lZMin = new Location(world, lAbove.getX(), lAbove.getY(), lAbove.getZ() - 1);
            if (canConnect(block.getMaterial(), lZMin, BlockFace.NORTH)) {
                if (wall.getHeight(BlockFace.NORTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.NORTH, Wall.Height.TALL);
                }
            }

            //East (Positive X)
            Location lXMax = new Location(world, lAbove.getX() + 1, lAbove.getY(), lAbove.getZ());
            if (canConnect(block.getMaterial(), lXMax, BlockFace.EAST)) {
                if (wall.getHeight(BlockFace.EAST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.EAST, Wall.Height.TALL);
                }
            }

            //South (Positive Z)
            Location lZMax = new Location(world, lAbove.getX(), lAbove.getY(), lAbove.getZ() + 1);
            if (canConnect(block.getMaterial(), lZMax, BlockFace.SOUTH)) {
                if (wall.getHeight(BlockFace.SOUTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.SOUTH, Wall.Height.TALL);
                }
            }

            //West (Negative X)
            Location lXMin = new Location(world, lAbove.getX() - 1, lAbove.getY(), lAbove.getZ());
            if (canConnect(block.getMaterial(), lXMin, BlockFace.WEST)) {
                if (wall.getHeight(BlockFace.WEST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.WEST, Wall.Height.TALL);
                }
            }
        } else if (isFenceGate(bAbove.getMaterial())) {

            //Check the direction of the fence gate.
            Gate gate = (Gate) bAbove;

            if (gate.getFacing() == BlockFace.NORTH || gate.getFacing() == BlockFace.SOUTH) {
                if (wall.getHeight(BlockFace.EAST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.EAST, Wall.Height.TALL);
                }

                if (wall.getHeight(BlockFace.WEST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.WEST, Wall.Height.TALL);
                }
            } else {
                if (wall.getHeight(BlockFace.NORTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.NORTH, Wall.Height.TALL);
                }

                if (wall.getHeight(BlockFace.SOUTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.SOUTH, Wall.Height.TALL);
                }
            }
        } else {
            //Check of the wall can connect to the block above.
            if (canConnectAbove(bAbove)) {


                //Set all heights to tall.
                wall = setConnections(l, block, wall, Wall.Height.TALL);

            }
        }

        //Check if the walls up=true.
        //This is true if there are no connections, a single connection, a corner only connection.
        //Also a lot of other blocks will cause this
        //Set up to true by default.
        //Check for cases where this is not true.
        wall.setUp(true);

        //If wall has a straight or cross shape.
        if (
                ((wall.getHeight(BlockFace.NORTH) == wall.getHeight(BlockFace.SOUTH)) && wall.getHeight(BlockFace.NORTH) != Wall.Height.NONE &&
                        (wall.getHeight(BlockFace.WEST) == Wall.Height.NONE && wall.getHeight(BlockFace.EAST) == Wall.Height.NONE))
                        ||
                        (wall.getHeight(BlockFace.EAST) == wall.getHeight(BlockFace.WEST) && wall.getHeight(BlockFace.EAST) != Wall.Height.NONE &&
                                wall.getHeight(BlockFace.NORTH) == Wall.Height.NONE && wall.getHeight(BlockFace.SOUTH) == Wall.Height.NONE)
                        ||
                        ((wall.getHeight(BlockFace.NORTH) == wall.getHeight(BlockFace.SOUTH)) && (wall.getHeight(BlockFace.EAST) == wall.getHeight(BlockFace.WEST)) && wall.getHeight(BlockFace.NORTH) != Wall.Height.NONE)
        ) {
            wall.setUp(false);

            //Check for cases where this wouldn't be true.
            //Certain blocks will still set the wall to up.
            switch (bAbove.getMaterial()) {

                case TORCH, REDSTONE_TORCH, STONE_PRESSURE_PLATE, OAK_PRESSURE_PLATE,
                        LIGHT_WEIGHTED_PRESSURE_PLATE, HEAVY_WEIGHTED_PRESSURE_PLATE,
                        OAK_SIGN, BREWING_STAND, FLOWER_POT, POTTED_DANDELION, POTTED_POPPY,
                        POTTED_BLUE_ORCHID, POTTED_ALLIUM, POTTED_AZURE_BLUET, POTTED_RED_TULIP,
                        POTTED_ORANGE_TULIP, POTTED_WHITE_TULIP, POTTED_PINK_TULIP, POTTED_OXEYE_DAISY,
                        POTTED_OAK_SAPLING, POTTED_SPRUCE_SAPLING, POTTED_BIRCH_SAPLING, POTTED_JUNGLE_SAPLING,
                        POTTED_ACACIA_SAPLING, POTTED_DARK_OAK_SAPLING, POTTED_RED_MUSHROOM, POTTED_BROWN_MUSHROOM,
                        POTTED_FERN, POTTED_DEAD_BUSH, POTTED_CACTUS,
                        SKELETON_SKULL, WITHER_SKELETON_SKULL, PLAYER_HEAD, ZOMBIE_HEAD, CREEPER_HEAD, DRAGON_HEAD,
                        WHITE_BANNER, ORANGE_BANNER, MAGENTA_BANNER, LIGHT_BLUE_BANNER, YELLOW_BANNER, LIME_BANNER,
                        PINK_BANNER, GRAY_BANNER, LIGHT_GRAY_BANNER, CYAN_BANNER, PURPLE_BANNER, BLUE_BANNER,
                        BROWN_BANNER, GREEN_BANNER, RED_BANNER, BLACK_BANNER -> {

                    wall.setUp(true);
                }

                case END_ROD, HOPPER -> {
                    Directional direction = (Directional) bAbove;
                    if (direction.getFacing() == BlockFace.UP || direction.getFacing() == BlockFace.DOWN) {
                        wall.setUp(true);
                    }
                }
            }

            if (isFenceGate(bAbove.getMaterial())) {
                if (wall.getHeight(BlockFace.NORTH) != Wall.Height.TALL || wall.getHeight(BlockFace.SOUTH) != Wall.Height.TALL
                        || wall.getHeight(BlockFace.EAST) != Wall.Height.TALL || wall.getHeight(BlockFace.WEST) == Wall.Height.TALL) {
                    wall.setUp(true);
                }
            } else if (bAbove instanceof Wall) {

                //Check if the wall above is up or not, if it's up then this wall will also be up.
                if (getWall(lAbove).isUp()) {
                    wall.setUp(true);
                }
            }
        }

        return wall;
    }

    private PatternType getPatternType(String p) {

        switch (p) {

            case "bs" -> {
                return PatternType.STRIPE_BOTTOM;
            }

            case "ts" -> {
                return PatternType.STRIPE_TOP;
            }

            case "ls" -> {
                return PatternType.STRIPE_LEFT;
            }

            case "rs" -> {
                return PatternType.STRIPE_RIGHT;
            }

            case "cs" -> {
                return PatternType.STRIPE_CENTER;
            }

            case "ms" -> {
                return PatternType.STRIPE_MIDDLE;
            }

            case "drs" -> {
                return PatternType.STRIPE_DOWNRIGHT;
            }

            case "dls" -> {
                return PatternType.STRIPE_DOWNLEFT;
            }

            case "ss" -> {
                return PatternType.STRIPE_SMALL;
            }

            case "cr" -> {
                return PatternType.CROSS;
            }

            case "sc" -> {
                return PatternType.STRAIGHT_CROSS;
            }

            case "ld" -> {
                return PatternType.DIAGONAL_LEFT;
            }

            case "rud" -> {
                return PatternType.DIAGONAL_RIGHT_MIRROR;
            }

            case "lud" -> {
                return PatternType.DIAGONAL_LEFT_MIRROR;
            }

            case "rd" -> {
                return PatternType.DIAGONAL_RIGHT;
            }

            case "vh" -> {
                return PatternType.HALF_VERTICAL;
            }

            case "vhr" -> {
                return PatternType.HALF_VERTICAL_MIRROR;
            }

            case "hh" -> {
                return PatternType.HALF_HORIZONTAL;
            }

            case "hhb" -> {
                return PatternType.HALF_HORIZONTAL_MIRROR;
            }

            case "bl" -> {
                return PatternType.SQUARE_BOTTOM_LEFT;
            }

            case "br" -> {
                return PatternType.SQUARE_BOTTOM_RIGHT;
            }

            case "tl" -> {
                return PatternType.SQUARE_TOP_LEFT;
            }

            case "tr" -> {
                return PatternType.SQUARE_TOP_RIGHT;
            }

            case "bt" -> {
                return PatternType.TRIANGLE_BOTTOM;
            }

            case "tt" -> {
                return PatternType.TRIANGLE_TOP;
            }

            case "bts" -> {
                return PatternType.TRIANGLES_BOTTOM;
            }

            case "tts" -> {
                return PatternType.TRIANGLES_TOP;
            }

            case "mc" -> {
                return PatternType.CIRCLE_MIDDLE;
            }

            case "mr" -> {
                return PatternType.RHOMBUS_MIDDLE;
            }
            case "bo" -> {
                return PatternType.BORDER;
            }

            case "cbo" -> {
                return PatternType.CURLY_BORDER;
            }

            case "bri" -> {
                return PatternType.BRICKS;
            }

            case "gra" -> {
                return PatternType.GRADIENT;
            }

            case "gru" -> {
                return PatternType.GRADIENT_UP;
            }

            case "cre" -> {
                return PatternType.CREEPER;
            }

            case "sku" -> {
                return PatternType.SKULL;
            }

            case "flo" -> {
                return PatternType.FLOWER;
            }

            case "moj" -> {
                return PatternType.MOJANG;
            }

            case "glb" -> {
                return PatternType.GLOBE;
            }

            case "pig" -> {
                return PatternType.PIGLIN;
            }

            default -> {
                return PatternType.BASE;
            }
        }
    }

    private DyeColor getDyeColour(String c) {

        switch (c) {

            case "orange" -> {
                return DyeColor.ORANGE;
            }

            case "magenta" -> {
                return DyeColor.MAGENTA;
            }

            case "light_blue" -> {
                return DyeColor.LIGHT_BLUE;
            }

            case "yellow" -> {
                return DyeColor.YELLOW;
            }

            case "lime" -> {
                return DyeColor.LIME;
            }

            case "pink" -> {
                return DyeColor.PINK;
            }

            case "gray" -> {
                return DyeColor.GRAY;
            }

            case "light_gray" -> {
                return DyeColor.LIGHT_GRAY;
            }

            case "cyan" -> {
                return DyeColor.CYAN;
            }

            case "purple" -> {
                return DyeColor.PURPLE;
            }

            case "blue" -> {
                return DyeColor.BLUE;
            }

            case "brown" -> {
                return DyeColor.BROWN;
            }

            case "green" -> {
                return DyeColor.GREEN;
            }

            case "red" -> {
                return DyeColor.RED;
            }

            case "black" -> {
                return DyeColor.BLACK;
            }

            default -> {
                return DyeColor.WHITE;
            }
        }
    }

    private Rotatable setRotation(Rotatable rot, byte rotation) {
        switch (rotation) {

            case 0 -> rot.setRotation(BlockFace.SOUTH);
            case 1 -> rot.setRotation(BlockFace.SOUTH_SOUTH_WEST);
            case 2 -> rot.setRotation(BlockFace.SOUTH_WEST);
            case 3 -> rot.setRotation(BlockFace.WEST_SOUTH_WEST);
            case 4 -> rot.setRotation(BlockFace.WEST);
            case 5 -> rot.setRotation(BlockFace.WEST_NORTH_WEST);
            case 6 -> rot.setRotation(BlockFace.NORTH_WEST);
            case 7 -> rot.setRotation(BlockFace.NORTH_NORTH_WEST);
            case 8 -> rot.setRotation(BlockFace.NORTH);
            case 9 -> rot.setRotation(BlockFace.NORTH_NORTH_EAST);
            case 10 -> rot.setRotation(BlockFace.NORTH_EAST);
            case 11 -> rot.setRotation(BlockFace.EAST_NORTH_EAST);
            case 12 -> rot.setRotation(BlockFace.EAST);
            case 13 -> rot.setRotation(BlockFace.EAST_SOUTH_EAST);
            case 14 -> rot.setRotation(BlockFace.SOUTH_EAST);
            case 15 -> rot.setRotation(BlockFace.SOUTH_SOUTH_EAST);

        }

        return rot;
    }

    private void setBannerPatterns(Block block, JSONArray patterns) {
        if (!patterns.isEmpty()) {
            Banner banner = (Banner) block.getState();
            patterns.forEach((p) -> {

                JSONObject pattern = (JSONObject) p;

                banner.addPattern(new Pattern(getDyeColour((String) pattern.get("colour")),
                        getPatternType((String) pattern.get("pattern"))));

            });
            banner.update();
        }
    }

    private boolean redstoneConnects(Block block, BlockFace face) {

        switch (block.getType()) {

            case REDSTONE_WIRE, REDSTONE_BLOCK, REDSTONE_TORCH, LEVER, DAYLIGHT_DETECTOR, COMPARATOR,
                    STONE_BUTTON, OAK_BUTTON, TRAPPED_CHEST, STONE_PRESSURE_PLATE, OAK_PRESSURE_PLATE,
                    HEAVY_WEIGHTED_PRESSURE_PLATE, LIGHT_WEIGHTED_PRESSURE_PLATE, DETECTOR_RAIL -> {
                return true;
            }

            case REPEATER -> {
                Repeater repeater = (Repeater) block.getBlockData();
                if ((face == BlockFace.NORTH || face == BlockFace.SOUTH) &&
                        (repeater.getFacing() == BlockFace.NORTH || repeater.getFacing() == BlockFace.SOUTH)) {
                    return true;
                } else if ((face == BlockFace.EAST || face == BlockFace.WEST) &&
                        (repeater.getFacing() == BlockFace.EAST || repeater.getFacing() == BlockFace.WEST)) {
                    return true;
                } else {
                    return false;
                }
            }

            case OBSERVER -> {
                Observer observer = (Observer) block.getBlockData();
                return (observer.getFacing() == face);
            }

            default -> {
                return false;
            }
        }
    }

    private boolean redstoneConnectsUp(Location l, BlockFace face) {

        //Check if the block above is redstone, else return null.
        Location lUp = new Location(world, l.getX(), l.getY() + 1, l.getZ());
        if (world.getType(lUp) != Material.REDSTONE_WIRE) {
            return false;
        }

        //Check if the block is a slab or stair, these are special cases.
        if (world.getBlockData(l) instanceof Stairs) {
            Stairs stair = (Stairs) world.getBlockData(l);
            return ((stair.getHalf() == Bisected.Half.TOP) && (((facingNumber(face) + 2 % 4)) == facingNumber(stair.getFacing())));
        } else if (world.getBlockData(l) instanceof Slab) {
            Slab slab = (Slab) world.getBlockData(l);
            return (slab.getType() == Slab.Type.DOUBLE);
        } else if (!world.getType(l).isOccluding()) {
            return false;
        } else {
            return true;
        }
    }

    //Set facing direction to a number for convenience.
    public int facingNumber(BlockFace f) {
        switch (f) {
            case NORTH -> {return 0;}
            case EAST -> {return 1;}
            case WEST -> {return 3;}
            case SOUTH -> {return 2;}
        }
        return 0;
    }

}
