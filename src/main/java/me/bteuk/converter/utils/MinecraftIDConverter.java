package me.bteuk.converter.utils;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MinecraftIDConverter {
    
        /*

    List of blocks for post-processing:

        175:11 (Top half of double_plant
        53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 156, 163, 164, 180, 203 (Stairs shape)
        85, 113, 188, 189, 190, 191, 192 (Fence connections)
        139, 139:1 (Wall connections)
        101 (Iron bar connections)
        102, 160:0 to 160:15 (Glass pane connections)
        54, 146 (Chest connections)
        55 (Redstone connections)
        199 (Chorus plant connections)
        26 (Bed colour)
        176, 177 (Banner colour and pattern)
        140 (Flower pot content)
        144 (Mob head types and player head texture)
        25 (Note block content)
        132 (Tripwire connections)
        106 (Vine on the top side)

     */

    //Convert a legacy Minecraft block id and get the 1.18.2 palette for the block.
    public static CompoundTag getBlock(LegacyID id) {

        //Create map for block.
        CompoundTag block = new CompoundTag();

        //Add block name as String tag.
        block.putString("Name", getNameSpace(id.getID(), id.getData()));

        //If block has properties, add them.
        if (hasBlockStates(id.getID(), id.getData())) {
            block.put("Properties", getBlockStates(id.getID(), id.getData()));
        }

        return block;
    }

    //Convert a legacy Minecraft biome id and get teh 1.18.2 namespace.
    public static String getBiome(byte id) {
        return "minecraft:" + getBiomeName(id);
    }

    public static String getBiomeName(byte id) {

        switch (id) {

            //Ocean
            case 0 -> {
                return "ocean";
            }

            //Plains
            case 1 -> {
                return "plains";
            }

            //Desert (Hills)
            case 2, 17, (byte) 130 -> {
                return "desert";
            }

            //Extreme Hills
            case 3, (byte) 131 -> {
                return "stony_peaks";
            }

            //Taiga (Hills)
            case 5, 19, (byte) 133 -> {
                return "taiga";
            }

            //Swampland
            case 6, (byte) 134 -> {
                return "swamp";
            }

            //River
            case 7 -> {
                return "river";
            }

            //Hell
            case 8 -> {
                return "nether_wastes";
            }

            //Sky
            case 9 -> {
                return "the_end";
            }

            //Frozen Ocean
            case 10 -> {
                return "frozen_ocean";
            }

            //Frozen River
            case 11 -> {
                return "frozen_river";
            }

            //Ice Flats
            case 12 -> {
                return "snowy_plains";
            }

            //Ice Mountains
            case 13 -> {
                return "frozen_peaks";
            }

            //Mushroom Island (Shore)
            case 14, 15 -> {
                return "mushroom_fields";
            }

            //Beach
            case 16 -> {
                return "beach";
            }

            //Extreme Hills Edge
            case 20 -> {
                return "windswept_hills";
            }

            //Jungle (Hills)
            case 21, 22, (byte) 149 -> {
                return "jungle";
            }

            //Jungle Edge
            case 23, (byte) 151 -> {
                return "sparse_jungle";
            }

            //Deep Ocean
            case 24 -> {
                return "deep_ocean";
            }

            //Stone Beach
            case 25 -> {
                return "stony_shore";
            }

            //Cold Beach
            case 26 -> {
                return "snowy_beach";
            }

            //Birch Forest (Hills)
            case 27, 28, (byte) 155, (byte) 156 -> {
                return "birch_forest";
            }

            //Roofed Forest
            case 29, (byte) 157 -> {
                return "dark_forest";
            }

            //Cold Taiga (Hills)
            case 30, 31, (byte) 158 -> {
                return "snowy_taiga";
            }

            //Mega Taiga (Hills)
            case 32, 33, (byte) 160, (byte) 161 -> {
                return "old_growth_spruce_taiga";
            }

            //Extreme Hills+
            case 34, (byte) 162 -> {
                return "windswept_forest";
            }

            //Savanna
            case 35, (byte) 163 -> {
                return "savanna";
            }

            //Savanna Plateau
            case 36, (byte) 164 -> {
                return "savanna_plateau";
            }

            //Mesa (Plateau)
            case 37, 39, (byte) 167 -> {
                return "badlands";
            }

            //Mesa Plateau F
            case 38, (byte) 166 -> {
                return "wooded_badlands";
            }

            //Void
            case 127 -> {
                return "the_void";
            }

            //Sunflower Plains
            case (byte) 129 -> {
                return "sunflower_plains";
            }

            //Flower Forest
            case (byte) 132 -> {
                return "flower_forest";
            }

            //Ice Plains Spikes
            case (byte) 140 -> {
                return "ice_spikes";
            }

            //Mesa Bryce
            case (byte) 165 -> {
                return "eroded_badlands";
            }

            default -> {
                return "forest";
            }
        }
    }


    //Convert a legacy Minecraft block id and get the 1.18.2 namespace version.
    public static String getNameSpace(byte id, byte data) {
        return ("minecraft:" + getBlockName(id, data));
    }

    //Check if the block has block states in 1.18.2, in this case it needs a properties tag in the palette.
    public static boolean hasBlockStates(byte id, byte data) {

        //Unique case for dirt.
        if (id == 3) {
            if (data == 2) {
                return true;
            } else {
                return false;
            }
        }

        //Unique case for double slabs (smooth stone and smooth sandstone)
        if (id == 43) {
            if (data == 8 || data == 9) {
                return false;
            } else {
                return true;
            }
        }

        switch (id) {

            case (byte) 139, 106, (byte) 131, (byte) 132, 96, (byte) 167, 50, 46, (byte) 255, 53, 67, 108, 109,
                    114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 156, (byte) 163, (byte) 164,
                    (byte) 180, (byte) 203, 78, 43, 125, (byte) 181, (byte) 204, 44, 126, (byte) 182, (byte) 205,
                    68, 63, (byte) 219, (byte) 220, (byte) 221, (byte) 222, (byte) 223, (byte) 224, (byte) 225,
                    (byte) 226, (byte) 227, (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232,
                    (byte) 233, (byte) 234, 6, 76, 75, 93, 94, 123, 73, 124, 74, 55, (byte) 149, (byte) 150, 27,
                    28, (byte) 157, 66, (byte) 155, 70, 72, (byte) 147, (byte) 148, (byte) 142, (byte) 141, 59,
                    (byte) 207, 115, 104, 105, 83, 34, 29, 33, (byte) 218, 25, 90, 99, 100, 17, (byte) 162, 69,
                    18, (byte) 161, 10, 11, 65, 84, (byte) 154, (byte) 170, (byte) 202, 2, 110, 3, (byte) 235,
                    (byte) 236, (byte) 237, (byte) 238, (byte) 239, (byte) 240, (byte) 241, (byte) 242,
                    (byte) 243, (byte) 244, (byte) 245, (byte) 246, (byte) 247, (byte) 248, (byte) 249,
                    (byte) 250, 91, 86, 102, (byte) 160, 101, 61, 62, (byte) 212, (byte) 175, 51, 107, (byte) 183,
                    (byte) 184, (byte) 185, (byte) 186, (byte) 187, 85, 113, (byte) 188, (byte) 189, (byte) 190,
                    (byte) 191, (byte) 192, 60, (byte) 198, 120, 64, 71, (byte) 193, (byte) 194, (byte) 195,
                    (byte) 196, (byte) 197, 23, (byte) 158, (byte) 178, (byte) 151, (byte) 137, (byte) 210,
                    (byte) 211, 127, (byte) 199, (byte) 200, (byte) 130, 54, (byte) 146, 118, 92, 81, 77,
                    (byte) 143, 117, (byte) 216, 26, (byte) 177, (byte) 144, (byte) 176, (byte) 145, 8, 9 -> {
                return true;
            }

        }

        return false;

    }

    //Check if the block is a block entity (formally known as tile entity)
    public static boolean isBlockEntity(byte id) {

        switch (id) {

            case 23, 25, 26, 52, 54, 61, 62, 63, 68, 116, 117, 119, (byte) 130, (byte) 137, (byte) 138, (byte) 144,
                    (byte) 146, (byte) 149, (byte) 150, (byte) 151, (byte) 154, (byte) 158, (byte) 176,
                    (byte) 177, (byte) 178, (byte) 209, (byte) 210, (byte) 211, (byte) 219, (byte) 220, (byte) 221,
                    (byte) 222, (byte) 223, (byte) 224, (byte) 225, (byte) 226, (byte) 227, (byte) 228, (byte) 229,
                    (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234, (byte) 255 -> {
                return true;
            }

        }

        return false;

    }

    //Check if the block entity should not be added to the block entity list.
    public static boolean blockEntityNotAdded(byte id) {

        switch (id) {

            //Note block (not a block entity in 1.18.2).
            //Skull, since that will be done in post-processing.
            case 25, (byte) 144 -> {
                return true;
            }

        }

        return false;

    }

    //Get the properties of the block for post-processing.
    public static boolean hasProperties(byte id) {
        
        /*

        List of blocks for post-processing with properties:

        53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 156, 163, 164, 180, 203 (Stairs shape)
        26 (Bed colour)
        176, 177 (Banner colour and pattern)
        140 (Flower pot content)
        144 (Mob head types and player head texture)
        25 (Note block content)

        */

        switch (id) {

            case 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 156, (byte) 163,
                    (byte) 164, (byte) 180, (byte) 203, 26, (byte) 176, (byte) 177, (byte) 140, (byte) 144, 25 -> {
                return true;
            }

        }

        return false;

    }

    //Get properties for post-processing blocks and return a json object.
    public static JSONObject getProperties(byte id, byte data, CompoundTag block_entity) {
        
        /*

        List of blocks for post-processing with properties:

        53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 156, 163, 164, 180, 203 (Stairs shape)
        26 (Bed colour)
        176, 177 (Banner colour and pattern)
        140 (Flower pot content)
        144 (Mob head types and player head texture)
        25 (Note block content)

        */

        JSONObject jo = new JSONObject();

        switch (id) {

            //Stairs
            case 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 156, (byte) 163,
                    (byte) 164, (byte) 180, (byte) 203 -> {

                //facing
                switch (data) {

                    case 0, 4 -> jo.put("facing", "east");
                    case 1, 5 -> jo.put("facing", "west");
                    case 2, 6 -> jo.put("facing", "south");
                    case 3, 7 -> jo.put("facing", "north");

                }

                //half
                if (data >= 4) {
                    jo.put("half", "top");
                } else {
                    jo.put("half", "bottom");
                }
            }

            //Beds
            case 26 -> {

                //part
                switch (data) {

                    case 0, 1, 2, 3, 4, 5, 6, 7 -> jo.put("part", "foot");
                    case 8, 9, 10, 11, 12, 13, 14, 15 -> jo.put("part", "head");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> jo.put("facing", "south");
                    case 1, 5, 9, 13 -> jo.put("facing", "west");
                    case 2, 6, 10, 14 -> jo.put("facing", "north");
                    case 3, 7, 11, 15 -> jo.put("facing", "east");

                }

                //Get bed colour.
                jo.put("colour", colourNameSpace(block_entity.getInt("color")));
            }

            //Standing Banner
            case (byte) 176 -> {

                jo.put("rotation", String.valueOf(data));

                //Banner colour
                jo.put("colour", colourNameSpace(15 - block_entity.getInt("Base")));

                //Patterns
                JSONArray ja = new JSONArray();
                ListTag<CompoundTag> patterns = (ListTag<CompoundTag>) block_entity.getListTag("Patterns");
                if (patterns != null) {
                    for (CompoundTag pattern : patterns) {
                        JSONObject p = new JSONObject();
                        p.put("colour", colourNameSpace(15 - pattern.getInt("Color")));
                        p.put("pattern", pattern.getString("Pattern"));
                        ja.add(p);
                    }
                }

                jo.put("patterns", ja);
            }

            //Wall Banner
            case (byte) 177 -> {

                switch (data) {

                    case 2 -> jo.put("facing", "north");
                    case 3 -> jo.put("facing", "south");
                    case 4 -> jo.put("facing", "west");
                    case 5 -> jo.put("facing", "east");

                }

                //Banner colour
                jo.put("colour", colourNameSpace(15 - block_entity.getInt("Base")));

                //Patterns
                JSONArray ja = new JSONArray();
                ListTag<CompoundTag> patterns = (ListTag<CompoundTag>) block_entity.getListTag("Patterns");
                if (patterns != null) {
                    for (CompoundTag pattern : patterns) {
                        JSONObject p = new JSONObject();
                        p.put("colour", colourNameSpace(15 - pattern.getInt("Color")));
                        p.put("pattern", pattern.getString("Pattern"));
                        ja.add(p);
                    }
                }

                jo.put("patterns", ja);
            }

            //Flower Pot
            case (byte) 140 -> {

                //Null case
                if (block_entity == null) {
                    jo.put("type", "flower_pot");
                } else {

                    //Sort by item.
                    switch (block_entity.getString("Item")) {

                        case "minecraft:air" -> jo.put("type", "flower_pot");

                        case "minecraft:yellow_flower" -> jo.put("type", "potted_dandelion");

                        case "minecraft:red_flower" -> {

                            switch (block_entity.getByte("Data")) {

                                case 0 -> jo.put("type", "potted_poppy");
                                case 1 -> jo.put("type", "potted_blue_orchid");
                                case 2 -> jo.put("type", "potted_allium");
                                case 3 -> jo.put("type", "potted_azure_bluet");
                                case 4 -> jo.put("type", "potted_red_tulip");
                                case 5 -> jo.put("type", "potted_orange_tulip");
                                case 6 -> jo.put("type", "potted_white_tulip");
                                case 7 -> jo.put("type", "potted_pink_tulip");
                                case 8 -> jo.put("type", "potted_oxeye_daisy");

                            }
                        }

                        case "minecraft:sapling" -> {

                            switch (block_entity.getByte("Data")) {

                                case 0 -> jo.put("type", "potted_oak_sapling");
                                case 1 -> jo.put("type", "potted_spruce_sapling");
                                case 2 -> jo.put("type", "potted_birch_sapling");
                                case 3 -> jo.put("type", "potted_jungle_sapling");
                                case 4 -> jo.put("type", "potted_acacia_sapling");
                                case 5 -> jo.put("type", "potted_dark_oak_sapling");

                            }
                        }

                        case "minecraft:red_mushroom" -> jo.put("type", "potted_red_mushroom");
                        case "minecraft:brown_mushroom" -> jo.put("type", "potted_brown_mushroom");

                        case "minecraft:tallgrass" -> jo.put("type", "potted_fern");
                        case "minecraft:deadbush" -> jo.put("type", "potted_dead_bush");
                        case "minecraft:cactus" -> jo.put("type", "potted_cactus");

                    }
                }
            }

            //Mob and Player Heads
            case (byte) 144 -> {

                switch (data) {

                    case 2 -> jo.put("facing", "north");
                    case 3 -> jo.put("facing", "south");
                    case 4 -> jo.put("facing", "west");
                    case 5 -> jo.put("facing", "east");
                    default -> jo.put("facing", "floor");

                }
                //Skull type.
                switch (block_entity.getByte("SkullType")) {

                    case 0 -> jo.put("type", "skeleton_skull");
                    case 1 -> jo.put("type", "wither_skeleton_skull");
                    case 2 -> jo.put("type", "zombie_head");
                    case 3 -> {

                        jo.put("type", "player_head");

                        //Get owner data.
                        CompoundTag owner = block_entity.getCompoundTag("Owner");

                        if (owner != null) {

                            jo.put("id", owner.getString("Id"));

                            CompoundTag properties = owner.getCompoundTag("Properties");
                            ListTag<CompoundTag> textures = (ListTag<CompoundTag>) properties.getListTag("textures");

                            //Get first texture.
                            CompoundTag texture = textures.get(0);

                            jo.put("texture", texture.getString("Value"));

                        }

                    }
                    case 4 -> jo.put("type", "creeper_head");
                    case 5 -> jo.put("type", "dragon_head");

                }

                //Rotation
                jo.put("rotation", block_entity.getByte("Rot"));
            }

            //Note Block
            case 25 -> {
                jo.put("note", block_entity.getByte("note"));
            }

        }

        return jo;

    }

    //Get the namespace for a 1.12.2 colour id.
    public static String colourNameSpace(int id) {

        switch (id) {

            case 0 -> {
                return "white";
            }

            case 1 -> {
                return "orange";
            }

            case 2 -> {
                return "magenta";
            }

            case 3 -> {
                return "light_blue";
            }

            case 4 -> {
                return "yellow";
            }

            case 5 -> {
                return "lime";
            }

            case 6 -> {
                return "pink";
            }

            case 7 -> {
                return "gray";
            }

            case 8 -> {
                return "light_gray";
            }

            case 9 -> {
                return "cyan";
            }

            case 10 -> {
                return "purple";
            }

            case 11 -> {
                return "blue";
            }

            case 12 -> {
                return "brown";
            }

            case 13 -> {
                return "green";
            }

            case 14 -> {
                return "red";
            }

            case 15 -> {
                return "black";
            }

            default -> {
                return null;
            }
        }
    }

    //Check if the block requires post-processing, in this case store the data in a txt file.
    public static boolean requiredPostProcessing(byte id, byte data) {

        switch (id) {

            case (byte) 175 -> {
                if (data == 11) {
                    return true;
                }
            }

            case 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 156, (byte) 163,
                    (byte) 164, (byte) 180, (byte) 203, 85, 113, (byte) 188, (byte) 189, (byte) 190, (byte) 191,
                    (byte) 192, (byte) 139, 101, 102, (byte) 160, 54, (byte) 146, 55, (byte) 199, 26,
                    (byte) 176, (byte) 177, 104, 105, (byte) 140, (byte) 144, 25, (byte) 132, 106,
                    64, 71, (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197 -> {
                return true;
            }
        }

        return false;

    }

    //Get the block entity of a block
    public static CompoundTag getBlockEntity(byte id, byte data, CompoundTag tile_entity) {

        CompoundTag block_entity = new CompoundTag();

        //Add the global tags that are common to all block entities.
        //For unique case of spawner, beds, shulker boxes, signs and banners, the namespace of the block entity differs from the actual block namespace.
        if (id == 52) {
            block_entity.putString("id", "minecraft:mob_spawner");
        } else if (id == (byte) 176 || id == (byte) 177) {
            block_entity.putString("id", "minecraft:banner");
        } else if (id == 26) {
            block_entity.putString("id", "minecraft:bed");
        } else if (id == (byte) 219 || id == (byte) 220 || id == (byte) 221 || id == (byte) 222 ||
                id == (byte) 223 || id == (byte) 224 || id == (byte) 225 || id == (byte) 226 ||
                id == (byte) 227 || id == (byte) 228 || id == (byte) 229 || id == (byte) 230 ||
                id == (byte) 231 || id == (byte) 232 || id == (byte) 233 || id == (byte) 234) {
            block_entity.putString("id", "minecraft:shulker_box");
        } else if (id == 63 || id == 68) {
            block_entity.putString("id", "minecraft:sign");
        } else {
            block_entity.putString("id", getNameSpace(id, data));
        }
        block_entity.putBoolean("keepPacked", false);
        block_entity.putInt("x", tile_entity.getInt("x"));
        block_entity.putInt("y", tile_entity.getInt("y"));
        block_entity.putInt("z", tile_entity.getInt("z"));

        //Now for the unique tags.
        switch (id) {

            //Dispenser, (Trapped) Chest, Shulker Boxes and Dropper (all default values since we don't care what's inside)
            case 23, 54, (byte) 146, (byte) 158, (byte) 219, (byte) 220, (byte) 221,
                    (byte) 222, (byte) 223, (byte) 224, (byte) 225, (byte) 226, (byte) 227, (byte) 228, (byte) 229,
                    (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234 -> block_entity.put("Items", new ListTag<>(CompoundTag.class));

            //Mob Spawner (all default values)
            case 52 -> {

                block_entity.putShort("Delay", (short) 0);
                block_entity.putShort("MaxNearbyEntities", (short) 6);
                block_entity.putShort("MaxSpawnDelay", (short) 800);
                block_entity.putShort("MinSpawnDelay", (short) 200);
                block_entity.putShort("RequiredPlayerRange", (short) 16);
                block_entity.putShort("SpawnCount", (short) 4);
                block_entity.putShort("SpawnRange", (short) 4);

                CompoundTag spawnData = new CompoundTag();
                CompoundTag entity = new CompoundTag();
                entity.putString("id", "minecraft:pig");
                spawnData.put("entity", entity);

                block_entity.put("SpawnData", spawnData);

            }

            //Furnace
            case 61, 62 -> {

                block_entity.put("Items", new ListTag<>(CompoundTag.class));
                block_entity.put("RecipesUsed", new ListTag<>(CompoundTag.class));
                block_entity.putShort("BurnTime", (short) 0);
                block_entity.putShort("CookTime", (short) 0);
                block_entity.putShort("CookTimeTotal", (short) 200);
                block_entity.putShort("BurnTime", (short) 0);

            }

            //Sign
            case 63, 68 -> {

                block_entity.putBoolean("GlowingText", false);
                block_entity.putString("Color", "black");
                block_entity.putString("Text1", tile_entity.getString("Text1"));
                block_entity.putString("Text2", tile_entity.getString("Text2"));
                block_entity.putString("Text3", tile_entity.getString("Text3"));
                block_entity.putString("Text4", tile_entity.getString("Text4"));

            }

            //Brewing Stand
            case 117 -> {

                block_entity.put("Items", new ListTag<>(CompoundTag.class));
                block_entity.putShort("BrewTime", (short) 0);
                block_entity.putByte("Fuel", (byte) 0);

            }

            //Command Block
            case (byte) 137, (byte) 210, (byte) 211 -> {

                block_entity.putBoolean("auto", false);
                block_entity.putString("Command", "");
                block_entity.putBoolean("conditionMet", false);
                block_entity.putLong("LastExecution", 0);
                block_entity.putString("LastOutput", "");
                block_entity.putBoolean("powered", false);
                block_entity.putInt("SuccessCount", 0);
                block_entity.putBoolean("TrackOutput", true);
                block_entity.putBoolean("UpdateLastExecution", true);

            }

            //Beacon
            case (byte) 138 -> {

                block_entity.putInt("Levels", 0);
                block_entity.putInt("Primary", -1);
                block_entity.putInt("Secondary", -1);

            }

            //Redstone Comparator
            case (byte) 149, (byte) 150 -> block_entity.putInt("OutputSignal", 0);

            //Hopper
            case (byte) 154 -> {

                block_entity.put("Items", new ListTag<>(CompoundTag.class));
                block_entity.putInt("TransferCooldown", 0);

            }

            //Banners
            case (byte) 176, (byte) 177 -> block_entity.put("Patterns", new ListTag<>(CompoundTag.class));

            //End Gateway
            case (byte) 209 -> {

                block_entity.putLong("Age", 0);
                block_entity.putBoolean("ExactTeleport", true);

                CompoundTag exitPortal = new CompoundTag();
                exitPortal.putInt("X", 0);
                exitPortal.putInt("Y", 1);
                exitPortal.putInt("Z", 0);

                block_entity.put("ExitPortal", exitPortal);

            }

            //Structure Block
            case (byte) 255 -> {

                block_entity.putString("author", "?");
                block_entity.putBoolean("ignoreEntities", true);
                block_entity.putFloat("integrity", 1);
                block_entity.putString("metadata", "");
                block_entity.putString("mirror", "NONE");
                block_entity.putString("mode", "DATA");
                block_entity.putString("name", "");
                block_entity.putInt("posX", 0);
                block_entity.putInt("posY", 1);
                block_entity.putInt("posZ", 0);
                block_entity.putBoolean("powered", false);
                block_entity.putString("rotation", "NONE");
                block_entity.putLong("seed", 0);
                block_entity.putBoolean("showboundingbox", false);
                block_entity.putInt("sizeX", 1);
                block_entity.putInt("sizeY", 1);
                block_entity.putInt("sizeZ", 1);

            }

            //Other block entities which only have default values.
            //Includes: Bed, Enchantment Table, End Portal, Ender Chest, Daylight Detector

        }

        return block_entity;

    }

    //Get the block states of a block.
    public static CompoundTag getBlockStates(byte id, byte data) {

        CompoundTag block_states = new CompoundTag();

        switch (id) {

            //Anvil (all types)
            case (byte) 145 -> {

                switch (data) {
                    case 0, 4, 8 -> block_states.putString("facing", "south");
                    case 1, 5, 9 -> block_states.putString("facing", "west");
                    case 2, 6, 10 -> block_states.putString("facing", "north");
                    case 3, 7, 11 -> block_states.putString("facing", "east");
                }
            }

            //Standing Banner
            case (byte) 176 -> block_states.putString("rotation", String.valueOf(data));

            //Wall Banner and Mob Heads
            case (byte) 177, (byte) 144 -> {

                switch (data) {

                    case 2 -> block_states.putString("facing", "north");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "west");
                    case 5 -> block_states.putString("facing", "east");

                }
            }

            //Bed (all colours)
            case 26 -> {

                //part
                switch (data) {

                    case 0, 1, 2, 3, 4, 5, 6, 7 -> block_states.putString("part", "foot");
                    case 8, 9, 10, 11, 12, 13, 14, 15 -> block_states.putString("part", "head");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //occupied
                block_states.putString("occupied", "false");

            }

            //Bone Block
            case (byte) 216 -> {

                switch (data) {

                    case 0, 1, 2, 3, 12, 13, 14, 15 -> block_states.putString("axis", "y");
                    case 4, 5, 6, 7 -> block_states.putString("axis", "x");
                    case 8, 9, 10, 11 -> block_states.putString("axis", "z");

                }
            }

            //Brewing Stand
            case 117 -> {

                //Has Bottle 0
                switch (data) {

                    case 0, 2, 4, 6, 8, 10, 12, 14 -> block_states.putString("has_bottle_0", "false");
                    case 1, 3, 5, 7, 9, 11, 13, 15 -> block_states.putString("has_bottle_0", "true");

                }

                //Has Bottle 1
                switch (data) {

                    case 0, 1, 4, 5, 8, 9, 12, 13 -> block_states.putString("has_bottle_1", "false");
                    case 2, 3, 6, 7, 10, 11, 14, 15 -> block_states.putString("has_bottle_1", "true");

                }

                //Has Bottle 2
                switch (data) {

                    case 0, 1, 2, 3, 8, 9, 10, 11 -> block_states.putString("has_bottle_2", "false");
                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("has_bottle_2", "true");

                }
            }

            //Stone/Wooden Button
            case 77, (byte) 143 -> {

                //face
                switch (data) {

                    case 0, 8 -> block_states.putString("face", "ceiling");
                    case 1, 2, 3, 4, 9, 10, 11, 12 -> block_states.putString("face", "wall");
                    case 5, 6, 7, 13, 14, 15 -> block_states.putString("face", "floor");

                }

                //facing
                switch (data) {

                    case 0, 4, 5, 6, 7, 8, 12, 13, 14, 15 -> block_states.putString("facing", "north");
                    case 1, 9 -> block_states.putString("facing", "east");
                    case 2, 10 -> block_states.putString("facing", "west");
                    case 3, 11 -> block_states.putString("facing", "south");

                }

                //powered
                block_states.putString("powered", "false");

            }

            //Cactus
            case 81 -> block_states.putString("age", "0");

            //Cake
            case 92 -> {
                block_states.putString("bites", String.valueOf(data));
                block_states.putString("lit", "false");
            }

            //Cauldron
            case 118 -> block_states.putString("level", String.valueOf(data));

            //(Trapped) Chest
            case 54, (byte) 146 -> {

                //facing
                switch (data) {

                    case 0, 1, 2, 6, 7, 8, 12, 13, 14 -> block_states.putString("facing", "north");
                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");

                }

                //type
                block_states.putString("type", "single");

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Ender Chest
            case (byte) 130 -> {

                //facing
                switch (data) {

                    case 0, 1, 2, 6, 7, 8, 12, 13, 14 -> block_states.putString("facing", "north");
                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Chorus Flower
            case (byte) 200 -> block_states.putString("age", String.valueOf(data));

            //Chorus Plant
            case (byte) 199 -> {

                //down
                block_states.putString("down", "false");

                //east
                block_states.putString("east", "false");

                //north
                block_states.putString("north", "false");

                //south
                block_states.putString("south", "false");

                //up
                block_states.putString("up", "false");

                //west
                block_states.putString("west", "false");

            }

            //Cocoa
            case 127 -> {

                //age
                switch (data) {

                    case 0, 1, 2, 3 -> block_states.putString("age", "0");
                    case 4, 5, 6, 7 -> block_states.putString("age", "1");
                    case 8, 9, 10, 11 -> block_states.putString("age", "2");

                }

                //facing
                switch (data) {

                    case 0, 4, 8 -> block_states.putString("facing", "south");
                    case 1, 5, 9 -> block_states.putString("facing", "west");
                    case 2, 6, 10 -> block_states.putString("facing", "north");
                    case 3, 7, 11 -> block_states.putString("facing", "east");

                }
            }

            //Command Block
            case (byte) 137, (byte) 210, (byte) 211 -> {

                //conditional
                switch (data) {

                    case 0, 1, 2, 3, 4, 5, 6, 7 -> block_states.putString("conditional", "false");
                    case 8, 9, 10, 11, 12, 13, 14, 15 -> block_states.putString("conditional", "true");

                }

                //facing
                switch (data) {

                    case 0, 6, 8, 14 -> block_states.putString("facing", "down");
                    case 1, 7, 9, 15 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "earth");

                }
            }

            //Daylight Sensor
            case (byte) 151 -> {

                //inverted
                block_states.putString("inverted", "false");

                //power
                block_states.putString("power", "0");


            }

            //Inverted Daylight Sensor
            case (byte) 178 -> {

                //inverted
                block_states.putString("inverted", "true");

                //power
                block_states.putString("power", "0");

            }

            //Dispenser and Dropper
            case 23, (byte) 158 -> {

                //facing
                switch (data) {

                    case 0, 6, 8, 14 -> block_states.putString("facing", "down");
                    case 1, 7, 9, 15 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }

                //triggered
                block_states.putString("triggered", "false");

            }

            //Doors
            case 64, 71, (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197 -> {

                //Bottom half
                if (data < 8) {

                    block_states.putString("half", "lower");

                } else {

                    //Top half
                    block_states.putString("half", "upper");

                }

                //facing
                switch (data) {

                    case 1, 5 -> block_states.putString("facing", "south");
                    case 2, 6 -> block_states.putString("facing", "west");
                    case 3, 7 -> block_states.putString("facing", "north");
                    default -> block_states.putString("facing", "east");

                }

                //hinge
                switch (data) {

                    case 8, 10, 12, 14 -> block_states.putString("hinge", "left");
                    default -> block_states.putString("hinge", "right");

                }

                //open
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("open", "true");
                    default -> block_states.putString("open", "false");

                }

                //Powered
                switch (data) {
                    case 10, 11, 14, 15 -> block_states.putString("powered", "true");
                    default -> block_states.putString("powered", "false");
                }
            }

            //End Portal Frame
            case 120 -> {

                //eye
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("eye", "true");
                    default -> block_states.putString("eye", "false");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }
            }

            //End Rod
            case (byte) 198 -> {

                //facing
                switch (data) {

                    case 0, 6, 12 -> block_states.putString("facing", "down");
                    case 1, 7, 13 -> block_states.putString("facing", "up");
                    case 2, 8, 14 -> block_states.putString("facing", "north");
                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");

                }
            }

            //Farmland
            case 60 -> {

                switch (data) {

                    //moisture
                    case 0, 8 -> block_states.putString("moisture", "0");
                    case 1, 9 -> block_states.putString("moisture", "1");
                    case 2, 10 -> block_states.putString("moisture", "2");
                    case 3, 11 -> block_states.putString("moisture", "3");
                    case 4, 12 -> block_states.putString("moisture", "4");
                    case 5, 13 -> block_states.putString("moisture", "5");
                    case 6, 14 -> block_states.putString("moisture", "6");
                    case 7, 15 -> block_states.putString("moisture", "7");

                }
            }

            //Fences
            case 85, 113, (byte) 188, (byte) 189, (byte) 190, (byte) 191, (byte) 192 -> {

                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("waterlogged", "false");
                block_states.putString("west", "false");

            }

            //Fence Gates
            case 107, (byte) 183, (byte) 184, (byte) 185, (byte) 186, (byte) 187 -> {

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //in_wall
                block_states.putString("in_wall", "false");

                //open
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("open", "true");
                    default -> block_states.putString("open", "false");

                }

                //powered
                block_states.putString("powered", "false");

            }

            //Fire
            case 51 -> {

                block_states.putString("age", "0");
                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("up", "false");
                block_states.putString("west", "false");

            }

            //Double Plant
            case (byte) 175 -> {

                if (data == 11) {
                    block_states.putString("half", "upper");
                } else {
                    block_states.putString("half", "lower");
                }

            }

            //Frosted Ice
            case (byte) 212 -> {

                if (data < 3) {
                    block_states.putString("age", String.valueOf(data));
                } else {
                    block_states.putString("age", "3");
                }

            }

            //Furnace
            case 61, 62 -> {

                //lit
                if (id == 62) {
                    block_states.putString("lit", "true");
                } else {
                    block_states.putString("lit", "false");
                }

                //facing
                switch (data) {

                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");
                    default -> block_states.putString("facing", "north");

                }
            }

            //Glass Panes and Iron Bars
            case 102, (byte) 160, 101 -> {

                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("watterlogged", "false");
                block_states.putString("west", "false");

            }

            //Glazed Terracotta, Pumpkin Jack o'Lantern
            case (byte) 235, (byte) 236, (byte) 237, (byte) 238, (byte) 239, (byte) 240, (byte) 241, (byte) 242,
                    (byte) 243, (byte) 244, (byte) 245, (byte) 246, (byte) 247, (byte) 248, (byte) 249,
                    (byte) 250, 91, 86 -> {

                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }
            }

            //Grass, Myceleum and Podzol
            case 2, 110, 3 -> {

                if (!(id == 3 && data != 2)) {

                    block_states.putString("snowy", "false");

                }
            }

            //Hay Bale, Purpur Pillar and Logs
            case (byte) 170, (byte) 202, 17, (byte) 162 -> {

                switch (data) {

                    case 4, 5, 6, 7 -> block_states.putString("axis", "x");
                    case 8, 9, 10, 11 -> block_states.putString("axis", "z");
                    default -> block_states.putString("axis", "y");

                }
            }

            //Hopper
            case (byte) 154 -> {

                //enabled
                block_states.putString("enabled", "false");

                //facing
                switch (data) {

                    case 0, 6, 8, 14, 15 -> block_states.putString("facing", "down");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }
            }

            //Jukebox
            case 84 -> block_states.putString("has_record", "false");

            //Ladder
            case 65 -> {

                switch (data) {

                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");
                    default -> block_states.putString("facing", "north");

                }
            }

            //Lava
            case 10, 11 -> block_states.putString("level", String.valueOf(data));

            //Leaves
            case 18, (byte) 161 -> {

                block_states.putString("distance", "7");
                block_states.putString("persistent", "true");
                block_states.putString("waterlogged", "false");

            }

            //Lever
            case 69 -> {

                //face
                switch (data) {

                    case 0, 7, 8, 15 -> block_states.putString("face", "ceiling");
                    case 5, 6, 13, 14 -> block_states.putString("face", "floor");
                    case 1, 2, 3, 4, 9, 10, 11, 12 -> block_states.putString("face", "wall");

                }

                //facing
                switch (data) {

                    case 1, 9 -> block_states.putString("facing", "east");
                    case 2, 10, 0, 8, 6, 14 -> block_states.putString("facing", "west");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12, 7, 5, 13 -> block_states.putString("facing", "north");

                }

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

            }


            //Mushroom Blocks
            case 99, 100 -> {

                //east
                if (data == 3 || data == 6 || data == 9 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("east", "true");
                } else {
                    block_states.putString("east", "false");
                }

                //down
                if (data == 14 || data == 15) {
                    block_states.putString("down", "true");
                } else {
                    block_states.putString("down", "false");
                }

                //north
                if (data == 1 || data == 2 || data == 3 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("north", "true");
                } else {
                    block_states.putString("north", "false");
                }

                //south
                if (data == 7 || data == 8 || data == 9 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("south", "true");
                } else {
                    block_states.putString("south", "false");
                }

                //up
                if (data == 0 || data == 10) {
                    block_states.putString("up", "false");
                } else {
                    block_states.putString("up", "true");
                }

                //west
                if (data == 1 || data == 4 || data == 7 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("west", "true");
                } else {
                    block_states.putString("west", "false");
                }
            }

            //Nether Portal
            case 90 -> {
                if (data == 1) {
                    block_states.putString("axis", "z");
                } else {
                    block_states.putString("axis", "x");
                }
            }

            //Note Block
            case 25 -> {
                block_states.putString("instrument", "harp");
                block_states.putString("note", "0");
                block_states.putString("powered", "false");
            }

            //Observer
            case (byte) 218 -> {

                //facing
                switch (data) {

                    case 0, 8 -> block_states.putString("facing", "down");
                    case 1, 9 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }

                //powered
                block_states.putString("powered", "false");

            }

            //Pistons
            case 29, 33 -> {

                //extended
                if (data >= 8) {
                    block_states.putString("extended", "true");
                } else {
                    block_states.putString("extended", "false");
                }

                //facing
                switch (data) {

                    case 0, 8 -> block_states.putString("facing", "down");
                    case 1, 9 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }
            }

            //Piston Head
            case 34 -> {

                //facing
                switch (data) {

                    case 0, 8 -> block_states.putString("facing", "down");
                    case 1, 9 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }

                //short
                block_states.putString("short", "false");

                //type
                if (data >= 8) {
                    block_states.putString("type", "sticky");
                } else {
                    block_states.putString("type", "normal");
                }
            }

            //Potatoes, Carrots, Beetroots, Nether Wart, Melon/Pumpkin Stem, Sugar Canes and Wheat
            case (byte) 142, (byte) 141, 59, (byte) 207, 115, 104, 105, 83 -> block_states.putString("age", String.valueOf(data));

            //Pressure Plates
            case 70, 72, (byte) 147, (byte) 148 -> block_states.putString("powered", "false");

            //Quartz Pillar
            case (byte) 155 -> {
                switch (data) {

                    case 2 -> block_states.putString("axis", "y");
                    case 3 -> block_states.putString("axis", "x");
                    case 4 -> block_states.putString("axis", "z");

                }
            }

            //Rail
            case 66 -> {

                //shape
                switch (data) {

                    case 0 -> block_states.putString("shape", "north_south");
                    case 1 -> block_states.putString("shape", "east_west");
                    case 2 -> block_states.putString("shape", "ascending_east");
                    case 3 -> block_states.putString("shape", "ascending_west");
                    case 4 -> block_states.putString("shape", "ascending_north");
                    case 5 -> block_states.putString("shape", "ascending_south");
                    case 6 -> block_states.putString("shape", "south_east");
                    case 7 -> block_states.putString("shape", "south_west");
                    case 8 -> block_states.putString("shape", "north_west");
                    case 9 -> block_states.putString("shape", "north_east");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Activator Rail, Detector Rail and Powered Rail
            case 27, 28, (byte) 157 -> {

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

                //shape
                switch (data) {

                    case 0, 8 -> block_states.putString("shape", "north_south");
                    case 1, 9 -> block_states.putString("shape", "east_west");
                    case 2, 10 -> block_states.putString("shape", "ascending_east");
                    case 3, 11 -> block_states.putString("shape", "ascending_west");
                    case 4, 12 -> block_states.putString("shape", "ascending_north");
                    case 5, 13 -> block_states.putString("shape", "ascending_south");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Redstone Comparator
            case (byte) 149, (byte) 150 -> {

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "north");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "east");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "south");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "west");

                }

                //mode
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("mode", "subtract");
                    default -> block_states.putString("mode", "compare");

                }

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }
            }

            //Redstone Dust
            case 55 -> {

                block_states.putString("east", "none");
                block_states.putString("north", "none");
                block_states.putString("power", "0");
                block_states.putString("south", "none");
                block_states.putString("west", "none");

            }

            //Redstone Lamp and Ore
            case 123, 73 -> block_states.putString("lit", "false");
            case 124, 74 -> block_states.putString("lit", "true");

            //Redstone Repeater
            case 93, 94 -> {

                //delay
                switch (data) {

                    case 0, 1, 2, 3 -> block_states.putString("delay", "1");
                    case 4, 5, 6, 7 -> block_states.putString("delay", "2");
                    case 8, 9, 10, 11 -> block_states.putString("delay", "3");
                    case 12, 13, 14, 15 -> block_states.putString("delay", "4");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }
            }

            //Redstone Torch
            case 75 -> {

                //lit
                block_states.putString("lit", "false");

                //facing
                switch (data) {

                    case 1 -> block_states.putString("facing", "east");
                    case 2 -> block_states.putString("facing", "west");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "north");

                }
            }

            case 76 -> {

                //lit
                block_states.putString("lit", "true");

                //facing
                switch (data) {

                    case 1 -> block_states.putString("facing", "west");
                    case 2 -> block_states.putString("facing", "east");
                    case 3 -> block_states.putString("facing", "north");
                    case 4 -> block_states.putString("facing", "south");

                }
            }

            //Saplings
            case 6 -> block_states.putString("stage", "0");

            //Shulker Boxes
            case (byte) 219, (byte) 220, (byte) 221, (byte) 222, (byte) 223, (byte) 224, (byte) 225, (byte) 226, (byte) 227, (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234 -> {
                switch (data) {

                    case 0 -> block_states.putString("facing", "down");
                    case 1 -> block_states.putString("facing", "up");
                    case 2 -> block_states.putString("facing", "north");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "west");
                    case 5 -> block_states.putString("facing", "east");

                }
            }

            //Sign
            case 63 -> {

                //rotation
                block_states.putString("rotation", String.valueOf(data));

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            case 68 -> {

                //facing
                switch (data) {

                    case 2 -> block_states.putString("facing", "north");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "west");
                    case 5 -> block_states.putString("facing", "east");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Slabs
            case 44, 126, (byte) 182, (byte) 205 -> {

                //type
                if (data < 8) {
                    block_states.putString("type", "bottom");
                } else {
                    block_states.putString("type", "top");
                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            case 43, 125, (byte) 181, (byte) 204 -> {

                //type
                if (data < 8) {
                    block_states.putString("type", "double");
                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Snow
            case 78 -> block_states.putString("layer", String.valueOf((data + 1)));

            //Stairs
            case 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136,
                    (byte) 156, (byte) 163, (byte) 164, (byte) 180, (byte) 203 -> {

                //facing
                switch (data) {

                    case 0, 4 -> block_states.putString("facing", "east");
                    case 1, 5 -> block_states.putString("facing", "west");
                    case 2, 6 -> block_states.putString("facing", "south");
                    case 3, 7 -> block_states.putString("facing", "north");

                }

                //half
                if (data >= 4) {
                    block_states.putString("half", "top");
                } else {
                    block_states.putString("half", "bottom");
                }

                //shape
                block_states.putString("shape", "straight");

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Structure Block
            case (byte) 255 -> block_states.putString("mode", "data");

            //TNT
            case 46 -> block_states.putString("unstable", "false");

            //Torch
            case 50 -> {
                switch (data) {

                    case 1 -> block_states.putString("facing", "east");
                    case 2 -> block_states.putString("facing", "west");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "north");

                }
            }

            //Trapdoors
            case 96, (byte) 167 -> {

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "north");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "south");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "west");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //half
                if (data >= 8) {
                    block_states.putString("half", "top");
                } else {
                    block_states.putString("half", "bottom");
                }

                //open
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("open", "true");
                    default -> block_states.putString("open", "false");

                }

                //powered
                block_states.putString("powered", "false");

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Tripwire
            case (byte) 132 -> {

                //attached
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("attached", "true");
                    default -> block_states.putString("attached", "false");

                }

                //disarmed
                if (data >= 8) {
                    block_states.putString("disarmed", "true");
                } else {
                    block_states.putString("disarmed", "false");
                }

                //east
                block_states.putString("east", "false");

                //north
                block_states.putString("north", "false");

                //powered
                if (data % 2 == 1) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

                //south
                block_states.putString("south", "false");

                //west
                block_states.putString("west", "false");

            }

            //Tripwire Hook
            case (byte) 131 -> {

                //attached
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("attached", "true");
                    default -> block_states.putString("attached", "false");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

            }

            //Vines
            case 106 -> {

                //east
                if ((data & (byte) 8) == (byte) 8) {
                    block_states.putString("east", "true");
                } else {
                    block_states.putString("east", "false");
                }

                //north
                if ((data & (byte) 4) == (byte) 4) {
                    block_states.putString("north", "true");
                } else {
                    block_states.putString("north", "false");
                }

                //south
                if (data % 2 == 1) {
                    block_states.putString("south", "true");
                } else {
                    block_states.putString("false", "true");
                }

                //up
                block_states.putString("up", "false");

                //west
                if ((data & (byte) 2) == (byte) 2) {
                    block_states.putString("west", "true");
                } else {
                    block_states.putString("west", "false");
                }
            }

            //Walls
            case (byte) 139 -> {

                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("up", "false");
                block_states.putString("waterlogged", "false");
                block_states.putString("west", "false");

            }

            //Water
            case 8, 9 -> block_states.putString("level", String.valueOf(data));

        }


        return block_states;
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

                    default -> {
                        return "oak_sapling";
                    }
                }
            }

            //Bedrock
            case 7 -> {
                return "bedrock";
            }

            //Water
            case 8, 9 -> {
                return "water";
            }

            //Lava
            case 10, 11 -> {
                return "lava";
            }

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

            //Log and Wood
            case 17 -> {
                switch (data) {

                    case 0, 4, 8 -> {
                        return "oak_log";
                    }

                    case 1, 5, 9 -> {
                        return "spruce_log";
                    }

                    case 2, 6, 10 -> {
                        return "birch_log";
                    }

                    case 3, 7, 11 -> {
                        return "jungle_log";
                    }

                    //Wood 6 sided
                    case 12 -> {
                        return "oak_wood";
                    }

                    case 13 -> {
                        return "spruce_wood";

                    }

                    case 14 -> {
                        return "birch_wood";
                    }

                    case 15 -> {
                        return "jungle_wood";
                    }
                }
            }

            //Leaves
            case 18 -> {
                switch (data) {

                    case 0, 4, 8, 12 -> {
                        return "oak_leaves";
                    }

                    case 1, 5, 9, 13 -> {
                        return "spruce_leaves";
                    }

                    case 2, 6, 10, 14 -> {
                        return "birch_leaves";
                    }

                    case 3, 7, 11, 15 -> {
                        return "jungle_leaves";
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

            //Colour of the bed is stored as a block entity, this will be fixed in post-processing.
            //Bed
            case 26 -> {
                return "red_bed";
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

            //Piston Head
            case 34 -> {
                return "piston_head";
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

            //Double Stone Slab
            case 43 -> {
                switch (data) {

                    case 0 -> {
                        return "smooth_stone_slab";
                    }

                    case 1 -> {
                        return "sandstone_slab";
                    }

                    case 2 -> {
                        return "oak_slab";
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

                    case 8 -> {
                        return "smooth_stone";
                    }

                    case 9 -> {
                        return "smooth_sandstone";
                    }
                }
            }

            //Stone Slab
            case 44 -> {
                switch (data) {

                    case 0, 8 -> {
                        return "smooth_stone_slab";
                    }

                    case 1, 9 -> {
                        return "sandstone_slab";
                    }

                    case 2, 10 -> {
                        return "oak_slab";
                    }

                    case 3, 11 -> {
                        return "cobblestone_slab";
                    }

                    case 4, 12 -> {
                        return "brick_slab";
                    }

                    case 5, 13 -> {
                        return "stone_brick_slab";
                    }

                    case 6, 14 -> {
                        return "nether_brick_slab";
                    }

                    case 7, 15 -> {
                        return "quartz_slab";
                    }
                }
            }

            //Brick Block
            case 45 -> {
                return "bricks";
            }

            //Tnt
            case 46 -> {
                return "tnt";
            }

            //Bookshelf
            case 47 -> {
                return "bookshelf";
            }

            //Moss Stone
            case 48 -> {
                return "mossy_cobblestone";
            }

            //Obsidian
            case 49 -> {
                return "obsidian";
            }

            //Torch
            case 50 -> {
                switch (data) {

                    case 1, 2, 3, 4 -> {
                        return "wall_torch";
                    }

                    default -> {
                        return "torch";
                    }
                }
            }

            //Fire
            case 51 -> {
                return "fire";
            }

            //Mob Spawner
            case 52 -> {
                return "spawner";
            }

            //Oak Stairs
            case 53 -> {
                return "oak_stairs";
            }

            //Chest
            case 54 -> {
                return "chest";
            }

            //Redstone Wire
            case 55 -> {
                return "redstone_wire";
            }

            //Diamond Ore
            case 56 -> {
                return "diamond_ore";
            }

            //Diamond Block
            case 57 -> {
                return "diamond_block";
            }

            //Crafting Table
            case 58 -> {
                return "crafting_table";
            }

            //Wheat Crops
            case 59 -> {
                return "wheat";
            }

            //Farmland
            case 60 -> {
                return "farmland";
            }

            //(Burning) Furnace
            case 61, 62 -> {
                return "furnace";
            }

            //Standing Sign Block
            case 63 -> {
                return "oak_sign";
            }

            //Oak Door Block
            case 64 -> {
                return "oak_door";
            }

            //Ladder
            case 65 -> {
                return "ladder";
            }

            //Rail
            case 66 -> {
                return "rail";
            }

            //Cobblestone Stairs
            case 67 -> {
                return "cobblestone_stairs";
            }

            //Wall Mounted Sign Block
            case 68 -> {
                return "oak_wall_sign";
            }

            //Lever
            case 69 -> {
                return "lever";
            }

            //Stone Pressure Plate
            case 70 -> {
                return "stone_pressure_plate";
            }

            //Iron Door Block
            case 71 -> {
                return "iron_door";
            }

            //Wooden Pressure Plate
            case 72 -> {
                return "oak_pressure_plate";
            }

            //Redstone Ore
            case 73, 74 -> {
                return "redstone_ore";
            }

            //Redstone Torch
            case 75, 76 -> {
                switch (data) {

                    case 1, 2, 3, 4 -> {
                        return "redstone_wall_torch";
                    }

                    default -> {
                        return "redstone_torch";
                    }
                }
            }

            //Stone Button
            case 77 -> {
                return "stone_button";
            }

            //Snow
            case 78 -> {
                return "snow";
            }

            //Ice
            case 79 -> {
                return "ice";
            }

            //Snow Block
            case 80 -> {
                return "snow_block";
            }

            //Cactus
            case 81 -> {
                return "cactus";
            }

            //Clay
            case 82 -> {
                return "clay";
            }

            //Sugar Canes
            case 83 -> {
                return "sugar_cane";
            }

            //Jukebox
            case 84 -> {
                return "jukebox";
            }

            //Oak Fence
            case 85 -> {
                return "oak_fence";
            }

            //Pumpkin
            case 86 -> {
                return "carved_pumpkin";
            }

            //Netherrack
            case 87 -> {
                return "netherrack";
            }

            //Soul Sand
            case 88 -> {
                return "soul_sand";
            }

            //Glowstone
            case 89 -> {
                return "glowstone";
            }

            //Nether Portal
            case 90 -> {
                return "nether_portal";
            }

            //Jack o'Lantern
            case 91 -> {
                return "jack_o_lantern";
            }

            //Cake Block
            case 92 -> {
                return "cake";
            }

            //Redstone Repeater
            case 93, 94 -> {
                return "repeater";
            }

            //Stained Glass
            case 95 -> {

                switch (data) {

                    case 0 -> {
                        return "white_stained_glass";
                    }

                    case 1 -> {
                        return "orange_stained_glass";
                    }

                    case 2 -> {
                        return "magenta_stained_glass";
                    }

                    case 3 -> {
                        return "light_blue_stained_glass";
                    }

                    case 4 -> {
                        return "yellow_stained_glass";
                    }

                    case 5 -> {
                        return "lime_stained_glass";
                    }

                    case 6 -> {
                        return "pink_stained_glass";
                    }

                    case 7 -> {
                        return "gray_stained_glass";
                    }

                    case 8 -> {
                        return "light_gray_stained_glass";
                    }

                    case 9 -> {
                        return "cyan_stained_glass";
                    }

                    case 10 -> {
                        return "purple_stained_glass";
                    }

                    case 11 -> {
                        return "blue_stained_glass";
                    }

                    case 12 -> {
                        return "brown_stained_glass";
                    }

                    case 13 -> {
                        return "green_stain_glass";
                    }

                    case 14 -> {
                        return "red_stained_glass";
                    }

                    case 15 -> {
                        return "black_stained_glass";
                    }
                }
            }

            //Wooden Trapdoor
            case 96 -> {
                return "oak_trapdoor";
            }

            //Monster Egg
            case 97 -> {

                switch (data) {

                    case 0 -> {
                        return "infested_stone";
                    }

                    case 1 -> {
                        return "infested_cobblestone";
                    }

                    case 2 -> {
                        return "infested_stone_bricks";
                    }

                    case 3 -> {
                        return "infested_mossy_stone_bricks";
                    }

                    case 4 -> {
                        return "infested_cracked_stone_bricks";
                    }

                    case 5 -> {
                        return "infested_chiseled_stone_bricks";
                    }
                }
            }

            //Stone Brick
            case 98 -> {
                switch (data) {

                    case 0 -> {
                        return "stone_bricks";
                    }

                    case 1 -> {
                        return "mossy_stone_bricks";
                    }

                    case 2 -> {
                        return "cracked_stone_bricks";
                    }

                    case 3 -> {
                        return "chiseled_stone_bricks";
                    }
                }
            }

            //Brown Mushroom Block
            case 99 -> {
                switch (data) {

                    case 10, 15 -> {
                        return "mushroom_stem";
                    }

                    default -> {
                        return "brown_mushroom_block";
                    }
                }
            }

            //Red Mushroom Block
            case 100 -> {
                switch (data) {

                    case 10, 15 -> {
                        return "mushroom_stem";
                    }

                    default -> {
                        return "red_mushroom_block";
                    }
                }
            }

            //Iron Bars
            case 101 -> {
                return "iron_bars";
            }

            //Glass Pane
            case 102 -> {
                return "glass_pane";
            }

            //Melon Block
            case 103 -> {
                return "melon";
            }

            //Pumpkin Stem
            case 104 -> {
                return "pumpkin_stem";
            }

            //Melon Stem
            case 105 -> {
                return "melon_stem";
            }

            //Vines
            case 106 -> {
                return "vine";
            }

            //Oak Fence Gate
            case 107 -> {
                return "oak_fence_gate";
            }

            //Brick Stairs
            case 108 -> {
                return "brick_stairs";
            }

            //Stone Brick Stairs
            case 109 -> {
                return "stone_brick_stairs";
            }

            //Mycelium
            case 110 -> {
                return "mycelium";
            }

            //Lily Pad
            case 111 -> {
                return "lily_pad";
            }

            //Nether Brick
            case 112 -> {
                return "nether_bricks";
            }

            //Nether Brick Fence
            case 113 -> {
                return "nether_brick_fence";
            }

            //Nether Brick Stairs
            case 114 -> {
                return "nether_brick_stairs";
            }

            //Nether Wart
            case 115 -> {
                return "nether_wart";
            }

            //Enchantment Table
            case 116 -> {
                return "enchanting_table";
            }

            //Brewing Stand
            case 117 -> {
                return "brewing_stand";
            }

            //Cauldron
            case 118 -> {
                return "cauldron";
            }

            //End Portal
            case 119 -> {
                return "end_portal";
            }

            //End Portal Frame
            case 120 -> {
                return "end_portal_frame";
            }

            //End Stone
            case 121 -> {
                return "end_stone";
            }

            //Dragon Egg
            case 122 -> {
                return "dragon_egg";
            }

            //Redstone Lamp
            case 123, 124 -> {
                return "redstone_lamp";
            }

            //Double Wood Slabs
            case 125 -> {
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

            //Wooden Slab
            case 126 -> {
                switch (data) {

                    case 0, 8 -> {
                        return "oak_slab";
                    }

                    case 1, 9 -> {
                        return "spruce_slab";
                    }

                    case 2, 10 -> {
                        return "birch_slab";
                    }

                    case 3, 11 -> {
                        return "jungle_slab";
                    }

                    case 4, 12 -> {
                        return "acacia_slab";
                    }

                    case 5, 13 -> {
                        return "dark_oak_slab";
                    }
                }
            }

            //Cocoa
            case 127 -> {
                return "cocoa";
            }

            //Sandstone Stairs
            case (byte) 128 -> {
                return "sandstone_stairs";
            }

            //Emerald Ore
            case (byte) 129 -> {
                return "emerald_ore";
            }

            //Ender Chest
            case (byte) 130 -> {
                return "ender_chest";
            }

            //Tripwire Hook
            case (byte) 131 -> {
                return "tripwire_hook";
            }

            //Tripwire
            case (byte) 132 -> {
                return "tripwire";
            }

            //Emerald Block
            case (byte) 133 -> {
                return "emerald_block";
            }

            //Spruce Wood Stairs
            case (byte) 134 -> {
                return "spruce_stairs";
            }

            //Birch Wood Stairs
            case (byte) 135 -> {
                return "birch_stairs";
            }

            //Jungle Wood Stairs
            case (byte) 136 -> {
                return "jungle_stairs";
            }

            //Command Block
            case (byte) 137 -> {
                return "command_block";
            }

            //Beacon
            case (byte) 138 -> {
                return "beacon";
            }

            //Cobblestone Wall
            case (byte) 139 -> {
                switch (data) {

                    case 0 -> {
                        return "cobblestone_wall";
                    }

                    case 1 -> {
                        return "mossy_cobblestone_wall";
                    }
                }
            }

            //Flower Pot
            case (byte) 140 -> {
                return "flower_pot";
            }

            //Carrots
            case (byte) 141 -> {
                return "carrots";
            }

            //Potatoes
            case (byte) 142 -> {
                return "potatoes";
            }

            //Wooden Button
            case (byte) 143 -> {
                return "oak_button";
            }

            //Mob Head
            case (byte) 144 -> {
                return "skeleton_skull";
            }

            //Anvil
            case (byte) 145 -> {
                switch (data) {

                    case 0, 1, 2, 3 -> {
                        return "anvil";
                    }

                    case 4, 5, 6, 7 -> {
                        return "chipped_anvil";
                    }

                    case 8, 9, 10, 11 -> {
                        return "damaged_anvil";
                    }
                }
            }

            //Trapped Chest
            case (byte) 146 -> {
                return "trapped_chest";
            }

            //Weighted Pressure Plate (light)
            case (byte) 147 -> {
                return "light_weighted_pressure_plate";
            }

            //Weighted Pressure Plate (heavy)
            case (byte) 148 -> {
                return "heavy_weighted_pressure_plate";
            }

            //Redstone Comporator
            case (byte) 149, (byte) 150 -> {
                return "comparator";
            }

            //Daylight Sensor
            case (byte) 151, (byte) 178 -> {
                return "daylight_detector";
            }

            //Redstone Block
            case (byte) 152 -> {
                return "redstone_block";
            }

            //Nether Quartz Ore
            case (byte) 153 -> {
                return "nether_quartz_ore";
            }

            //Hopper
            case (byte) 154 -> {
                return "hopper";
            }

            //Quartz Block
            case (byte) 155 -> {
                switch (data) {

                    case 0 -> {
                        return "quartz_block";
                    }

                    case 1 -> {
                        return "chiseled_quartz_block";
                    }

                    case 2,3,4 -> {
                        return "quartz_pillar";
                    }
                }
            }

            //Quartz Stairs
            case (byte) 156 -> {
                return "quartz_stairs";
            }

            //Activitor Rail
            case (byte) 157 -> {
                return "activator_rail";
            }

            //Dropper
            case (byte) 158 -> {
                return "dropper";
            }

            //Hardened Clay
            case (byte) 159 -> {
                switch (data) {

                    case 0 -> {
                        return "white_terracotta";
                    }

                    case 1 -> {
                        return "orange_terracotta";
                    }

                    case 2 -> {
                        return "magenta_terracotta";
                    }

                    case 3 -> {
                        return "light_blue_terracotta";
                    }

                    case 4 -> {
                        return "yellow_terracotta";
                    }

                    case 5 -> {
                        return "lime_terracotta";
                    }

                    case 6 -> {
                        return "pink_terracotta";
                    }

                    case 7 -> {
                        return "gray_terracotta";
                    }

                    case 8 -> {
                        return "light_gray_terracotta";
                    }

                    case 9 -> {
                        return "cyan_terracotta";
                    }

                    case 10 -> {
                        return "purple_terracotta";
                    }

                    case 11 -> {
                        return "blue_terracotta";
                    }

                    case 12 -> {
                        return "brown_terracotta";
                    }

                    case 13 -> {
                        return "green_terracotta";
                    }

                    case 14 -> {
                        return "red_terracotta";
                    }

                    case 15 -> {
                        return "black_terracotta";
                    }
                }
            }

            //Stained Glass Panes
            case (byte) 160 -> {
                switch (data) {

                    case 0 -> {
                        return "white_stained_glass_pane";
                    }

                    case 1 -> {
                        return "orange_stained_glass_pane";
                    }

                    case 2 -> {
                        return "magenta_stained_glass_pane";
                    }

                    case 3 -> {
                        return "light_blue_stained_glass_pane";
                    }

                    case 4 -> {
                        return "yellow_stained_glass_pane";
                    }

                    case 5 -> {
                        return "lime_stained_glass_pane";
                    }

                    case 6 -> {
                        return "pink_stained_glass_pane";
                    }

                    case 7 -> {
                        return "gray_stained_glass_pane";
                    }

                    case 8 -> {
                        return "light_gray_stained_glass_pane";
                    }

                    case 9 -> {
                        return "cyan_stained_glass_pane";
                    }

                    case 10 -> {
                        return "purple_stained_glass_pane";
                    }

                    case 11 -> {
                        return "blue_stained_glass_pane";
                    }

                    case 12 -> {
                        return "brown_stained_glass_pane";
                    }

                    case 13 -> {
                        return "green_stained_glass_pane";
                    }

                    case 14 -> {
                        return "red_stained_glass_pane";
                    }

                    case 15 -> {
                        return "black_stained_glass_pane";
                    }
                }
            }

            //Leaves 2
            case (byte) 161 -> {
                switch (data) {

                    case 0, 4, 8, 12 -> {
                        return "acacia_leaves";
                    }

                    case 1, 5, 9, 13 -> {
                        return "dark_oak_leaves";
                    }
                }
            }

            //Log 2
            case (byte) 162 -> {
                switch (data) {

                    case 0, 4, 8 -> {
                        return "acacia_log";
                    }

                    case 1, 5, 9 -> {
                        return "dark_oak_log";
                    }

                    case 12 -> {
                        return "acacia_wood";
                    }

                    case 13 -> {
                        return "dark_oak_wood";
                    }
                }
            }

            //Acacia Wood Stairs
            case (byte) 163 -> {
                return "acacia_stairs";
            }

            //Dark Oak Wood Stairs
            case (byte) 164 -> {
                return "dark_oak_stairs";
            }

            //Slime Block
            case (byte) 165 -> {
                return "slime_block";
            }

            //Barrier
            case (byte) 166 -> {
                return "barrier";
            }

            //Iron Trapdoor
            case (byte) 167 -> {
                return "iron_trapdoor";
            }

            //Prismarine
            case (byte) 168 -> {
                switch (data) {

                    case 0 -> {
                        return "prismarine";
                    }

                    case 1 -> {
                        return "prismarine_bricks";
                    }

                    case 2 -> {
                        return "dark_prismarine";
                    }
                }
            }

            //Sea Lantern
            case (byte) 169 -> {
                return "sea_lantern";
            }

            //Hay Bale
            case (byte) 170 -> {
                return "hay_block";
            }

            //Carpet
            case (byte) 171 -> {
                switch (data) {

                    case 0 -> {
                        return "white_carpet";
                    }

                    case 1 -> {
                        return "orange_carpet";
                    }

                    case 2 -> {
                        return "magenta_carpet";
                    }

                    case 3 -> {
                        return "light_blue_carpet";
                    }

                    case 4 -> {
                        return "yellow_carpet";
                    }

                    case 5 -> {
                        return "lime_carpet";
                    }

                    case 6 -> {
                        return "pink_carpet";
                    }

                    case 7 -> {
                        return "gray_carpet";
                    }

                    case 8 -> {
                        return "light_gray_carpet";
                    }

                    case 9 -> {
                        return "cyan_carpet";
                    }

                    case 10 -> {
                        return "purple_carpet";
                    }

                    case 11 -> {
                        return "blue_carpet";
                    }

                    case 12 -> {
                        return "brown_carpet";
                    }

                    case 13 -> {
                        return "green_carpet";
                    }

                    case 14 -> {
                        return "red_carpet";
                    }

                    case 15 -> {
                        return "black_carpet";
                    }
                }
            }

            //Hardened Clay
            case (byte) 172 -> {
                return "terracotta";
            }

            //Block of Coal
            case (byte) 173 -> {
                return "coal_block";
            }

            //Packed Ice
            case (byte) 174 -> {
                return "packed_ice";
            }

            //Double Plant
            //Top half is converted to the default (sunflower top), this will  be edited in post-processing
            case (byte) 175 -> {
                switch (data) {

                    case 1 -> {
                        return "lilac";
                    }

                    case 2 -> {
                        return "tall_grass";
                    }

                    case 3 -> {
                        return "large_fern";
                    }

                    case 4 -> {
                        return "rose_bush";
                    }

                    case 5 -> {
                        return "peony";
                    }

                    default -> {
                        return "sunflower";
                    }
                }
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Free-standing Banner
            case (byte) 176 -> {
                return "white_banner";
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Wall-mounted Banner
            case (byte) 177 -> {
                return "white_wall_banner";
            }

            //Red Sandstone
            case (byte) 179 -> {
                switch (data) {

                    case 0 -> {
                        return "red_sandstone";
                    }

                    case 1 -> {
                        return "chiseled_red_sandstone";
                    }

                    case 2 -> {
                        return "cut_red_sandstone";
                    }
                }
            }

            //Red Sandstone Stais
            case (byte) 180 -> {
                return "red_sandstone_stairs";
            }

            //Red Sandstone Slab
            case (byte) 181, (byte) 182 -> {

                if (id == (byte) 181 && data >= 8) {
                    return "smooth_red_sandstone";
                } else {
                    return "red_sandstone_slab";
                }
            }

            //Spruce Fence Gate
            case (byte) 183 -> {
                return "spruce_fence_gate";
            }

            //Birch Fence Gate
            case (byte) 184 -> {
                return "birch_fence_gate";
            }

            //Jungle Fence Gate
            case (byte) 185 -> {
                return "jungle_fence_gate";
            }

            //Dark Oak Fence Gate
            case (byte) 186 -> {
                return "dark_oak_fence_gate";
            }

            //Acacia Fence Gate
            case (byte) 187 -> {
                return "acacia_fence_gate";
            }

            //Spruce Fence
            case (byte) 188 -> {
                return "spruce_fence";
            }

            //Birch Fence
            case (byte) 189 -> {
                return "birch_fence";
            }

            //Jungle Fence
            case (byte) 190 -> {
                return "jungle_fence";
            }

            //Dark Oak Fence
            case (byte) 191 -> {
                return "dark_oak_fence";
            }

            //Acacia Fence
            case (byte) 192 -> {
                return "acacia_fence";
            }

            //Spruce Door
            case (byte) 193 -> {
                return "spruce_door";
            }

            //Birch Door
            case (byte) 194 -> {
                return "birch_door";
            }

            //Jungle Door
            case (byte) 195 -> {
                return "jungle_door";
            }

            //Acacia Door
            case (byte) 196 -> {
                return "acacia_door";
            }

            //Dark Oak Door
            case (byte) 197 -> {
                return "dark_oak_door";
            }

            //End Rod
            case (byte) 198 -> {
                return "end_rod";
            }

            //Chorus Plant
            case (byte) 199 -> {
                return "chorus_plant";
            }

            //Chorus Flower
            case (byte) 200 -> {
                return "chorus_flower";
            }

            //Purpur Block
            case (byte) 201 -> {
                return "purpur_block";
            }

            //Purpur Pillar
            case (byte) 202 -> {
                return "purpur_pillar";
            }

            //Purpur Stairs
            case (byte) 203 -> {
                return "purpur_stairs";
            }

            //Purpur Slab
            case (byte) 204, (byte) 205 -> {
                return "purpur_slab";
            }

            //End Stone Bricks
            case (byte) 206 -> {
                return "end_stone_bricks";
            }

            //Beetroot Block
            case (byte) 207 -> {
                return "beetroots";
            }

            //Grass Path
            case (byte) 208 -> {
                return "dirt_path";
            }

            //End Gateway
            case (byte) 209 -> {
                return "end_gateway";
            }

            //Repeating Command Block
            case (byte) 210 -> {
                return "repeating_command_block";
            }

            //Chain Command Block
            case (byte) 211 -> {
                return "chain_command_block";
            }

            //Frosted Ice
            case (byte) 212 -> {
                return "frosted_ice";
            }

            //Magma Block
            case (byte) 213 -> {
                return "magma_block";
            }

            //Nether Wart Block
            case (byte) 214 -> {
                return "nether_wart_block";
            }

            //Red Nether Brick
            case (byte) 215 -> {
                return "red_nether_bricks";
            }

            //Bone Block
            case (byte) 216 -> {
                return "bone_block";
            }

            //Structure Void
            case (byte) 217 -> {
                return "structure_void";
            }

            //Observer
            case (byte) 218 -> {
                return "observer";
            }

            //White Shulker Box
            case (byte) 219 -> {
                return "white_shulker_box";
            }

            //Orange Shulker Box
            case (byte) 220 -> {
                return "orange_shulker_box";
            }

            //Magenta Shulker Box
            case (byte) 221 -> {
                return "magenta_shulker_box";
            }

            //Light Blue Shulker Box
            case (byte) 222 -> {
                return "light_blue_shulker_box";
            }

            //Yellow Shulker Box
            case (byte) 223 -> {
                return "yellow_shulker_box";
            }

            //Lime Shulker Box
            case (byte) 224 -> {
                return "lime_shulker_box";
            }

            //Pink Shulker Box
            case (byte) 225 -> {
                return "pink_shulker_box";
            }

            //Gray Shulker Box
            case (byte) 226 -> {
                return "gray_shulker_box";
            }

            //Light Gray Shulker Box
            case (byte) 227 -> {
                return "light_gray_shulker_box";
            }

            //Cyan Shulker Box
            case (byte) 228 -> {
                return "cyan_shulker_box";
            }

            //Default Shulker Box
            case (byte) 229 -> {
                return "shulker_box";
            }

            //Blue Shulker Box
            case (byte) 230 -> {
                return "blue_shulker_box";
            }

            //Brown Shulker Box
            case (byte) 231 -> {
                return "brown_shulker_box";
            }

            //Green Shulker Box
            case (byte) 232 -> {
                return "green_shulker_box";
            }

            //Red Shulker Box
            case (byte) 233 -> {
                return "red_shulker_box";
            }

            //Black Shulker Box
            case (byte) 234 -> {
                return "black_shulker_box";
            }

            //White Glazed Terracotta
            case (byte) 235 -> {
                return "white_glazed_terracotta";
            }

            //Orange Glazed Terracotta
            case (byte) 236 -> {
                return "orange_glazed_terracotta";
            }

            //Magenta Glazed Terracotta
            case (byte) 237 -> {
                return "magenta_glazed_terracotta";
            }

            //Light Blue Glazed Terracotta
            case (byte) 238 -> {
                return "light_blue_glazed_terracotta";
            }

            //Yellow Glazed Terracotta
            case (byte) 239 -> {
                return "yellow_glazed_terracotta";
            }

            //Lime Glazed Terracotta
            case (byte) 240 -> {
                return "lime_glazed_terracotta";
            }

            //Pink Glazed Terracotta
            case (byte) 241 -> {
                return "pink_glazed_terracotta";
            }

            //Gray Glazed Terracotta
            case (byte) 242 -> {
                return "gray_glazed_terracotta";
            }

            //Light Gray Glazed Terracotta
            case (byte) 243 -> {
                return "light_gray_glazed_terracotta";
            }

            //Cyan Glazed Terracotta
            case (byte) 244 -> {
                return "cyan_glazed_terracotta";
            }

            //Purple Glazed Terracotta
            case (byte) 245 -> {
                return "purple_glazed_terracotta";
            }

            //Blue Glazed Terracotta
            case (byte) 246 -> {
                return "blue_glazed_terracotta";
            }

            //Brown Glazed Terracotta
            case (byte) 247 -> {
                return "brown_glazed_terracotta";
            }

            //Green Glazed Terracotta
            case (byte) 248 -> {
                return "green_glazed_terracotta";
            }

            //Red Glazed Terracotta
            case (byte) 249 -> {
                return "red_glazed_terracotta";
            }

            //Black Glazed Terracotta
            case (byte) 250 -> {
                return "black_glazed_terracotta";
            }

            //Concrete
            case (byte) 251 -> {
                switch (data) {

                    case 0 -> {
                        return "white_concrete";
                    }

                    case 1 -> {
                        return "orange_concrete";
                    }

                    case 2 -> {
                        return "magenta_concrete";
                    }

                    case 3 -> {
                        return "light_blue_concrete";
                    }

                    case 4 -> {
                        return "yellow_concrete";
                    }

                    case 5 -> {
                        return "lime_concrete";
                    }

                    case 6 -> {
                        return "pink_concrete";
                    }

                    case 7 -> {
                        return "gray_concrete";
                    }

                    case 8 -> {
                        return "light_gray_concrete";
                    }

                    case 9 -> {
                        return "cyan_concrete";
                    }

                    case 10 -> {
                        return "purple_concrete";
                    }

                    case 11 -> {
                        return "blue_concrete";
                    }

                    case 12 -> {
                        return "brown_concrete";
                    }

                    case 13 -> {
                        return "green_concrete";
                    }

                    case 14 -> {
                        return "red_concrete";
                    }

                    case 15 -> {
                        return "black_concrete";
                    }
                }
            }

            //Concrete Powder
            case (byte) 252 -> {
                switch (data) {

                    case 0 -> {
                        return "white_concrete_powder";
                    }

                    case 1 -> {
                        return "orange_concrete_powder";
                    }

                    case 2 -> {
                        return "magenta_concrete_powder";
                    }

                    case 3 -> {
                        return "light_blue_concrete_powder";
                    }

                    case 4 -> {
                        return "yellow_concrete_powder";
                    }

                    case 5 -> {
                        return "lime_concrete_powder";
                    }

                    case 6 -> {
                        return "pink_concrete_powder";
                    }

                    case 7 -> {
                        return "gray_concrete_powder";
                    }

                    case 8 -> {
                        return "light_gray_concrete_powder";
                    }

                    case 9 -> {
                        return "cyan_concrete_powder";
                    }

                    case 10 -> {
                        return "purple_concrete_powder";
                    }

                    case 11 -> {
                        return "blue_concrete_powder";
                    }

                    case 12 -> {
                        return "brown_concrete_powder";
                    }

                    case 13 -> {
                        return "green_concrete_powder";
                    }

                    case 14 -> {
                        return "red_concrete_powder";
                    }

                    case 15 -> {
                        return "black_concrete_powder";
                    }
                }
            }

            case (byte) 255 -> {
                return "structure_block";
            }
        }

        return "air";

    }
}
