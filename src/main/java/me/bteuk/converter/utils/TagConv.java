package me.bteuk.converter.utils;

import net.querz.nbt.tag.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TagConv {
    public static void floatTagListToJson(String listKey, CompoundTag tag, JSONObject json){
        if(tag.containsKey(listKey)){
            ListTag<FloatTag> floatTags = tag.getListTag(listKey).asFloatTagList();
            List<Float> floatList = new ArrayList<>();
            for (FloatTag val : floatTags) {
                floatList.add(val.asFloat());
            }
            json.put(listKey, floatList);
        }
    }

    public static void getStringTagListProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName)){
            ListTag<StringTag> stringTags = tag.getListTag(tagName).asStringTagList();
            List<String> stringList = new ArrayList<>();
            for(StringTag stringTag : stringTags)
                stringList.add(stringTag.getValue());

            if(!stringList.isEmpty())
                properties.put(propName, stringList);
        }
    }

    public static void getIntArrayTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName)){
            IntArrayTag intArrayTag = tag.getIntArrayTag(tagName);
            properties.put(propName, intArrayTag.getValue());
        }
    }

    public static void getByteTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getByte(tagName));
    }

    public static void getStringTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getString(tagName));
    }

    public static void getDoubleTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getDouble(tagName));
    }

    public static void getBooleanTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getBoolean(tagName));
    }

    public static void getIntTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getInt(tagName));
    }

    public static void getLongTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getLong(tagName));
    }

    public static void getShortTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getShort(tagName));
    }

    public static void getFloatTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getFloat(tagName));
    }

    public static void getCompoundTagProperties(CompoundTag tag, JSONObject properties){
        Set<String> keySet = tag.keySet();
        for(String key : keySet){
            Tag<?> tag1 = tag.get(key);
            if(tag1 instanceof IntTag)
                properties.put(key, ((IntTag)tag1).asInt());
            if(tag1 instanceof StringTag)
                properties.put(key, ((StringTag)tag1).getValue());
            if(tag1 instanceof ByteTag)
                properties.put(key, ((ByteTag)tag1).asByte());
            if(tag1 instanceof DoubleTag)
                properties.put(key, ((DoubleTag)tag1).asDouble());
            if(tag1 instanceof FloatTag)
                properties.put(key, ((FloatTag)tag1).asFloat());
            if(tag1 instanceof ListTag<?>){
                ListTag<?> listTag = ((ListTag<?>) tag1);
                if(listTag.getTypeClass() == ByteTag.class){
                    ListTag<StringTag> tags = listTag.asStringTagList();
                    List<String> list = new ArrayList<>();
                    for (StringTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == FloatTag.class){
                    ListTag<FloatTag> tags = listTag.asFloatTagList();
                    List<Float> list = new ArrayList<>();
                    for (FloatTag val : tags) {
                        list.add(val.asFloat());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == ByteTag.class){
                    ListTag<ByteTag> tags = listTag.asByteTagList();
                    List<Byte> list = new ArrayList<>();
                    for (ByteTag val : tags) {
                        list.add(val.asByte());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == IntTag.class){
                    ListTag<IntTag> tags = listTag.asIntTagList();
                    List<Integer> list = new ArrayList<>();
                    for (IntTag val : tags) {
                        list.add(val.asInt());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == DoubleTag.class){
                    ListTag<DoubleTag> tags = listTag.asDoubleTagList();
                    List<Byte> list = new ArrayList<>();
                    for (DoubleTag val : tags) {
                        list.add(val.asByte());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == ShortTag.class){
                    ListTag<ShortTag> tags = listTag.asShortTagList();
                    List<Short> list = new ArrayList<>();
                    for (ShortTag val : tags) {
                        list.add(val.asShort());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == LongTag.class){
                    ListTag<LongTag> tags = listTag.asLongTagList();
                    List<Long> list = new ArrayList<>();
                    for (LongTag val : tags) {
                        list.add(val.asLong());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == ByteArrayTag.class){
                    ListTag<ByteArrayTag> tags = listTag.asByteArrayTagList();
                    List<byte[]> list = new ArrayList<>();
                    for (ByteArrayTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == IntArrayTag.class){
                    ListTag<IntArrayTag> tags = listTag.asIntArrayTagList();
                    List<int[]> list = new ArrayList<>();
                    for (IntArrayTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == LongArrayTag.class){
                    ListTag<LongArrayTag> tags = listTag.asLongArrayTagList();
                    List<long[]> list = new ArrayList<>();
                    for (LongArrayTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }
            }

        }
    }

    public static String flattenStringsCompoundTag(CompoundTag tag){
        String flattened = "";
        Set<String> tagKeys =  tag.keySet();
        int c = 0;
        for(String state : tagKeys){
            flattened += String.format("%1$s=%2$s", state, tag.getString(state) + (c + 1 == tagKeys.size() ? "" : ","));
            c++;
        }

        return flattened;
    }

    public static void parseFlattenedTags(String flattenedTags, HashMap<String, String> tags){
        if(flattenedTags.contains("=")) {
            if (!flattenedTags.contains(",")) {
                int index = flattenedTags.indexOf("=");
                tags.put(flattenedTags.substring(0, index), flattenedTags.substring(index + 1));
            }else {
                String[] argStates = flattenedTags.split(",");
                for (String argState : argStates){
                    int index = argState.indexOf("=");
                    tags.put(argState.substring(0, index), argState.substring(index + 1));
                }
            }
        }
    }
}
