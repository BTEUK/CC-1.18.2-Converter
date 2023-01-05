package me.bteuk.converter.cc;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.stream.NBTInputStream;
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
    HashMap<LegacyID,Integer> paletteID = new HashMap<>();
    List<CompoundTag> palette = new ArrayList<>();
    byte meta;
    byte[] blocks;
    byte[] data;

    /*

    Iterates through all .2dr (512x512 files)

     */

    public WorldIterator(String path) {

        //Get the path from the String.
        Path file = Paths.get(path);

        //Check if file exists.
        if (!Files.exists(file)) {
            return;
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

            //Iterate through all possible chunk columns in the file and continue with any that contain data.
            //A region2d is 512x512 which is 32*32 in terms of chunks, so 1024 individual chunks to iterate.
            for (int i = 0; i < 1024; i++) {

                RegionKey key = new RegionKey(sFile);
                EntryLocation2D e2d = provider2d.fromRegionAndId(key, i);

                try {

                    //Create list of sections to store the new sections in.
                    List<CompoundTag> sections = new ArrayList<>();

                    //Gets the data from the 2dr file.
                    Optional<ByteBuffer> column = saveCubeColumns.load(e2d, true);

                    //If the columndata is not empty
                    if (column.isPresent()) {

                        //Get all cubes that could be in the range of heights.
                        for (int y = Main.MIN_Y_CUBE; y < Main.MAX_Y_CUBE; y++) {

                            //Get the cube of data.
                            e3d = new EntryLocation3D(e2d.getEntryX(), y, e2d.getEntryZ());
                            Optional<ByteBuffer> cube = saveCubeColumns.load(e3d, true);

                            //Check if it exists.
                            if (cube.isPresent()) {

                                //Retrieve the data from the cube.
                                CompoundTag cubeTag = readCompressedCC(new ByteArrayInputStream(cube.get().array()));
                                CompoundMap cubeLevel = (CompoundMap) cubeTag.getValue().get("Level").getValue();

                                if (cubeLevel == null) {
                                    continue;
                                }

                                if (cubeLevel.get("Sections") == null) {
                                    continue;
                                }

                                //Get the sections from the cube.
                                List<CompoundTag> oldSections = (List<CompoundTag>) cubeLevel.get("Sections").getValue();

                                CompoundMap oldSection = oldSections.get(0).getValue();

                                //Get unique blocks in section using Blocks and Data.
                                blocks = (byte[]) oldSection.get("Blocks").getValue();
                                data = (byte[]) oldSection.get("Data").getValue();

                                //Clear uniqueBlocks.
                                uniqueBlocks.clear();

                                for (int j = 0; j < 4096; j++) {

                                    //Get data for block.
                                    if (j % 2 == 0) {
                                        meta = (byte) (data[j>>1] & 0x0f);
                                    } else {
                                        meta = (byte) ((data[j>>1] >> 4) & 0x0f);
                                    }

                                    //Add block if unique.
                                    addUnique(blocks[j], meta);

                                    //TODO Store blocks such as double plants, stairs, ect.
                                    // They will be fixed in post-processing.

                                }

                                //Convert the list of unique blocks to a 1.18.2 block palette.
                                palette.clear();

                                int counter = 0;
                                for (LegacyID id : uniqueBlocks) {
                                    //Store the index of this block, so we can easily reference it
                                    // from the palette without having the convert it again.
                                    paletteID.put(id, 0);
                                    counter++;
                                    palette.add(MinecraftIDConverter.getBlock(id));
                                }

                                //Convert the data to the new section structure of Minecraft 1.18.2


                            } else {

                                //Skip this cube.
                                continue;

                            }
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

            System.out.println(uniqueBlocks);
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

    private CompoundTag readCompressedCC(InputStream is) throws IOException {
        try (NBTInputStream nbtInputStream = new NBTInputStream(new BufferedInputStream(new GZIPInputStream(is)), false)) {
            return (CompoundTag) nbtInputStream.readTag();
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
}
