package me.bteuk.converterplugin.utils.items;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.map.MapView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ItemMapsHelper {
    public static ItemMapsHelper instance;

    private JSONObject mapsID;
    private World world;
    private Path mapsIdPath;
    private Path mapsPath;
    private Path dataPath;
    private JSONParser parser;

    public ItemMapsHelper(World world, Path mapsIdPath, Path mapsPath, Path dataPath, JSONParser parser) {
        this.mapsID = new JSONObject();
        this.world = world;
        this.mapsIdPath = mapsIdPath;
        this.mapsPath = mapsPath;
        this.dataPath = dataPath;
        this.parser = parser;

        instance = this;
    }

    public void readReadMapsID() throws IOException, ParseException {
        Reader reader = new FileReader(mapsIdPath.toFile());
        Object rawMapID = parser.parse(reader);
        mapsID = (JSONObject) rawMapID;
    }

    public void writeMapsID() throws IOException {
        FileWriter _mapsID = new FileWriter(mapsIdPath.toFile());
        if (mapsID == null)
            mapsID = new JSONObject();

        _mapsID.write(mapsID.toJSONString());
        _mapsID.flush();
        _mapsID.close();
    }

    public void convertMaps() throws Exception {
        Stream<Path> entries = Files.list(mapsPath);
        List<Path> mapsFiles = entries.toList();

        List<String> exceptions = new ArrayList<>();

        for(Path mapPath : mapsFiles){
            String mapName = mapPath.toFile().getName();
            if(mapName.startsWith("map_") && mapName.endsWith(".json")){
                mapName = mapName.substring(4);
                mapName = mapName.replace(".json", "");
                int id = Integer.parseInt(mapName);

                JSONObject mappingID = new JSONObject();

                Reader reader = new FileReader(mapPath.toFile());
                JSONObject mapItem = (JSONObject) parser.parse(reader);
                reader.close();

                MapView mapView = Bukkit.createMap(world);
                int new_id = mapView.getId();

                byte scale = (byte) (long) mapItem.get("scale");
                switch (scale) {
                    case 0 -> mapView.setScale(MapView.Scale.CLOSEST);
                    case 1 -> mapView.setScale(MapView.Scale.CLOSE);
                    case 2 -> mapView.setScale(MapView.Scale.NORMAL);
                    case 3 -> mapView.setScale(MapView.Scale.FAR);
                    case 4 -> mapView.setScale(MapView.Scale.FARTHEST);
                }

                if (mapItem.containsKey("unlimited_tracking"))
                    mapView.setUnlimitedTracking((int) (long) mapItem.get("unlimited_tracking") == 1);

                mapView.setCenterX((int) (long) mapItem.get("x_center"));
                mapView.setCenterZ((int) (long) mapItem.get("z_center"));

                try {
                    JSONArray _colors = (JSONArray) mapItem.get("colors");
                    byte[] cols = new byte[_colors.size()];
                    for(int c = 0; c < _colors.size(); c++)
                        cols[c] = (byte)(long)_colors.get(c);

                    Field worldMapField = mapView.getClass().getDeclaredField("worldMap");
                    worldMapField.setAccessible(true);
                    Object worldMap = worldMapField.get(mapView);
                    //MapsViewReflection.WORLDMAP_PROXY.setColors(worldMap, cols);

                    //String colorFieldName = "g";
                    //Boolean foundColorField = false;
                    //String vanillaRenderFieldName = "vanillaRender";
                    //Boolean foundRenderDataField = false;
                    Field[] worldMapFields = worldMap.getClass().getDeclaredFields();
                    for(Field _worldMapField : worldMapFields) {
                        if (_worldMapField.getType() == byte[].class){
                            _worldMapField.setAccessible(true);
                            byte[] colField = (byte[]) _worldMapField.get(worldMap);
                            if(colField.length == cols.length)
                                _worldMapField.set(worldMap, cols);
                            _worldMapField.setAccessible(false);
                        }
                        else if(_worldMapField.getType().getName().endsWith("RenderData")) {
                            _worldMapField.setAccessible(true);
                            Object worldMapRenderData = _worldMapField.get(worldMap);

                            String renderDataBufferFieldName = "buffer";
                            Field[] renderDataFields = worldMapRenderData.getClass().getDeclaredFields();
                            for (Field renderDataField : renderDataFields)
                                if (renderDataField.getType() == byte[].class && !renderDataField.getName().equals(renderDataBufferFieldName))
                                    renderDataBufferFieldName = renderDataField.getName();

                            Field renderDataBufferField = worldMapRenderData.getClass().getDeclaredField(renderDataBufferFieldName);
                            renderDataBufferField.setAccessible(true);
                            renderDataBufferField.set(worldMapRenderData, cols);
                            renderDataBufferField.setAccessible(false);

                            _worldMapField.set(worldMap, worldMapRenderData);
                            _worldMapField.setAccessible(false);
                        }
                    }

                    worldMapField.set(mapView, worldMap);
                    worldMapField.setAccessible(false);

                } catch (Exception e) {
                    exceptions.add(e.getMessage());
                }

                world.save();

                mappingID.put("new_id", new_id);
                mapsID.put("map_" + id, mappingID);

                if (Files.exists(mapPath))
                    try {
                        Files.delete(mapPath);
                    } catch (IOException exception) {
                        String w = "2";
                    }

                //mapView = Bukkit.getMap(new_id);

                /*
                //Manually edit the map_<#>.dat file
                Path mapDatFile = dataPath.resolve("map_" + new_id + ".dat");

                try{
                    NamedTag namedMapTag = NBTUtil.read(mapDatFile.toFile());
                    CompoundTag mapTag = (CompoundTag) namedMapTag.getTag();
                    CompoundTag mapDataTag = mapTag.getCompoundTag("data");

                    JSONArray _colors = (JSONArray) mapItem.get("colors");
                    byte[] cols = new byte[_colors.size()];
                    for(int c = 0; c < _colors.size(); c++)
                        cols[c] = (byte)(long)_colors.get(c);
                    ByteArrayTag colors = new ByteArrayTag(cols);
                    mapDataTag.put("colors", colors);

                    mapTag.put("data", mapDataTag);
                    namedMapTag.setTag(mapTag);
                    NBTUtil.write(namedMapTag, mapDatFile.toFile());

                }catch (Exception ex){
                    logger.warning(String.format(""));
                }*/


            }
        }



        writeMapsID();

        if(!exceptions.isEmpty()){
            String ex = exceptions.get(0);
            if(exceptions.size() > 1) {
                for (int c = 1; c < exceptions.size(); c++)
                    ex += "\n" + exceptions.get(c);
            }
            throw new Exception(ex);
        }
    }

    public MapView getMapView(int id) {
        if (mapsID.containsKey("map_" + id)) {
            JSONObject mappingID = (JSONObject) mapsID.get("map_" + id);
            int new_id = 0;
            Object raw_new_id = mappingID.get("new_id");
            if(raw_new_id instanceof Long){
                Long longNewID = (Long) raw_new_id;
                new_id = longNewID.intValue();
            }else if(raw_new_id instanceof Integer){
                new_id = (Integer) raw_new_id;
            }
            return Bukkit.getMap(new_id);
        } else {
            return Bukkit.createMap(world);
        }
    }

    /*public static final class MapsViewReflection {
        public static final WorldMapProxy WORLDMAP_PROXY;

        static {
            final ReflectionRemapper reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar();
            final ReflectionProxyFactory reflectionProxyFactory = ReflectionProxyFactory.create(reflectionRemapper, MapsViewReflection.class.getClassLoader());
            WORLDMAP_PROXY = reflectionProxyFactory.reflectionProxy(WorldMapProxy.class);
        }
    }

    @Proxies(className = "net.minecraft.world.level.saveddata.maps.WorldMap")
    public interface WorldMapProxy {
        @FieldSetter("g")
        void setColors(Object worldMap, byte[] value);
    }*/
}
