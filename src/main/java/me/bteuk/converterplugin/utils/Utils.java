package me.bteuk.converterplugin.utils;

import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.util.EulerAngle;
import org.json.simple.JSONObject;

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
}
