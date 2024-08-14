package me.bteuk.converterplugin.utils.entities;

import me.bteuk.converterplugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.MetadataValueAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class MinecartHelper {

    public static void setCommonMinecartProps(Minecart minecart, JSONObject props){
        Utils.prepEntity(minecart, props);

        if(props.containsKey("display_tile") && (int) (long) props.get("display_tile") == 1){
            String displayTileBlock = ((String)props.get("display_tile_block")).substring(10);
            Material displayTileMaterial = Material.getMaterial(displayTileBlock);
            if(props.containsKey("display_tile_block_states")){
                JSONObject displayBlockStates = (JSONObject) props.get("display_tile_block_states");
                String _blockData = "minecraft:" + displayTileBlock + "[" + Utils.flattenBlockState(displayBlockStates) + "]";
                BlockData displayBlockData = displayTileMaterial.createBlockData(_blockData);
                minecart.setDisplayBlockData(displayBlockData);
            }else
                minecart.setDisplayBlockData(displayTileMaterial.createBlockData());

            if(props.containsKey("display_tile_offset"))
                minecart.setDisplayBlockOffset((int)props.get("display_tile_offset"));
        }
    }

    public static void prepChestMinecart(StorageMinecart storageMinecart, JSONObject props){
        prepInventoryChest(storageMinecart, props);
        prepLootableChest(storageMinecart, props);
    }

    public static void prepHopperMinecart(HopperMinecart hopperMinecart, JSONObject props){
        prepInventoryChest(hopperMinecart, props);
        prepLootableChest(hopperMinecart, props);
        if(props.containsKey("enabled"))
            hopperMinecart.setEnabled((int) (long) props.get("enabled") == 1);
    }

    public static void prepFurnaceMinecart(PoweredMinecart furnaceMinecart, JSONObject props){
        if(props.containsKey("fuel"))
            furnaceMinecart.setFuel((int) (long) props.get("fuel"));
        if(props.containsKey("push_x"))
            furnaceMinecart.setPushX((double) props.get("push_x"));
        if(props.containsKey("push_z"))
            furnaceMinecart.setPushZ((double) props.get("push_z"));
    }

    public static void prepCommandMinecart(CommandMinecart commandMinecart, JSONObject props){
        if(props.containsKey("command"))
            commandMinecart.setCommand((String) props.get("command"));
    }

    public static void prepExplosiveMinecart(ExplosiveMinecart explosiveMinecart, JSONObject props){
        if(props.containsKey("tnt_fuse")){
            //ToDo: Once updated to 1.20.4+ Use explosiveMinecart.setFuseTicks((int)props.get("tnt_fuse"))
        }
    }

    public static void prepInventoryChest(InventoryHolder inventoryHolder, JSONObject props){
        if(props.containsKey("minecart_items")){
            JSONArray minecartItems = (JSONArray) props.get("minecart_items");
            //ToDo: Create items stacks from the minecart items
            //Inventory inventory = storageMinecart.getInventory();
            //ItemStack itemStack = new ItemStack(Material.getMaterial())
            //inventory.addItem()
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
