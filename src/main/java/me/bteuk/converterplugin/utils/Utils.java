package me.bteuk.converterplugin.utils;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.util.EulerAngle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Utils {
    public static int floor(double num) {
        int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    public static EulerAngle DegreesToEulerAngles(double x, double y, double z){
        double xRad = Math.toRadians(x);
        double yRad = Math.toRadians(y);
        double zRad = Math.toRadians(z);

        return new EulerAngle(xRad, yRad, zRad);
    }

    public static String flattenBlockState(JSONObject blockStates){
        String flatten = "";
        Set<String> blockStateKeys = blockStates.keySet();
        int c = 0;
        for(String key : blockStateKeys){
            flatten += key + "=" + blockStates.get(key).toString() + (c != blockStates.size() - 1 ? "," : "");
            c++;
        }

        return flatten;
    }

    public static LootTable getLootTable(String lootTable){
        if(lootTable.startsWith("minecraft:chests/"))
            return LootTables.valueOf(lootTable.substring(17).toUpperCase()).getLootTable();

        if(lootTable.startsWith("minecraft:entities"))
            return LootTables.valueOf(lootTable.substring(19).toUpperCase()).getLootTable();

        return null;
    }

    public static void prepEntity(Entity entity, JSONObject properties){
        entity.setGravity((int) (long) properties.getOrDefault("NoGravity", (long)0) == 1);
        if(properties.containsKey("Rotation")){
            JSONArray entityRotationArray = (JSONArray) properties.get("Rotation");
            entity.setRotation( (float) (double)entityRotationArray.get(0), (float) (double)entityRotationArray.get(1));
        }
    }

    public static List<Integer> getIntegerListFromJson(JSONObject properties, String key){
        List<Integer> list = new ArrayList<>();
        JSONArray rawArray = (JSONArray) properties.get(key);
        for(Object item : rawArray)
            list.add((int) (long) item);
        return list;
    }

    public static PatternType getPatternType(String p) {

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

    public static DyeColor getDyeColour(String c) {

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
}
