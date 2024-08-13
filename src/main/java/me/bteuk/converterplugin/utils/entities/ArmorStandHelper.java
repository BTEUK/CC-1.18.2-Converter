package me.bteuk.converterplugin.utils.entities;

import me.bteuk.converterplugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

public class ArmorStandHelper {
    public static void propArmorStand(ArmorStand armorStand, JSONObject properties) throws ParseException, IOException {
        armorStand.setGravity((int) (long) properties.getOrDefault("NoGravity", (long)0) == 1);
        armorStand.setArms((int) (long)properties.get("ShowArms") == 1);
        armorStand.setInvisible((int) (long) properties.get("Invisible") == 1);
        armorStand.setSmall((int) (long) properties.get("Small") == 1);
        armorStand.setBasePlate((int) (long)properties.get("NoBasePlate") == 0);

        if(properties.containsKey("Pose")) {
            JSONObject poseObject = (JSONObject) properties.get("Pose");

            if (poseObject.containsKey("Body")) {
                JSONArray bodyPoseArray = (JSONArray) poseObject.get("Body");
                EulerAngle bodyPose = Utils.DegreesToEulerAngles((double) bodyPoseArray.get(0), (double) bodyPoseArray.get(1), (double) bodyPoseArray.get(2));

                armorStand.setBodyPose(bodyPose);
            }

            if (poseObject.containsKey("Head")) {
                JSONArray headPoseArray = (JSONArray) poseObject.get("Head");
                EulerAngle headPose = Utils.DegreesToEulerAngles((double) headPoseArray.get(0), (double) headPoseArray.get(1), (double) headPoseArray.get(2));
                armorStand.setHeadPose(headPose);
            }

            if (poseObject.containsKey("LeftArm")) {
                JSONArray leftArmPoseArray = (JSONArray) poseObject.get("LeftArm");
                EulerAngle leftArmPose = Utils.DegreesToEulerAngles((double) leftArmPoseArray.get(0), (double) leftArmPoseArray.get(1), (double) leftArmPoseArray.get(2));
                armorStand.setLeftArmPose(leftArmPose);
            }

            if (poseObject.containsKey("RightArm")) {
                JSONArray rightArmPoseArray = (JSONArray) poseObject.get("RightArm");
                EulerAngle rightArmPose = Utils.DegreesToEulerAngles((double) rightArmPoseArray.get(0), (double) rightArmPoseArray.get(1), (double) rightArmPoseArray.get(2));
                armorStand.setRightArmPose(rightArmPose);
            }

            if (poseObject.containsKey("LeftLeg")) {
                JSONArray leftLegPoseArray = (JSONArray) poseObject.get("LeftLeg");
                EulerAngle leftLegPose = Utils.DegreesToEulerAngles((double) leftLegPoseArray.get(0), (double) leftLegPoseArray.get(1), (double) leftLegPoseArray.get(2));
                armorStand.setLeftLegPose(leftLegPose);
            }

            if (poseObject.containsKey("RightLeg")) {
                JSONArray rightLegPoseArray = (JSONArray) poseObject.get("RightLeg");
                EulerAngle rightLegPose = Utils.DegreesToEulerAngles((double) rightLegPoseArray.get(0), (double) rightLegPoseArray.get(1), (double) rightLegPoseArray.get(2));
                armorStand.setRightLegPose(rightLegPose);
            }
        }

        JSONArray armorItemsArray = (JSONArray) properties.get("ArmorItems");
        EntityEquipment armorEquipment = armorStand.getEquipment();
        for(int c = 0; c < 4; c++) {
            JSONObject armorItemObject = (JSONObject) armorItemsArray.get(c);
            if (armorItemObject.isEmpty())
                continue;

            String armorItemID = ((String) armorItemObject.get("entity")).substring(10);

            //Player head
            if (c == 3 && armorItemID.equals("skull") && armorItemObject.containsKey("SkullOwner")) {

                ItemStack skullItem = null;

                JSONObject skullOwnerObject = (JSONObject) armorItemObject.get("SkullOwner");

                if (skullOwnerObject.containsKey("profileId")) {
                    String rawUUID = (String) skullOwnerObject.get("profileId");
                    BigInteger mostBits = new BigInteger(rawUUID.substring(0, 16), 16);
                    BigInteger leastBits = new BigInteger(rawUUID.substring(16, 32), 16);
                    UUID playerID = new UUID(mostBits.longValue(), leastBits.longValue());
                    skullItem = ItemSkullHelper.fromUUID(playerID);

                } else if (skullOwnerObject.containsKey("profileName")) {
                    String profileName = (String) skullOwnerObject.get("profileName");
                    skullItem = ItemSkullHelper.fromUsername(profileName);
                } else if (skullOwnerObject.containsKey("texture")) {
                    String skullId = (String)skullOwnerObject.getOrDefault("id", "");
                    String skullTexture = (String) skullOwnerObject.get("texture");

                    skullItem = ItemSkullHelper.fromBase64(skullId , skullTexture);
                }

                if (skullItem != null) {
                    //if (armorItemObject.containsKey("display_name")) {
                    //    SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
                    //    skullMeta.displayName(Component.text((String) armorItemObject.get("display_name")));
                    //    skullItem.setItemMeta(skullMeta);
                    //}

                    armorEquipment.setHelmet(skullItem);
                }


            } else {
                ItemStack armorItem = new ItemStack(Material.getMaterial(armorItemID.toUpperCase()));
                if (armorItemObject.containsKey("display_color")) {
                    LeatherArmorMeta armorMeta = (LeatherArmorMeta) armorItem.getItemMeta();
                    armorMeta.setColor(org.bukkit.Color.fromRGB((int) (long) armorItemObject.get("display_color")));
                    armorItem.setItemMeta(armorMeta);
                }

                if (c == 0)
                    armorEquipment.setBoots(armorItem);
                else if (c == 1)
                    armorEquipment.setLeggings(armorItem);
                else
                    armorEquipment.setChestplate(armorItem);
            }
        }

        JSONArray handItemsArray = (JSONArray) properties.get("HandItems");
        for(int c = 0; c < 2; c++ ){
            String handItemID = (String) handItemsArray.get(c);
            if(handItemID.isEmpty())
                continue;

            handItemID = handItemID.substring(10);

            ItemStack handItem = new ItemStack(Material.getMaterial(handItemID.toUpperCase()));

            if(c == 0)
                armorEquipment.setItemInMainHand(handItem);
            else
                armorEquipment.setItemInOffHand(handItem);
        }

        if(properties.containsKey("Rotation")){
            JSONArray entityRotationArray = (JSONArray) properties.get("Rotation");
            armorStand.setRotation( (float) (double)entityRotationArray.get(0), (float) (double)entityRotationArray.get(1));
        }
    }
}
