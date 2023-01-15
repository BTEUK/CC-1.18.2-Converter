package me.bteuk.converter;

import cubicchunks.regionlib.api.region.IRegionProvider;
import cubicchunks.regionlib.api.region.key.RegionKey;
import cubicchunks.regionlib.impl.EntryLocation2D;
import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.MinecraftChunkLocation;
import cubicchunks.regionlib.impl.SaveCubeColumns;
import cubicchunks.regionlib.impl.header.TimestampHeaderEntryProvider;
import cubicchunks.regionlib.impl.save.MinecraftSaveSection;
import cubicchunks.regionlib.impl.save.SaveSection2D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cubicchunks.regionlib.lib.ExtRegion;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import me.bteuk.converter.Main;
import me.bteuk.converter.cc.MemoryReadRegion;
import me.bteuk.converter.cc.MemoryWriteRegion;
import me.bteuk.converter.cc.RWLockingCachedRegionProvider;
import me.bteuk.converter.cc.Utils;
import me.bteuk.converter.utils.LegacyID;
import me.bteuk.converter.utils.MinecraftIDConverter;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;
import org.checkerframework.checker.units.qual.C;
import org.json.simple.JSONArray;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static cubicchunks.regionlib.impl.save.MinecraftSaveSection.MinecraftRegionType.MCA;

public class WorldIterator {

    private static final Map<SaveCubeColumns, List<IRegionProvider<EntryLocation2D>>> providers2d = new WeakHashMap<>();
    private static final Map<SaveCubeColumns, List<IRegionProvider<EntryLocation3D>>> providers3d = new WeakHashMap<>();

    EntryLocation3D e3d;
    boolean unique;
    ArrayList<LegacyID> uniqueBlocks = new ArrayList<>();
    HashMap<LegacyID, Integer> paletteID = new HashMap<>();
    ListTag<CompoundTag> palette = new ListTag<>(CompoundTag.class);
    ListTag<StringTag> biomePalette = new ListTag<>(StringTag.class);
    HashMap<Byte, Integer> biomePaletteID = new HashMap<>();
    ListTag<CompoundTag> airPalette = new ListTag<>(CompoundTag.class);
    ListTag<StringTag> defaultBiome = new ListTag<>(StringTag.class);
    ListTag<CompoundTag> tile_entities = new ListTag<>(CompoundTag.class);
    CompoundTag block_entity = null;
    byte meta;
    byte[] blocks;
    byte[] data;
    int entryX;
    int entryZ;
    int cX;
    int cY;
    int cZ;
    int x;
    int z;
    String biomeName;

    ListTag<ListTag<ShortTag>> post_processing = new ListTag<>(ListTag.class);
    CompoundTag structures = new CompoundTag();

    /*

    Iterates through all .2dr (512x512 files)

     */

