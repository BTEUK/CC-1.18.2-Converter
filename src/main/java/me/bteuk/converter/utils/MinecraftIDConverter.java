package me.bteuk.converter.utils;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.StringTag;

public class MinecraftIDConverter {

    //Convert a legacy Minecraft block id and get the 1.18.2 palette for the block.
    public static CompoundTag getBlock(byte id, byte data) {

        //Create map for block.
        CompoundMap block = new CompoundMap();

        //Add block name as String tag.
        block.put(new StringTag("Name", getNameSpace(id, data)));

        //If block has properties, add them.
        if (hasBlockStates(id)) {
            block.put(getBlockStates(id, data));
        }

        return new CompoundTag("", block);
    }


    //Convert a legacy Minecraft block id and get the 1.18.2 namespace version.
    public static String getNameSpace(byte id, byte data) {
        return ("minecraft:" + getBlock(id, data));
    }

    //Check if the block has block states in 1.18.2, in this case it needs a properties tag in the palette.
    public static boolean hasBlockStates(byte id) {

        switch (id) {

            case 2, 23, 51, 54, 64, 71, 77, 81, 85, 92, 113, 117, 118, 120, 127,
                    (byte) 130, (byte) 137, (byte) 141, (byte) 143, (byte) 145, (byte) 146, (byte) 151, (byte) 158,
                    (byte) 176, (byte) 177, (byte) 178, (byte) 188, (byte) 189, (byte) 190, (byte) 191, (byte) 192,
                    (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197,
                    (byte) 198, (byte) 199, (byte) 200, (byte) 207,(byte) 210, (byte) 211,
                    (byte) 216 -> {return true;}

        }

        return false;

    }

    //Get the block states of a block.
    public static CompoundTag getBlockStates(byte id, byte data) {

        CompoundMap block_states = new CompoundMap();

        switch (id) {

            //Anvil (all types)
            case (byte) 145 -> {

                switch (data) {
                    case 0,4,8 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11 -> block_states.put(new StringTag("facing", "east"));
                }
            }

            //TODO Get banner colour
            //Standing Banner
            case (byte) 176 -> block_states.put(new StringTag("rotation", String.valueOf(data)));

            //TODO Get banner colour
            //Wall Banner
            case (byte) 177 -> {

                switch (data) {

                    case 0,1,2,7,8,12,13,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //TODO Get bed colour
            //Bed (all colours)
            case 26 -> {

                //part
                switch (data) {

                    case 0,1,2,3,4,5,6,7 -> block_states.put(new StringTag("part", "foot"));
                    case 8,9,10,11,12,13,14,15 -> block_states.put(new StringTag("part", "head"));

                }

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "east"));

                }

                //occupied
                block_states.put(new StringTag("occupied", "false"));

            }

            //Beetroots
            case (byte) 207 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Bone Block
            case (byte) 216 -> {

                switch (data) {

                    case 0,1,2,3,12,13,14,15 -> block_states.put(new StringTag("axis", "y"));
                    case 4,5,6,7 -> block_states.put(new StringTag("axis", "x"));
                    case 8,9,10,11 -> block_states.put(new StringTag("axis", "z"));

                }
            }

            //Brewing Stand
            case 117 -> {

                //Has Bottle 0
                switch (data) {

                    case 0,2,4,6,8,10,12,14 -> block_states.put(new StringTag("has_bottle_0", "false"));
                    case 1,3,5,7,9,11,13,15 -> block_states.put(new StringTag("has_bottle_0", "true"));

                }

                //Has Bottle 1
                switch (data) {

                    case 0,1,4,5,8,9,12,13 -> block_states.put(new StringTag("has_bottle_1", "false"));
                    case 2,3,6,7,10,11,14,15 -> block_states.put(new StringTag("has_bottle_1", "true"));

                }

                //Has Bottle 2
                switch (data) {

                    case 0,1,2,3,8,9,10,11 -> block_states.put(new StringTag("has_bottle_2", "false"));
                    case 4,5,6,7,12,13,14,15 -> block_states.put(new StringTag("has_bottle_2", "true"));

                }
            }

            //Stone/Wooden Button
            case 77, (byte) 143 -> {

                //face
                switch (data) {

                    case 0,8 -> block_states.put(new StringTag("face", "ceiling"));
                    case 1,2,3,4,9,10,11,12 -> block_states.put(new StringTag("face", "wall"));
                    case 5,6,7,13,14,15 -> block_states.put(new StringTag("face", "floor"));

                }

                //facing
                switch (data) {

                    case 0,4,5,6,7,8,12,13,14,15 -> block_states.put(new StringTag("facing", "north"));
                    case 1,9 -> block_states.put(new StringTag("facing", "east"));
                    case 2,10 -> block_states.put(new StringTag("facing", "west"));
                    case 3,11 -> block_states.put(new StringTag("facing", "south"));

                }

                //powered
                block_states.put(new StringTag("powered", "false"));

            }

            //Cactus
            case 81 -> block_states.put(new StringTag("age", "0"));

            //Cake
            case 92 -> block_states.put(new StringTag("bites", String.valueOf(data)));

            //Carrots
            case (byte) 141 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Cauldron
            case 118 -> block_states.put(new StringTag("level", String.valueOf(data)));

            //(Trapped) Chest
            case 54, (byte) 146 -> {

                //facing
                switch (data) {

                    case 0,1,2,6,7,8,12,13,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));

                }

                //type
                block_states.put(new StringTag("type", "single"));

                //waterlogged
                block_states.put(new StringTag("waterlogged", "false"));

            }

