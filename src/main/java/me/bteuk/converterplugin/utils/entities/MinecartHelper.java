package me.bteuk.converterplugin.utils.entities;

import me.bteuk.converterplugin.utils.Utils;
import me.bteuk.converterplugin.utils.inventory.InventoryHelper;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.*;
import org.json.simple.JSONObject;

public class MinecartHelper {

    public static void setCommonMinecartProps(Minecart minecart, JSONObject props){
        Utils.prepEntity(minecart, props);

        if(props.containsKey("display_tile") && (int) (long) props.get("display_tile") == 1){
            String displayTileBlock = ((String)props.get("display_tile_block")).substring(10);
            Material displayTileMaterial = Material.getMaterial(displayTileBlock.toUpperCase());
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
        InventoryHelper.prepInventoryChest(storageMinecart, props);
        InventoryHelper.prepLootableChest(storageMinecart, props);
    }

    public static void prepHopperMinecart(HopperMinecart hopperMinecart, JSONObject props){
        InventoryHelper.prepInventoryChest(hopperMinecart, props);
        InventoryHelper.prepLootableChest(hopperMinecart, props);
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
}
