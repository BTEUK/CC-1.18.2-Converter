package me.bteuk.converterplugin.utils.items;

import com.destroystokyo.paper.Namespaced;
import me.bteuk.converterplugin.utils.entities.ItemSkullHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ItemsHelper {

    private static Logger logger;

    public static void setLogger(Logger _logger){
        logger = _logger;
    }

    public static ItemStack getItem(String id, JSONObject props) throws Exception{
        ItemStack itemStack = new ItemStack(Material.getMaterial(id.toUpperCase()));


        ItemMeta itemMeta = itemStack.getItemMeta();

        boolean skipDisplayProps = false;

        //Player Head
        if(props.containsKey("SkullOwner")){
            JSONObject skullOwnerObject = (JSONObject) props.get("SkullOwner");

            if (skullOwnerObject.containsKey("profileId")) {
                String rawUUID = (String) skullOwnerObject.get("profileId");
                BigInteger mostBits = new BigInteger(rawUUID.substring(0, 16), 16);
                BigInteger leastBits = new BigInteger(rawUUID.substring(16, 32), 16);
                UUID playerID = new UUID(mostBits.longValue(), leastBits.longValue());
                itemStack = ItemSkullHelper.fromUUID(playerID);
                itemMeta = itemStack.getItemMeta();
            } else if (skullOwnerObject.containsKey("profileName")) {
                String profileName = (String) skullOwnerObject.get("profileName");
                itemStack = ItemSkullHelper.fromUsername(profileName);
                itemMeta = itemStack.getItemMeta();
            } else if (skullOwnerObject.containsKey("texture")) {
                String skullId = (String)skullOwnerObject.getOrDefault("id", "");
                String skullTexture = (String) skullOwnerObject.get("texture");
                itemStack = ItemSkullHelper.fromBase64(skullId , skullTexture);
                itemMeta = itemStack.getItemMeta();
            }

            skipDisplayProps = true;
        }

        //General tags
        if(props.containsKey("GeneralTags")){
            JSONObject generalTags = (JSONObject) props.get("GeneralTags");
            if(generalTags.containsKey("unbreakable"))
                itemMeta.setUnbreakable((int)(long)generalTags.get("unbreakable") == 1);
            if(generalTags.containsKey("CanDestroy")){
                JSONArray canDestroy = (JSONArray) generalTags.get("CanDestroy");
                Collection<Namespaced> canDestroyCol = new JSONArray();
                int index = 0;
                for(int c = 0; c < canDestroy.size(); c++){
                    String _namespaceID = (String) canDestroy.get(c);
                    index = _namespaceID.indexOf(":");
                    canDestroyCol.add(new NamespacedKey(_namespaceID.substring(0, index), _namespaceID.substring(index + 1)));
                }
                if(!canDestroyCol.isEmpty())
                    itemMeta.setDestroyableKeys(canDestroyCol);
            }
        }


        //Potions
        if(props.containsKey("PotionEffects")){
            JSONObject potionEffects = (JSONObject) props.get("PotionEffects");
            String _potion = ((String) potionEffects.get("Potion")).substring(10).toUpperCase();
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(_potion)));
            itemStack.setItemMeta(potionMeta);
        }


        //Fireworks
        if(props.containsKey("Fireworks")){
            JSONObject fireworks = (JSONObject) props.get("Fireworks");
            FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;

            if(fireworks.containsKey("Explosion")){
                JSONObject _explosion = (JSONObject) fireworks.get("Explosion");
                fireworkMeta.addEffect(getFireworksEffect(_explosion));
            }

            if(fireworks.containsKey("Fireworks")){
                JSONObject _fireworks = (JSONObject) fireworks.get("Fireworks");
                if(_fireworks.containsKey("flight")){
                    int _flight = (int) (long) _fireworks.get("flight");
                    _flight = Math.max(0, Math.min(127, _flight));
                    fireworkMeta.setPower(_flight);
                }
                if(_fireworks.containsKey("explosions")){
                    JSONArray _explosions = (JSONArray) _fireworks.get("explosions");
                    List<FireworkEffect> fireworkEffects = new ArrayList<>();
                    for(int c = 0; c < _explosions.size(); c++){
                        JSONObject _firework = (JSONObject) _explosions.get(c);
                        fireworkEffects.add(getFireworksEffect(_firework));
                    }
                    fireworkMeta.addEffects(fireworkEffects);
                }
            }

            itemStack.setItemMeta(fireworkMeta);
        }

        //Display Properties
        if(props.containsKey("DisplayProps") && !skipDisplayProps){
            JSONObject displayProps = (JSONObject) props.get("DisplayProps");
            boolean skipDisplayColor = false;

            switch (id){
                case "leather_boots", "leather_leggings", "leather_chestplate" -> {
                    if (displayProps.containsKey("display_color")) {
                        skipDisplayColor = true;
                        LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemMeta;
                        armorMeta.setColor(org.bukkit.Color.fromRGB((int) (long) displayProps.get("display_color")));
                        itemStack.setItemMeta(armorMeta);
                    }
                }
            }

            if(displayProps.containsKey("display_name")){
                TextComponent component = Component.text((String) displayProps.get("display_name"));
                if(displayProps.containsKey("display_color") && !skipDisplayColor){
                    component.color(TextColor.color((int)(long)displayProps.get("display_color")));
                }
                itemMeta.displayName(component.asComponent());
            }



        }

        //Block Tags
        if(props.containsKey("BlockTags")){
            JSONObject blockTags = (JSONObject) props.get("BlockTags");
            if(blockTags.containsKey("CanPlaceOn")){
                JSONArray canPlaceOn = (JSONArray) blockTags.get("CanPlaceOn");
                Collection<Namespaced> canPlaceOnCol = new JSONArray();
                int index = 0;
                for(int c = 0; c < canPlaceOn.size(); c++){
                    String _namespaceID = (String) canPlaceOn.get(c);
                    index = _namespaceID.indexOf(":");
                    canPlaceOnCol.add(new NamespacedKey(_namespaceID.substring(0, index), _namespaceID.substring(index + 1)));
                }
                if(!canPlaceOnCol.isEmpty())
                    itemMeta.setPlaceableKeys(canPlaceOnCol);
            }
            if(blockTags.containsKey("BlockEntityTag")){
                //ToDo: Go through items that use the BlockEntityTag tag
            }
        }


        //Enchantments
        if(props.containsKey("EnchantmentsTags")){
            JSONObject enchantmentsTags = (JSONObject) props.get("EnchantmentsTags");
            if(enchantmentsTags.containsKey("enchantments")){
                JSONArray enchantments = (JSONArray) enchantmentsTags.get("enchantments");
                for (int c = 0; c < enchantments.size(); c++){
                    JSONObject enchantment = (JSONObject) enchantments.get(c);
                    Enchantment _enchantment = Enchantment.getByKey(new NamespacedKey("minecraft", (String) enchantment.get("id")));
                    itemMeta.addEnchant(_enchantment, (int) (long)enchantment.get("lvl"), true);
                }
            }
            if(enchantmentsTags.containsKey("stored_enchantments")){
                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta)itemMeta;
                JSONArray storedEnchantments = (JSONArray)enchantmentsTags.get("stored_enchantments");
                for(int c = 0; c < storedEnchantments.size(); c++){
                    JSONObject storedEnchantment = (JSONObject) storedEnchantments.get(c);
                    Enchantment _enchantment = Enchantment.getByKey(new NamespacedKey("minecraft", (String) storedEnchantment.get("id")));
                    storageMeta.addStoredEnchant(_enchantment, (int) (long)storedEnchantment.get("lvl"), true);
                }
                itemStack.setItemMeta(storageMeta);
            }
        }


        //Attribute Modifiers
        if(props.containsKey("AttributeModifiers")){
            JSONArray attributeModifiers = (JSONArray) props.get("AttributeModifiers");
            for(int c = 0; c < attributeModifiers.size(); c++){
                JSONObject attributeModifier = (JSONObject) attributeModifiers.get(c);
                org.bukkit.attribute.Attribute _attribute = org.bukkit.attribute.Attribute.valueOf(((String) attributeModifier.get("attribute_name")).toUpperCase());
                String _attributeName = (String)attributeModifier.getOrDefault("name","");
                Double _amount = (Double)attributeModifier.getOrDefault("amount",0.0d);
                org.bukkit.attribute.AttributeModifier _attributeModifier = null;
                AttributeModifier.Operation _operation = AttributeModifier.Operation.valueOf((String) attributeModifier.get("operation"));
                if(attributeModifier.containsKey("uuid_most") && attributeModifier.containsKey("uuid_least")){
                    UUID _uuid = new UUID((long) attributeModifier.get("uuid_most"), (long) attributeModifier.get("uuid_least"));
                    if(attributeModifier.containsKey("slot"))
                        _attributeModifier = new AttributeModifier(_uuid,
                                _attributeName,
                                _amount, _operation,
                                EquipmentSlot.valueOf(((String) attributeModifier.get("slot")).toUpperCase()));
                    else
                        _attributeModifier = new AttributeModifier(_uuid, _attributeName, _amount, _operation);
                }else
                    _attributeModifier = new org.bukkit.attribute.AttributeModifier(_attributeName, _amount, _operation);
                itemMeta.addAttributeModifier(_attribute, _attributeModifier);
            }
        }


        return itemStack;
    }

    public static FireworkEffect getFireworksEffect(JSONObject fireworkProps){
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        byte _type = (byte) (long)fireworkProps.get("type");
        switch (_type){
            case 1 -> type = FireworkEffect.Type.BALL_LARGE;
            case 2 -> type = FireworkEffect.Type.STAR;
            case 3 -> type = FireworkEffect.Type.CREEPER;
            case 4 -> type = FireworkEffect.Type.BURST;
        }

        FireworkEffect.Builder fireworkEffectBuilder = FireworkEffect.builder();
        fireworkEffectBuilder.with(type);

        if(fireworkProps.containsKey("colors")){
            JSONArray _colors = (JSONArray) fireworkProps.get("colors");
            List<Color> cols = getColors(_colors);
            if(!cols.isEmpty())
                fireworkEffectBuilder.withColor(cols);
        }

        if(fireworkProps.containsKey("fade_colors")){
            JSONArray _colors = (JSONArray) fireworkProps.get("fade_colors");
            List<Color> cols = getColors(_colors);
            if(!cols.isEmpty())
                fireworkEffectBuilder.withFade(cols);
        }

        if(fireworkProps.containsKey("flicker") && (int)(long)fireworkProps.get("flicker") == 1)
            fireworkEffectBuilder.withFlicker();

        if(fireworkProps.containsKey("trail") && (int)(long)fireworkProps.get("trail") == 1)
            fireworkEffectBuilder.withTrail();

        return fireworkEffectBuilder.build();
    }

    private static List<Color> getColors(JSONArray colors){
        List<Color> cols = new ArrayList<>();
        for(int c = 0; c < colors.size(); c++){
            int col = (int) (long) colors.get(c);
            cols.add(Color.fromRGB(col));
        }
        return cols;
    }

    public static void setItems(Inventory inventory, JSONArray items){
        for(Object itemRaw : items){
            JSONObject _item = (JSONObject) itemRaw;
            String _id = (String) _item.get("id");
            if(_id.startsWith("minecraft:")) {
                _id = _id.substring(10);
                JSONObject _props = (JSONObject) _item.getOrDefault("Properties", new JSONObject());
                int _slot = (int) (long) _props.get("slot");
                int _count = (int) (long) _props.get("count");

                try {
                    ItemStack itemStack = ItemsHelper.getItem(_id, _props);
                    itemStack.setAmount(_count);
                    inventory.setItem(_slot, itemStack);
                }catch (Exception ex){
                    logger.warning(String.format("Exception while setting item: %1$s | Error: %2$s", _id, ex.getMessage()));
                }

            }
        }
    }
}