    public WorldIterator(String inputPath, String outputPath) {

        //Add air to airPalette for empty sections.
        CompoundTag block = new CompoundTag();
        //Add block name as String tag.
        block.putString("Name", "minecraft:air");
        airPalette.add(block);

        //Create the default biome palette;
        defaultBiome.addString(Main.DEFAULT_BIOME);

        //Get the path from the String.
        Path file = Paths.get(inputPath);
        Path output = Paths.get(outputPath);

        //Check if file exists.
        if (!Files.exists(file) && !Files.exists(output)) {
            return;
        }

        //Create output sub-folders.
        try {
            Files.createDirectories(output.resolve("post-processing"));
            Files.createDirectories(output.resolve("region"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SaveCubeColumns saveCubeColumns = createSave(file);

        //Get all the files in the folder.
        String[] files = new File(file.resolve("region2d").toString()).list();

        if (files == null) {
            return;
        }

        //Create 2d and 3d region provider.
        EntryLocation2D.Provider provider2d = new EntryLocation2D.Provider();

        //Create empty post-processing compound list.
        for (int p = Main.MIN_Y_CUBE; p < Main.MAX_Y_CUBE; p++) {
            post_processing.add(new ListTag<>(ShortTag.class));
        }

        //Create empty structures tag.
        structures.put("References", new CompoundTag());
        structures.put("starts", new CompoundTag());

        for (String sFile : files) {

            //Create new json array to store blocks for post-processing in.
            JSONArray ja = new JSONArray();

            //Iterate through all possible chunk columns in the file and continue with any that contain data.
            //A region2d is 512x512 which is 32*32 in terms of chunks, so 1024 individual chunks to iterate.
            for (int i = 0; i < 1024; i++) {

                CompoundTag chunk = new CompoundTag();

                long[] newData = null;
                long[] biomeData;
                RegionKey key = new RegionKey(sFile);
                EntryLocation2D e2d = provider2d.fromRegionAndId(key, i);
                entryX = e2d.getEntryX();
                entryZ = e2d.getEntryZ();

                try {

                    //Create list of sections to store the new sections in.
                    ListTag<CompoundTag> sections = new ListTag<>(CompoundTag.class);

                    //Gets the data from the 2dr file.
                    Optional<ByteBuffer> column = saveCubeColumns.load(e2d, true);

                    //Create a list of Block Entities for this chunk.
                    ListTag<CompoundTag> block_entities = new ListTag<>(CompoundTag.class);

                    //If the columndata is not empty
                    if (column.isPresent()) {

                        //Create a check for if a cube is empty, if the whole column is empty it can be skipped.
                        boolean isEmpty = true;

                        //Clear the biome palette.
                        biomePalette.clear();
                        biomePaletteID.clear();

                        CompoundTag columnTag = (CompoundTag) readCompressedCC(new ByteArrayInputStream(column.get().array())).getTag();
                        CompoundTag columnLevel = columnTag.getCompoundTag("Level");

                        CompoundTag biomes = new CompoundTag();

                        //Get the biome data.
                        byte[] oldBiomes = columnLevel.getByteArray("Biomes");
                        if (oldBiomes != null) {

                            //Get the biomes, since Minecraft 1.18.2 stored biomes in 4x4x4 cubes
                            // we must create a 4x4x4 array of cubes to fill a section.
                            //However we it is stored using a palette with a reference to the palette,
                            // so we start by constructing the palette using the number of unique biomes.
                            for (int ind = 0; ind < oldBiomes.length; ind++) {

                                //We can skip 3 out of 4 indexes since we only need a 4x4 layer rather than 16x16.
                                //We'll skip all indixes divisible by 2 and 3, this will leave the second index out of 4 (0,1,2,3).
                                if (ind % 4 == 1 && ind / 16 % 4 == 1) {

                                    biomeName = MinecraftIDConverter.getBiome(oldBiomes[ind]);
                                    if (!containsBiome(biomeName)) {
                                        //Index equals biome size before adding the new biome.
                                        biomePaletteID.put(oldBiomes[ind], biomePalette.size());
                                        biomePalette.add(new StringTag(biomeName));
                                    }

                                } else {
                                    continue;
                                }
                            }

                            //Add the biome palette
                            biomes.put("palette", biomePalette);

                            //If palette has more than 1 biome we need to create a data array.
                            if (biomePalette.size() > 1) {

                                //Find the number of blocks that will be stored in each long array. If the count is 16 or less then each block will use 4 bits.
                                //A long allows for 64 bits in total.
                                int blockSize = log2(biomePalette.size());
                                int blocksPerLong = 64 / blockSize;
                                biomeData = new long[(64 + blocksPerLong - 1) / blocksPerLong];

                                int counter = 0;
                                //To remember the index of the long.
                                int index = 0;
                                long blockIndex;

                                //To get the index from the old biome array.
                                int oldIndex = 0;

                                //Long value
                                long blockStorage = 0;

                                //Convert the data to the new section structure of Minecraft 1.18.2
                                for (int j = 0; j < 64; j++) {

                                    //If counter is equal or greater than the blocksPerLong we need to create a new long and reset the counter.
                                    if (counter >= blocksPerLong) {
                                        counter = 0;
                                        blockStorage = 0;
                                        index++;
                                    }

                                    //Get data for block.
                                    if (j % 2 == 0) {
                                        meta = (byte) (data[j >> 1] & 0x0f);
                                    } else {
                                        meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
                                    }

                                    //Find the next correct index for the old biome array.
                                    while (oldIndex % 4 != 1 || oldIndex / 16 % 4 != 1) {
                                        //If we've reached the end of the array indexes return to 1, which is the first valid index.
                                        if (oldIndex >= 255) {
                                            oldIndex = 1;
                                        }
                                        oldIndex++;
                                    }

                                    //Get the id for the block.
                                    blockIndex = biomePaletteID.get(oldBiomes[oldIndex]);
                                    oldIndex++;

                                    //Shift the blockIndex by blockSize * counter so the value gets put in the right place in the long.
                                    //Using the bitwise or function we can set the values in the long.
                                    blockIndex = blockIndex << (counter * blockSize);
                                    blockStorage = blockStorage | blockIndex;

                                    //Update the counter.
                                    counter++;

                                    //Set the long in the array when the counter is at the blocksPerLong value.
                                    if (counter == blocksPerLong) {
                                        biomeData[index] = blockStorage;
                                    }
                                }

                                //Add the biome data to the biomes tag.
                                biomes.putLongArray("data", biomeData);
                            }
                        }

                        //Get all cubes that could be in the range of heights.
                        for (int y = Main.MIN_Y_CUBE; y < Main.MAX_Y_CUBE; y++) {

                            //Get the cube of data.
                            e3d = new EntryLocation3D(e2d.getEntryX(), y, e2d.getEntryZ());
                            Optional<ByteBuffer> cube = saveCubeColumns.load(e3d, true);

                            //Check if it exists.
                            if (cube.isPresent()) {

                                //Retrieve the data from the cube.
                                CompoundTag cubeTag = (CompoundTag) readCompressedCC(new ByteArrayInputStream(cube.get().array())).getTag();
                                CompoundTag cubeLevel = cubeTag.getCompoundTag("Level");

                                if (cubeLevel == null) {
                                    sections.add(createEmptySection(y));
                                    continue;
                                }

                                if (cubeLevel.get("Sections") == null) {
                                    sections.add(createEmptySection(y));
                                    continue;
                                }

                                //Get the sections from the cube.
                                ListTag<CompoundTag> oldSections = (ListTag<CompoundTag>) cubeLevel.getListTag("Sections");
                                CompoundTag oldSection = oldSections.get(0);

                                //Get unique blocks in section using Blocks and Data.
                                blocks = oldSection.getByteArray("Blocks");
                                data = oldSection.getByteArray("Data");

                                //Clear uniqueBlocks.
                                uniqueBlocks.clear();

                                //Load tile entities.
                                tile_entities = (ListTag<CompoundTag>) cubeLevel.getListTag("TileEntities");

                                for (int j = 0; j < 4096; j++) {

                                    //Get data for block.
                                    if (j % 2 == 0) {
                                        meta = (byte) (data[j >> 1] & 0x0f);
                                    } else {
                                        meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
                                    }

                                    //Add block if unique.
                                    addUnique(blocks[j], meta);

                                    //Convert block entities
                                    //If the block is a block entity, load it.
                                    if (MinecraftIDConverter.isBlockEntity(blocks[j])) {

                                        //Convert current block to x,y,z coordinate.
                                        //blockPos = y*256 + z*16 + x = i
                                        cY = j / 256;
                                        cZ = (j - (cY * 256)) / 16;
                                        cX = j - (cY * 256) - (cZ * 16);

                                        //Find the tile entity from the list.
                                        for (CompoundTag tile_entity : tile_entities) {

                                            x = tile_entity.getInt("x") >= 0 ? tile_entity.getInt("x") % 16 : 16 + (tile_entity.getInt("x") % 16);
                                            z = tile_entity.getInt("z") >= 0 ? tile_entity.getInt("z") % 16 : 16 + (tile_entity.getInt("z") % 16);

                                            //If the coordinates are equal.
                                            if ((x == cX) && (z == cZ) && (tile_entity.getInt("y") == (y * 16 + cY))) {

                                                //Get the block entity
                                                block_entity = MinecraftIDConverter.getBlockEntity(blocks[j], meta, tile_entity);

                                                //Add the block entity to the list.
                                                //Only if the block entity needs to be added.
                                                if (!MinecraftIDConverter.blockEntityNotAdded(blocks[j])) {
                                                    block_entities.add(block_entity);
                                                }
                                                break;

                                            }
                                        }
                                    }

                                    //If block requires post-processing add it to the txt file.
                                    if (MinecraftIDConverter.requiredPostProcessing(blocks[j], meta)) {

                                        //If the block is a block entity, also get that data.
                                        if (MinecraftIDConverter.isBlockEntity(blocks[j])) {

                                            //TODO Store blocks such as double plants, stairs, ect.
                                            // They will be fixed in post-processing.

                                        }
                                    }
                                }

                                //Convert the list of unique blocks to a 1.18.2 block palette.
                                palette = new ListTag<>(CompoundTag.class);
                                paletteID = new HashMap<>();

                                int counter = 0;
                                for (LegacyID id : uniqueBlocks) {
                                    //Store the index of this block, so we can easily reference it
                                    // from the palette without having the convert it again.
                                    paletteID.put(id, counter);
                                    counter++;
                                    palette.add(MinecraftIDConverter.getBlock(id));
                                }

                                //If the palette only has one block skip the next part entirely.
                                if (palette.size() != 1) {

                                    //Set column to not empty.
                                    isEmpty = false;

                                    //Find the number of blocks that will be stored in each long array. If the count is 16 or less then each block will use 4 bits.
                                    //A long allows for 64 bits in total.
                                    int blockSize = log2(uniqueBlocks.size());
                                    if (blockSize < 4) {
                                        blockSize = 4;
                                    }
                                    int blocksPerLong = 64 / blockSize;
                                    newData = new long[(4096 + blocksPerLong - 1) / blocksPerLong];

                                    counter = 0;
                                    //To remember the index of the long.
                                    int index = 0;
                                    long blockIndex;

                                    //Long value
                                    long blockStorage = 0;

                                    //Convert the data to the new section structure of Minecraft 1.18.2
                                    for (int j = 0; j < 4096; j++) {

                                        //If counter is equal or greater than the blocksPerLong we need to create a new long and reset the counter.
                                        if (counter >= blocksPerLong) {
                                            counter = 0;
                                            blockStorage = 0;
                                            index++;
                                        }

                                        //Get data for block.
                                        if (j % 2 == 0) {
                                            meta = (byte) (data[j >> 1] & 0x0f);
                                        } else {
                                            meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
                                        }

                                        //Get the id for the block.
                                        blockIndex = getPaletteID(blocks[j], meta);

                                        //Shift the blockIndex by blockSize * counter so the value gets put in the right place in the long.
                                        //Using the bitwise or function we can set the values in the long.
                                        blockIndex = blockIndex << (counter * blockSize);
                                        blockStorage = blockStorage | blockIndex;

                                        //Update the counter.
                                        counter++;

                                        //Set the long in the array when the counter is at the blocksPerLong value.
                                        if (counter == blocksPerLong) {
                                            newData[index] = blockStorage;
                                        }

                                        //Check if the block is a block entity, if true also add it to the block entities list.
                                        if (MinecraftIDConverter.isBlockEntity(blocks[j])) {

                                            //If the block entity is going to be set in post-processing then add default values.
                                            if (MinecraftIDConverter.requiredPostProcessing(blocks[j], meta)) {


                                            }
                                        }
                                    }

                                } else {

                                    //If the single block is not air, set the chunk to not empty.
                                    if (uniqueBlocks.get(0).getID() != 0) {
                                        isEmpty = false;
                                    } else {
                                        sections.add(createEmptySection(y));
                                        continue;
                                    }
                                }

                                //Construct section.
                                CompoundTag section = new CompoundTag();

                                section.putByte("Y", (byte) y);

                                //Create block_states compound tag.
                                CompoundTag block_states = new CompoundTag();
                                block_states.put("palette", palette);
                                if (palette.size() > 1) {
                                    block_states.putLongArray("data", newData);
                                }

                                section.put("block_states", block_states);

                                section.put("biomes", biomes);

                                section.putByteArray("BlockLight", oldSection.getByteArray("BlockLight"));
                                section.putByteArray("SkyLight", oldSection.getByteArray("SkyLight"));

                                //Add section to sections.
                                sections.add(section);

                            } else {

                                //Add empty section
                                sections.add(createEmptySection(y));
                                continue;

                            }
                        }

                        //If the whole chunk is empty, don't save it.
                        if (!isEmpty) {

                            //Add block entities to the chunk.
                            chunk.put("block_entities", block_entities);

                            //Add sections to chunk.
                            chunk.put("sections", sections);

                            //Set chunk status.
                            chunk.putString("Status", "heightmaps");

                            //Set data version
                            chunk.putInt("DataVersion", 2975);

                            //Set position of chunk.
                            chunk.putInt("xPos", columnLevel.getInt("x"));
                            chunk.putInt("zPos", columnLevel.getInt("z"));
                            chunk.putInt("yPos", Main.MIN_Y_CUBE);

                            //Set last update.
                            chunk.putLong("LastUpdate", 0);

                            //Add block and fluid ticks.
                            chunk.put("fluid_ticks", new ListTag<>(CompoundTag.class));
                            chunk.put("block_ticks", new ListTag<>(CompoundTag.class));

                            //Inhabited Time
                            chunk.putLong("InhabitedTime", 0);

                            //Add Post Processing
                            chunk.put("PostProcessing", post_processing);

                            //Add Structures
                            chunk.put("structures", structures);

                            //Is Light On
                            chunk.putBoolean("isLightOn", true);

                            //Save the chunk in the region file.
                            save(output.resolve("region"), writeCompressed(chunk, true));

                        }



                        /*CompoundTag columnTag = column.get() == null ? null : readCompressedCC(new ByteArrayInputStream(column.get().array()));
                        CompoundMap columnLevel = (CompoundMap) columnTag.getValue().get("Level").getValue();
                        for (Tag<?> tag : columnLevel) {
                            System.out.println(tag.getName());
                        }*/
                    } else {
                        //Skip this column.
                        continue;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {

                //Write the json array to a file.
                FileWriter ppFile = new FileWriter(output.resolve("post-processing").toString() + "/" + sFile.replace(".2dr", ".txt"));
                ppFile.write(ja.toJSONString());
                ppFile.flush();
                ppFile.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static SaveCubeColumns createSave(Path path) {
        try {
            Utils.createDirectories(path);

            Path part2d = path.resolve("region2d");
            Utils.createDirectories(part2d);

            Path part3d = path.resolve("region3d");
            Utils.createDirectories(part3d);

            EntryLocation2D.Provider keyProv2d = new EntryLocation2D.Provider();
            EntryLocation3D.Provider keyProv3d = new EntryLocation3D.Provider();

            IRegionProvider<EntryLocation2D> prov2d1, prov2d2;
            IRegionProvider<EntryLocation3D> prov3d1, prov3d2;
            SaveSection2D section2d = new SaveSection2D(
                    prov2d1 = new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(keyProv2d, part2d, (keyProv, r) ->
                                    new MemoryReadRegion.Builder<EntryLocation2D>()
                                            .setDirectory(part2d)
                                            .setRegionKey(r)
                                            .setKeyProvider(keyProv2d)
                                            .setSectorSize(512)
                                            .build(),
                                    (file, key) -> Files.exists(file)
                            )
                    ),
                    prov2d2 = new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(new EntryLocation2D.Provider(), part2d,
                                    (keyProvider, regionKey) -> new ExtRegion<>(part2d, Collections.emptyList(), keyProvider, regionKey),
                                    (file, key) -> Files.exists(file.resolveSibling(key.getRegionKey().getName() + ".ext"))
                            )
                    ));
            SaveSection3D section3d = new SaveSection3D(
                    prov3d1 = new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(keyProv3d, part3d, (keyProv, r) ->
                                    new MemoryReadRegion.Builder<EntryLocation3D>()
                                            .setDirectory(part3d)
                                            .setRegionKey(r)
                                            .setKeyProvider(keyProv3d)
                                            .setSectorSize(512)
                                            .build(),
                                    (file, key) -> Files.exists(file)
                            )
                    ),
                    prov3d2 = new RWLockingCachedRegionProvider<>(
                            new SimpleRegionProvider<>(new EntryLocation3D.Provider(), part3d,
                                    (keyProvider, regionKey) -> new ExtRegion<>(part3d, Collections.emptyList(), keyProvider, regionKey),
                                    (dir, key) -> Files.exists(dir.resolveSibling(key.getRegionKey().getName() + ".ext"))
                            )
                    ));

            SaveCubeColumns saveCubeColumns = new SaveCubeColumns(section2d, section3d);
            providers2d.put(saveCubeColumns, Arrays.asList(prov2d1, prov2d2));
            providers3d.put(saveCubeColumns, Arrays.asList(prov3d1, prov3d2));
            return saveCubeColumns;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private NamedTag readCompressedCC(InputStream is) throws IOException {
        try (NBTInputStream nbtInputStream = new NBTInputStream(new BufferedInputStream(new GZIPInputStream(is)))) {
            return nbtInputStream.readTag(Tag.DEFAULT_MAX_DEPTH);
        }
    }

    private void addUnique(byte block, byte data) {
        unique = true;
        for (LegacyID id : uniqueBlocks) {
            if (id.equals(block, data)) {
                unique = false;
            }
        }
        if (unique == true) {
            uniqueBlocks.add(new LegacyID(block, meta));
        }
    }

    private boolean containsBiome(String biome) {
        for (StringTag biomeNamespace : biomePalette) {
            if (biomeNamespace.getValue().equals(biome)) {
                return true;
            }
        }
        return false;
    }

    private int getPaletteID(byte block, byte data) {
        for (LegacyID id : uniqueBlocks) {
            if (id.equals(block, data)) {
                return paletteID.get(id);
            }
        }
        return 0;
    }

    private int log2(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value);
    }

    private void save(Path regionDir, ByteBuffer data) throws IOException {
        Utils.createDirectories(regionDir);
        MinecraftSaveSection save = new MinecraftSaveSection(new RWLockingCachedRegionProvider<>(
                new SimpleRegionProvider<>(new MinecraftChunkLocation.Provider(MCA.name().toLowerCase()), regionDir, (keyProvider, regionKey) ->
                        MemoryWriteRegion.<MinecraftChunkLocation>builder()
                                .setDirectory(regionDir)
                                .setSectorSize(4096)
                                .setKeyProvider(keyProvider)
                                .setRegionKey(regionKey)
                                .addHeaderEntry(new TimestampHeaderEntryProvider<>(TimeUnit.SECONDS))
                                .build(),
                        (file, key) -> Files.exists(file)
                )
        ));
        save.save(new MinecraftChunkLocation(entryX, entryZ, "mca"), data);
    }

    private ByteBuffer writeCompressed(CompoundTag tag, boolean prefixFormat) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (prefixFormat) {
            bytes.write(1); // mark as GZIP
        }
        try (NBTOutputStream nbtOut = new NBTOutputStream(new BufferedOutputStream(new GZIPOutputStream(bytes)))) {
            nbtOut.writeTag(tag, Tag.DEFAULT_MAX_DEPTH);
        }
        return ByteBuffer.wrap(bytes.toByteArray());
    }

    private CompoundTag createEmptySection(int y) {

        //Construct section.
        CompoundTag section = new CompoundTag();

        section.putByte("Y", (byte) y);

        //Create block_states compound tag.
        CompoundTag block_states = new CompoundTag();

        //Add palette with just air.
        block_states.put("palette", airPalette);

        section.put("block_states", block_states);

        //Add the biome using the default biome.
        CompoundTag biomes = new CompoundTag();
        biomes.put("palette", defaultBiome);

        section.put("biomes", biomes);

        //TODO Add BlockLight and SkyLight

        return section;
    }
}
