package me.bteuk.converterplugin.utils.inventory;

import me.bteuk.converterplugin.utils.Utils;
import me.bteuk.converterplugin.utils.items.ItemsHelper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class InventoryHelper {
    public static void prepInventoryChest(InventoryHolder inventoryHolder, JSONObject props){
        if(props.containsKey("items")){
            JSONArray minecartItems = (JSONArray) props.get("items");
            Inventory inventory = inventoryHolder.getInventory();
            ItemsHelper.setItems(inventory, minecartItems);
        }
    }

    public static void prepLootableChest(com. destroystokyo. paper. loottable. LootableEntityInventory lootableEntityInventory, JSONObject props){
        if(props.containsKey("loot_table")){
            String _lootTable = (String) props.get("loot_table");
            LootTable lootTable = Utils.getLootTable(_lootTable);
            if(_lootTable != null)
                if(props.containsKey("loot_table_seed"))
                    lootableEntityInventory.setLootTable(lootTable, (long) props.get("loot_table_seed"));
                else
                    lootableEntityInventory.setLootTable(lootTable);
        }
    }
}
