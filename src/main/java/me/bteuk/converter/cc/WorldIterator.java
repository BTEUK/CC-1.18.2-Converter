package me.bteuk.converter.cc;

import cubicchunks.regionlib.api.region.IRegionProvider;
import cubicchunks.regionlib.api.region.key.RegionKey;
import cubicchunks.regionlib.impl.EntryLocation2D;
import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.SaveCubeColumns;
import cubicchunks.regionlib.impl.save.SaveSection2D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cubicchunks.regionlib.lib.ExtRegion;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import me.bteuk.converter.Main;
import me.bteuk.converter.utils.LegacyID;
import me.bteuk.converter.utils.MinecraftIDConverter;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import org.json.simple.JSONArray;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class WorldIterator {

    private static final Map<SaveCubeColumns, List<IRegionProvider<EntryLocation2D>>> providers2d = new WeakHashMap<>();
    private static final Map<SaveCubeColumns, List<IRegionProvider<EntryLocation3D>>> providers3d = new WeakHashMap<>();

    EntryLocation3D e3d;
    boolean unique;
    ArrayList<LegacyID> uniqueBlocks = new ArrayList<>();
    HashMap<LegacyID, Integer> paletteID = new HashMap<>();
    ListTag<CompoundTag> palette = new ListTag<>(CompoundTag.class);
    byte meta;
    byte[] blocks;
    byte[] data;

    /*

    Iterates through all .2dr (512x512 files)

     */

    public WorldIterator(String inputPath, String outputPath) {

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
        EntryLocation3D.Provider provider3d = new EntryLocation3D.Provider();

        for (String sFile : files) {

            //Create new json array to store blocks for post-processing in.
            JSONArray ja = new JSONArray();

            //Iterate through all possible chunk columns in the file and continue with any that contain data.
            //A region2d is 512x512 which is 32*32 in terms of chunks, so 1024 individual chunks to iterate.
            for (int i = 0; i < 1024; i++) {

                long[] newData;
                RegionKey key = new RegionKey(sFile);
                EntryLocation2D e2d = provider2d.fromRegionAndId(key, i);

                try {

                    //Create list of sections to store the new sections in.
                    List<CompoundTag> sections = new ArrayList<>();

                    //Gets the data from the 2dr file.
                    Optional<ByteBuffer> column = saveCubeColumns.load(e2d, true);

                    //Create a list of Block Entities for this chunk.
                    List<CompoundTag> block_entities = new ArrayList<>();

                    //If the columndata is not empty
                    if (column.isPresent()) {

                        //Create a check for if a cube is empty, if the whole column is empty it can be skipped.
                        boolean isEmpty = true;

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
                                    continue;
                                }

                                if (cubeLevel.get("Sections") == null) {
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

                                for (int j = 0; j < 4096; j++) {

                                    //Get data for block.
                                    if (j % 2 == 0) {
                                        meta = (byte) (data[j >> 1] & 0x0f);
                                    } else {
                                        meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
                                    }

                                    //Add block if unique.
                                    addUnique(blocks[j], meta);

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
                                palette.clear();

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
                                    int blocksPerLong = 64 / blockSize;
                                    newData = new long[(4096 + blocksPerLong - 1) / blocksPerLong];
                                    System.out.println(newData.length);

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

                                    //Print the output of the long array.
                                    System.out.println(Arrays.toString(blocks));
                                    System.out.println(Arrays.toString(newData));

                                } else {

                                    //If the single block is not air, set the chunk to not empty.
                                    if (uniqueBlocks.get(0).getID() != 0) {
                                        isEmpty = false;
                                    }

                                    continue;

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

                                //Add section to sections.



                            } else {

                                //Skip this cube.
                                continue;

                            }
                        }

                        //If the whole chunk is empty, don't save it.
                        if (!isEmpty) {

                            //Save the chunk in the region file.

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

                //Write the region to a file.

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
}
