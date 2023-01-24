package me.bteuk.converterplugin;

import me.bteuk.converterplugin.utils.blocks.stairs.StairData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
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
                    Stairs zMax = (Stairs) world.getBlockData(lZMax);
                    if (mainStair.half == zMax.getHalf()) {
                        //Add it to the array at index 3.
                        stairs[3] = new StairData(zMax, lZMax, mainStair);
                    }
                }

                //Set the stair shape.
                stair.setShape(mainStair.getShape(stairs));

                //Update the block.
                world.setBlockData(l, stair);

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
                    "minecraft:purple_stained_glass_pane", "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane", "minecraft:light_gray_stained_glass_pane",
                    "minecraft:iron_bars" -> {

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

    private boolean canConnect(Material mat, Location l, BlockData bd, BlockFace face) {

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

            Stairs stair = (Stairs) bd;

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
                case UP -> {
                    return (stair.getHalf() == Bisected.Half.BOTTOM);
                }
            }
        }

        //Slab Case
        if (bd instanceof Slab) {
            Slab slab = (Slab) bd;
            if (face == BlockFace.UP) {
                return (slab.getType() == Slab.Type.BOTTOM || slab.getType() == Slab.Type.DOUBLE);
            }
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
            if (face == BlockFace.UP) {
                return true;
            }
            return false;
        }

        //Doors
        if (bd instanceof Door) {
            Door door = (Door) bd;
            return (door.getFacing() == face && !door.isOpen());

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
            }}

        //All other cases.
        return false;
    }

    private boolean isFence(Material mat) {
        return (mat == Material.OAK_FENCE || mat == Material.SPRUCE_FENCE || mat == Material.BIRCH_FENCE || mat == Material.JUNGLE_FENCE || mat == Material.ACACIA_FENCE || mat == Material.DARK_OAK_FENCE || mat == Material.NETHER_BRICK_FENCE);
    }

}
