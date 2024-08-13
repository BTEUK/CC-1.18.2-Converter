package me.bteuk.converterplugin.utils.entities;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

public class ItemSkullHelper {

    private static JSONParser textureParser = new JSONParser();

    public static ItemStack fromTexture(String texture){
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);


        return  skullItem;
    }

    public static ItemStack fromUsername(String username){
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(username));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    public static ItemStack fromUUID(UUID uuid){
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    public static ItemStack fromBase64(String id, String base64) throws ParseException, IOException {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
        String rawJson = new String(Base64.getDecoder().decode(base64));
        StringReader reader = new StringReader(rawJson);
        JSONObject playerTexturesJsonObject = (JSONObject) textureParser.parse(reader);

        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();

        JSONObject playerTexturesJson = (JSONObject) playerTexturesJsonObject.getOrDefault("textures", new JSONObject());
        if(!playerTexturesJson.isEmpty()){
            JSONObject playerTextureSkin = (JSONObject) playerTexturesJson.getOrDefault("SKIN", new JSONObject());
            if(!playerTextureSkin.isEmpty()){
                String skinUrl = (String) playerTextureSkin.getOrDefault("url", "");

                if(!skinUrl.isEmpty()) {
                    PlayerProfile customPlayerProfile = Bukkit.createProfile(id.isEmpty() ? UUID.randomUUID() : UUID.fromString(id));
                    //customPlayerProfile.setProperty(new ProfileProperty("textures", base64));
                    PlayerTextures playerTextures = customPlayerProfile.getTextures();

                    String skinModel = "";

                    JSONObject playerTextureMeta = (JSONObject) playerTextureSkin.getOrDefault("metadata", new JSONObject());
                    if (!playerTextureMeta.isEmpty())
                        skinModel = (String) playerTextureMeta.getOrDefault("model", "");


                    if (!skinModel.isEmpty())
                        playerTextures.setSkin(new URL(skinUrl), (skinModel.equals("classic")) ? PlayerTextures.SkinModel.CLASSIC : PlayerTextures.SkinModel.SLIM);
                    else
                        playerTextures.setSkin(new URL(skinUrl));

                    customPlayerProfile.setTextures(playerTextures);
                    customPlayerProfile.complete();

                    skullMeta.setPlayerProfile(customPlayerProfile);
                }
            }
        }

        skullItem.setItemMeta(skullMeta);

        return  skullItem;

    }
}
