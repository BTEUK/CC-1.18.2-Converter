package me.bteuk.converterplugin.utils.items;

import com.destroystokyo.paper.Namespaced;
import me.bteuk.converterplugin.utils.Utils;
import me.bteuk.converterplugin.utils.inventory.InventoryHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Helper class to convert items stored in entities inside post-processing json file to in-game ItemStack items
 */
public class ItemsHelper {

    /**
     * Get the ItemsStack based on the ID of the item, and It's set the properties of the ItemStack based on the JSON object properties of the item
     * @param id The ID of the item, ex, "knowledge_book"
     * @param props The JSON object properties of the itme
     * @return The created ItemStack
     * @throws Exception If an error happened while setting the properties of the ItemStack
     */
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

        //Potions
        if(props.containsKey("PotionEffects")){
            JSONObject potionEffects = (JSONObject) props.get("PotionEffects");
            String _potion = ((String) potionEffects.get("Potion")).substring(10).toUpperCase();
            PotionMeta potionMeta = (PotionMeta) itemMeta;

            String _potionEnum = _potion;
            switch (_potion){
                case "LEAPING" -> _potionEnum = "JUMP";
                case "STRONG_LEAPING" -> _potionEnum = "STRONG_JUMP";
                case "LONG_LEAPING" -> _potionEnum = "LONG_JUMP";
                case "SWIFTNESS" -> _potionEnum = "SPEED";
                case "STRONG_SWIFTNESS" -> _potionEnum = "STRONG_SPEED";
                case "LONG_SWIFTNESS" -> _potionEnum = "LONG_SPEED";
                case "HEALING" -> _potionEnum = "INSTANT_HEAL";
                case "STRONG_HEALING" -> _potionEnum = "STRONG_INSTANT_HEAL";
                case "HARMING" -> _potionEnum = "INSTANT_DAMAGE";
                case "STRONG_HARMING" -> _potionEnum = "STRONG_INSTANT_DAMAGE";
                case "REGENERATION" -> _potionEnum = "REGEN";
                case "STRONG_REGENERATION" -> _potionEnum = "STRONG_REGEN";
                case "LONG_REGENERATION" -> _potionEnum = "LONG_REGEN";

            }

            if(_potionEnum.startsWith("LONG_"))
                potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(_potionEnum.substring(5)), true, false));
            else if( _potionEnum.startsWith("STRONG_"))
                potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(_potionEnum.substring(7)), false, true));
            else
                potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(_potionEnum)));



            if(potionEffects.containsKey("custom_potion_color")){
                int customPotionColor = (int)(long)potionEffects.get("custom_potion_color");
                potionMeta.setColor(Color.fromRGB(customPotionColor));
            }

            if(potionEffects.containsKey("custom_potion_effects")){
                JSONArray customPotionEffectsArray = (JSONArray) potionEffects.get("custom_potion_effects");
                for(int c = 0; c < customPotionEffectsArray.size(); c++){
                    JSONObject customPotionEffectItem = (JSONObject) customPotionEffectsArray.get(c);
                    int amplifier = 1;
                    if(customPotionEffectItem.containsKey("amplifier"))
                        amplifier = (int)(long)customPotionEffectItem.get("amplifier");
                    PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByKey(new NamespacedKey("minecraft", (String) customPotionEffectItem.get("id"))), (int)(long)customPotionEffectItem.get("duration"), amplifier);
                    if(customPotionEffectItem.containsKey("ambient"))
                        potionEffect.withAmbient((int)(long)customPotionEffectItem.get("ambient") == 1);
                    if(customPotionEffectItem.containsKey("show_particles"))
                        potionEffect.withParticles((int)(long)customPotionEffectItem.get("show_particles") == 1);

                    potionMeta.addCustomEffect(potionEffect, true);
                }
            }


            itemStack.setItemMeta(potionMeta);
        }else if((id.equals("writable_book") || id.equals("written_book"))){
            BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
            BookMeta.BookMetaBuilder bookMetaBuilder = bookMeta.toBuilder();


            if(props.containsKey("book_author"))
                bookMetaBuilder.author(Component.text((String) props.get("book_author")));
            if(props.containsKey("book_title"))
                bookMetaBuilder.title(Component.text((String) props.get("book_title")));

            if(props.containsKey("book_pages")){
                JSONArray _bookPages = (JSONArray)props.get("book_pages");
                List<Component> bookPages = new ArrayList<>();
                for(int c = 0; c < _bookPages.size(); c++){
                    bookPages.add(Component.text((String) _bookPages.get(c)));
                }
                bookMetaBuilder.pages(bookPages);
            }

            bookMeta = bookMetaBuilder.build();

            if(props.containsKey("book_generation"))
                bookMeta.setGeneration(BookMeta.Generation.valueOf((String) props.get("book_generation")));

            itemStack.setItemMeta(bookMeta);
            itemMeta = itemStack.getItemMeta();
        }else if(id.equals("knowledge_book") && props.containsKey("book_recipes")){
            JSONArray bookRecipes = (JSONArray) props.get("book_recipes");
            KnowledgeBookMeta knowledgeBookMeta = (KnowledgeBookMeta) itemStack.getItemMeta();
            for(int c = 0; c < bookRecipes.size(); c++){
                String recipe = (String) bookRecipes.get(c);
                knowledgeBookMeta.addRecipe(new NamespacedKey("minecraft", recipe));
            }

            itemStack.setItemMeta(knowledgeBookMeta);
            itemMeta = itemStack.getItemMeta();
        }else if(id.equals("filled_map") && props.containsKey("org_id") && props.containsKey("map_session")){
            int org_id = (int)(long)props.get("org_id");
            String session = (String) props.get("map_session");
            MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
            MapView mapView = ItemMapsHelper.instance.getMapView(org_id, session);
            mapMeta.setMapView(mapView);
            itemStack.setItemMeta(mapMeta);
            itemMeta = itemStack.getItemMeta();
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
                JSONObject blockEntity = (JSONObject) blockTags.get("BlockEntityTag");

                if(id.contains("shulker_box") && blockEntity.containsKey("items")){
                    JSONArray _items = (JSONArray) blockEntity.get("items");

                    BlockStateMeta blockStateMeta = (BlockStateMeta)itemMeta;
                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                    if(blockEntity.containsKey("loot_table")){
                        String _lootTable = (String) blockEntity.get("loot_table");
                        LootTable lootTable = InventoryHelper.getLootTable(_lootTable);
                        if(blockEntity.containsKey("loot_table_seed")){
                            long _lootTableSeed = (long) blockEntity.get("loot_table_seed");
                            shulkerBox.setLootTable(lootTable, _lootTableSeed);
                        }else
                            shulkerBox.setLootTable(lootTable);
                    }

                    Inventory inventory = shulkerBox.getInventory();
                    setItems(inventory, _items);

                    blockStateMeta.setBlockState(shulkerBox);
                    itemStack.setItemMeta(blockStateMeta);
                    itemMeta = itemStack.getItemMeta();
                }else if(id.contains("banner")){
                    BannerMeta bannerMeta = (BannerMeta) itemMeta;
                    JSONArray _patterns = (JSONArray) blockEntity.get("patterns");
                    for(int c = 0; c < _patterns.size(); c++){
                        JSONObject _pattern = (JSONObject) _patterns.get(c);
                        bannerMeta.addPattern(new Pattern(Utils.getDyeColour((String) _pattern.get("colour")),
                                Utils.getPatternType((String) _pattern.get("pattern"))));
                    }

                    itemStack.setItemMeta(bannerMeta);
                    itemMeta = itemStack.getItemMeta();
                }


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

    /**
     * Get the FireWorkEffect based on the JSON object properties
     * @param fireworkProps The JSON object properties
     * @return The created FireworkEffect based on the given properties
     */
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

    /**
     * Get a list of colors based on each color integer value inside the JSON array
     * @param colors JSON array containing integers representing colors
     * @return List of colors
     */
    private static List<Color> getColors(JSONArray colors){
        List<Color> cols = new ArrayList<>();
        for(int c = 0; c < colors.size(); c++){
            int col = (int) (long) colors.get(c);
            cols.add(Color.fromRGB(col));
        }
        return cols;
    }

    /**
     * Set the items in an Inventory from a JSON array containing the items
     * @param inventory Inventory to set the items
     * @param items JSON array containing the info of items
     * @throws Exception If an exception happened while setting the items
     */
    public static void setItems(Inventory inventory, JSONArray items) throws Exception {
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
                    throw new Exception(String.format("Exception while setting item: %1$s | Error: %2$s", _id, ex.getMessage()));
                }
            }
        }
    }
}
