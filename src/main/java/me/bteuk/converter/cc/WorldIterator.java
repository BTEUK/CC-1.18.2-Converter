package me.bteuk.converter.cc;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import cubicchunks.regionlib.api.region.IRegionProvider;
import cubicchunks.regionlib.api.region.key.IKey;
import cubicchunks.regionlib.api.region.key.RegionKey;
import cubicchunks.regionlib.impl.EntryLocation2D;
import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.SaveCubeColumns;
import cubicchunks.regionlib.impl.save.SaveSection2D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cubicchunks.regionlib.lib.ExtRegion;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import me.bteuk.converter.Main;

import javax.swing.text.html.Option;
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

        //Create 2d and 3d region provider.
        EntryLocation2D.Provider provider2d = new EntryLocation2D.Provider();
        EntryLocation3D.Provider provider3d = new EntryLocation3D.Provider();

        for (String sFile : files) {

            String[] split = sFile.split("\\.");

            //Iterate through all possible chunk columns in the file and continue with any that contain data.
            //A region2d is 512x512 which is 32*32 in terms of chunks, so 1024 individual chunks to iterate.
            for (int i = 0; i < 1024; i++) {

                RegionKey key = new RegionKey(sFile);
                EntryLocation2D e2d = provider2d.fromRegionAndId(key, i);

                try {

                    //Gets the data from the 2dr file.
                    Optional<ByteBuffer> column = saveCubeColumns.load(e2d, true);

                    //If the columndata is not empty
                    if (!column.isEmpty()) {

                        //Get all cubes that could be in the range of heights.
                        for (int y = Main.MIN_Y_CUBE; y < Main.MAX_Y_CUBE; y++) {

                            EntryLocation3D e3d = new EntryLocation3D(e2d.getEntryX(), y, e2d.getEntryZ());
                            System.out.println(e3d.getRegionKey().getName() + ", " + e3d.getId());

                            //Get the cube of data.
                            Optional<ByteBuffer> cube = saveCubeColumns.load(e3d,true);

                            //Create list of sections to store the new sections in.
                            List<CompoundTag> sections = new ArrayList<>();

                            //Check if it exists.
                            if (!cube.isEmpty()) {

                                //Retrieve the data from the cube.
                                CompoundTag cubeTag = cube.get() == null ? null : readCompressedCC(new ByteArrayInputStream(cube.get().array()));
                                CompoundTag cubeLevel = (CompoundTag) cubeTag.getValue().get("Level");

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

    public CompoundTag readCompressedCC(InputStream is) throws IOException {
        try (NBTInputStream nbtInputStream = new NBTInputStream(new BufferedInputStream(new GZIPInputStream(is)), false)) {
            return (CompoundTag) nbtInputStream.readTag();
        }
    }

}