            //Ender Chest
            case (byte) 130 -> {

                //facing
                switch (data) {

                    case 0,1,2,6,7,8,12,13,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));

                }

                //waterlogged
                block_states.put(new StringTag("waterlogged", "false"));

            }

            //Chorus Flower
            case (byte) 200 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Chorus Plant
            case (byte) 199 -> {

                //down
                block_states.put(new StringTag("down", "false"));

                //east
                block_states.put(new StringTag("east", "false"));

                //north
                block_states.put(new StringTag("north", "false"));

                //south
                block_states.put(new StringTag("south", "false"));

                //up
                block_states.put(new StringTag("up", "false"));

                //west
                block_states.put(new StringTag("west", "false"));

            }

            //Cocoa
            case 127 -> {

                //age
                switch (data) {

                    case 0,1,2,3 -> block_states.put(new StringTag("age", "0"));
                    case 4,5,6,7 -> block_states.put(new StringTag("age", "1"));
                    case 8,9,10,11 -> block_states.put(new StringTag("age", "2"));

                }

                //facing
                switch (data) {

                    case 0,4,8 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //Command Block
            case (byte) 137, (byte) 210, (byte) 211 -> {

                //conditional
                switch (data) {

                    case 0,1,2,3,4,5,6,7 -> block_states.put(new StringTag("conditional", "false"));
                    case 8,9,10,11,12,13,14,15 -> block_states.put(new StringTag("conditional", "true"));

                }

                //facing
                switch (data) {

                    case 0,6,8,14 -> block_states.put(new StringTag("facing", "down"));
                    case 1,7,9,15 -> block_states.put(new StringTag("facing", "up"));
                    case 2,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,11 -> block_states.put(new StringTag("facing", "south"));
                    case 4,12 -> block_states.put(new StringTag("facing", "west"));
                    case 5,13 -> block_states.put(new StringTag("facing", "earth"));

                }
            }

            //Daylight Sensor
            case (byte) 151 -> {

                //inverted
                block_states.put(new StringTag("inverted", "false"));

                //power
                block_states.put(new StringTag("power", "0"));


            }

            //Inverted Daylight Sensor
            case (byte) 178 -> {

                //inverted
                block_states.put(new StringTag("inverted", "true"));

                //power
                block_states.put(new StringTag("power", "0"));

            }

            //Dispenser and Dropper
            case 23, (byte) 158 -> {

                //facing
                switch (data) {

                    case 0,6,8,14 -> block_states.put(new StringTag("facing", "down"));
                    case 1,7,9,15 -> block_states.put(new StringTag("facing", "up"));
                    case 2,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,11 -> block_states.put(new StringTag("facing", "south"));
                    case 4,12 -> block_states.put(new StringTag("facing", "west"));
                    case 5,13 -> block_states.put(new StringTag("facing", "east"));

                }

                //triggered
                block_states.put(new StringTag("triggered", "false"));

            }

            //Doors
            case 64, 71, (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197 -> {

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "east"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "south"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "west"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "north"));

                }

                //half
                switch (data) {

                    case 0,1,2,3,4,5,6,7 -> block_states.put(new StringTag("half", "lower"));
                    case 8,9,10,11,12,13,14,15 -> block_states.put(new StringTag("half", "upper"));

                }

                //hinge
                switch (data) {

                    case 9,11,13,15 -> block_states.put(new StringTag("hinge", "right"));
                    default -> block_states.put(new StringTag("hinge", "left"));

                }

                //open
                switch (data) {

                    case 4,5,6,7 -> block_states.put(new StringTag("open", "true"));
                    default -> block_states.put(new StringTag("open", "false"));

                }

                //powered
                block_states.put(new StringTag("powered", "false"));

            }

            //End Portal Frame
            case 120 -> {

                //eye
                switch (data) {

                    case 4,5,6,7,12,13,14,15 -> block_states.put(new StringTag("eye", "true"));
                    default -> block_states.put(new StringTag("eye", "false"));

                }

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //End Rod
            case (byte) 198 -> {

                //facing
                switch (data) {

                    case 0,6,12 -> block_states.put(new StringTag("facing", "down"));
                    case 1,7,13 -> block_states.put(new StringTag("facing", "up"));
                    case 2,8,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //Farmland
            case 60 -> {

                switch (data) {

                    //moisture
                    case 0, 8 -> block_states.put(new StringTag("moisture", "0"));
                    case 1, 9 -> block_states.put(new StringTag("moisture", "1"));
                    case 2, 10 -> block_states.put(new StringTag("moisture", "2"));
                    case 3, 11 -> block_states.put(new StringTag("moisture", "3"));
                    case 4, 12 -> block_states.put(new StringTag("moisture", "4"));
                    case 5, 13 -> block_states.put(new StringTag("moisture", "5"));
                    case 6, 14 -> block_states.put(new StringTag("moisture", "6"));
                    case 7, 15 -> block_states.put(new StringTag("moisture", "7"));

                }
            }

            //Fences
            case 85, 113, (byte) 188, (byte) 189, (byte) 190, (byte) 191, (byte) 192 -> {

                block_states.put(new StringTag("east", "false"));
                block_states.put(new StringTag("north", "false"));
                block_states.put(new StringTag("south", "false"));
                block_states.put(new StringTag("waterlogged", "false"));
                block_states.put(new StringTag("west", "false"));

            }

            //Fence Gates
            case 107, (byte) 183, (byte) 184, (byte) 185, (byte) 186, (byte) 187 -> {

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "east"));

                }

                //in_wall
                block_states.put(new StringTag("in_wall", "false"));

                //open
                switch (data) {

                    case 4,5,6,7,12,13,14,15 -> block_states.put(new StringTag("open", "true"));
                    default -> block_states.put(new StringTag("open", "false"));

                }

                //powered
                block_states.put(new StringTag("powered", "false"));

            }

            //Fire
            case 51 -> {

                block_states.put(new StringTag("age", "0"));
                block_states.put(new StringTag("east", "false"));
                block_states.put(new StringTag("north", "false"));
                block_states.put(new StringTag("south", "false"));
                block_states.put(new StringTag("up", "false"));
                block_states.put(new StringTag("west", "false"));

            }

            //Double Plant
            case (byte) 175 -> {

                switch (data) {

                    case 0,1,2,3,4,5,6,7 -> block_states.put(new StringTag("half", "lower"));
                    default -> block_states.put(new StringTag("half", "upper"));

                }

            }




        }


        return new CompoundTag("Properties", block_states);
    }

    //Get the 1.18.2 block name.
    public static String getBlockName(byte id, byte data) {

        switch (id) {

            //Air
            case 0 -> {
                return "air";
            }

            //Stone
            case 1 -> {
                switch (data) {

                    case 0 -> {
                        return "stone";
                    }

                    case 1 -> {
                        return "granite";
                    }

                    case 2 -> {
                        return "polished_granite";
                    }

                    case 3 -> {
                        return "diorite";
                    }

                    case 4 -> {
                        return "polished_diorite";
                    }

                    case 5 -> {
                        return "andesite";
                    }

                    case 6 -> {
                        return "polished_andesite";
                    }
                }
            }

            //Grass
            case 2 -> {
                return "grass_block";
            }

            //Dirt
            case 3 -> {
                switch (data) {

                    case 0 -> {
                        return "dirt";
                    }

                    case 1 -> {
                        return "coarse_dirt";
                    }

                    case 2 -> {
                        return "podzol";
                    }
                }
            }

            //Cobblestone
            case 4 -> {
                return "cobblestone";
            }

            //Planks
            case 5 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_planks";
                    }

                    case 1 -> {
                        return "spruce_planks";
                    }

                    case 2 -> {
                        return "birch_planks";
                    }

                    case 3 -> {
                        return "jungle_planks";
                    }

                    case 4 -> {
                        return "acacia_planks";
                    }

                    case 5 -> {
                        return "dark_oak_planks";
                    }
                }
            }

            //Sapling
            case 6 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_sapling";
                    }

                    case 1 -> {
                        return "spruce_sapling";
                    }

                    case 2 -> {
                        return "birch_sapling";
                    }

                    case 3 -> {
                        return "jungle_sapling";
                    }

                    case 4 -> {
                        return "acacia_sapling";
                    }

                    case 5 -> {
                        return "dark_oak_sapling";
                    }
                }
            }

            //Bedrock
            case 7 -> {
                return "bedrock";
            }

            //TODO Water

            //TODO Lava

            //Sand
            case 12 -> {
                switch (data) {

                    case 0 -> {
                        return "sand";
                    }

                    case 1 -> {
                        return "red_sand";
                    }
                }
            }

            //Gravel
            case 13 -> {
                return "gravel";
            }

            //Gold Ore
            case 14 -> {
                return "gold_ore";
            }

            //Iron Ore
            case 15 -> {
                return "iron_ore";
            }

            //Coal Ore
            case 16 -> {
                return "coal_ore";
            }

            //TODO Log Directions
            //Log
            case 17 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_log";
                    }

                    case 1 -> {
                        return "spruce_log";
                    }

                    case 2 -> {
                        return "birch_log";
                    }

                    case 3 -> {
                        return "jungle_log";
                    }
                }
            }

            //TODO Log2 Directions
            //Log 2
            case (byte) 162 -> {
                switch (data) {

                    case 0 -> {
                        return "acacia_log";
                    }

                    case 1 -> {
                        return "dark_oak_log";
                    }
                }
            }

            //TODO Persistent Leaves
            //Leaves
            case 18 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_leaves";
                    }

                    case 1 -> {
                        return "spruce_leaves";
                    }

                    case 2 -> {
                        return "birch_leaves";
                    }

                    case 3 -> {
                        return "jungle_leaves";
                    }
                }
            }

            //TODO Persistent Leaves
            //Leaves 2
            case (byte) 161 -> {
                switch (data) {

                    case 0 -> {
                        return "acacia_leaves";
                    }

                    case 1 -> {
                        return "dark_oak_leaves";
                    }
                }
            }

            //Sponge
            case 19 -> {
                switch (data) {

                    case 0 -> {
                        return "sponge";
                    }

                    case 1 -> {
                        return "wet_sponge";
                    }
                }
            }

            //Glass
            case 20 -> {
                return "glass";
            }

            //Lapis Ore
            case 21 -> {
                return "lapis_ore";
            }

            //Lapis Block
            case 22 -> {
                return "lapis_block";
            }

            //Dispenser
            case 23 -> {
                return "dispenser";
            }

            //Sandstone
            case 24 -> {
                switch (data) {

                    case 0 -> {
                        return "sandstone";
                    }

                    case 1 -> {
                        return "chiseled_sandstone";
                    }

                    case 2 -> {
                        return "cut_sandstone";
                    }
                }
            }

            //Noteblock
            case 25 -> {
                return "note_block";
            }

            //Golden Rail
            case 27 -> {
                return "powered_rail";
            }

            //Detector Rail
            case 28 -> {
                return "detector_rail";
            }

            //Sticky Piston
            case 29 -> {
                return "sticky_piston";
            }

            //Cobweb
            case 30 -> {
                return "cobweb";
            }

            //Tallgrass
            case 31 -> {
                switch (data) {

                    case 0 -> {
                        return "dead_bush";
                    }

                    case 1 -> {
                        return "grass";
                    }

                    case 2 -> {
                        return "fern";
                    }
                }
            }

            //Dead Bush
            case 32 -> {
                return "dead_bush";
            }

            //Piston
            case 33 -> {
                return "piston";
            }

            //Wool
            case 35 -> {
                switch (data) {

                    case 0 -> {
                        return "white_wool";
                    }

                    case 1 -> {
                        return "orange_wool";
                    }

                    case 2 -> {
                        return "magenta_wool";
                    }

                    case 3 -> {
                        return "light_blue_wool";
                    }

                    case 4 -> {
                        return "yellow_wool";
                    }

                    case 5 -> {
                        return "lime_wool";
                    }

                    case 6 -> {
                        return "pink_wool";
                    }

                    case 7 -> {
                        return "gray_wool";
                    }

                    case 8 -> {
                        return "light_gray_wool";
                    }

                    case 9 -> {
                        return "cyan_wool";
                    }

                    case 10 -> {
                        return "purple_wool";
                    }

                    case 11 -> {
                        return "blue_wool";
                    }

                    case 12 -> {
                        return "brown_wool";
                    }

                    case 13 -> {
                        return "green_wool";
                    }

                    case 14 -> {
                        return "red_wool";
                    }

                    case 15 -> {
                        return "black_wool";
                    }
                }
            }

            //Yellow Flower
            case 37 -> {
                return "dandelion";
            }

            //Red Flower
            case 38 -> {
                switch (data) {

                    case 0 -> {
                        return "poppy";
                    }

                    case 1 -> {
                        return "blue_orchid";
                    }

                    case 2 -> {
                        return "allium";
                    }

                    case 3 -> {
                        return "azure_bluet";
                    }

                    case 4 -> {
                        return "red_tulip";
                    }

                    case 5 -> {
                        return "orange_tulip";
                    }

                    case 6 -> {
                        return "white_tulip";
                    }

                    case 7 -> {
                        return "pink_tulip";
                    }

                    case 8 -> {
                        return "oxeye_daisy";
                    }
                }
            }

            //Brown Mushroom
            case 39 -> {
                return "brown_mushroom";
            }

            //Red Mushroom
            case 40 -> {
                return "red_mushroom";
            }

            //Gold Block
            case 41 -> {
                return "gold_block";
            }

            //Iron Block
            case 42 -> {
                return "iron_block";
            }

            //TODO Top Half of Slab
            //Wooden Slab
            case 126 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_slab";
                    }

                    case 1 -> {
                        return "spruce_slab";
                    }

                    case 2 -> {
                        return "birch_slab";
                    }

                    case 3 -> {
                        return "jungle_slab";
                    }

                    case 4 -> {
                        return "acacia_slab";
                    }

                    case 5 -> {
                        return "dark_oak_slab";
                    }
                }
            }

            //TODO Top Half of Slab
            //Stone Slab
            case 44 -> {
                switch (data) {

                    case 0 -> {
                        return "stone_slab";
                    }

                    case 1 -> {
                        return "sandstone_slab";
                    }

                    case 2 -> {
                        return "petrified_oak_slab";
                    }

                    case 3 -> {
                        return "cobblestone_slab";
                    }

                    case 4 -> {
                        return "brick_slab";
                    }

                    case 5 -> {
                        return "stone_brick_slab";
                    }

                    case 6 -> {
                        return "nether_brick_slab";
                    }

                    case 7 -> {
                        return "quartz_slab";
                    }
                }
            }

            //Stone Slab 2
            case (byte) 182 -> {
                return "red_sandstone_slab";
            }

            //Purpur Slab
            case (byte) 205 -> {
                return "purpur_slab";
            }

            //TODO Double Slabs
            //Double Stone Slab
            case 43 -> {
                switch (data) {

                    case 7 -> {
                        return "smooth_quartz";
                    }

                    case 8 -> {
                        return "smooth_stone";
                    }
                }
            }

            //Brick Block
            case 45 -> {
                return "bricks";
            }

            //Tnt


        }

        return "air";

    }

    public static String getNameSpace(byte id) {
        return getNameSpace(id, (byte) 0);
    }
}
