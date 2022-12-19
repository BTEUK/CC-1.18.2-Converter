package me.bteuk.converter.cc;

import cubicchunks.regionlib.api.region.key.IKey;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorldIterator {

    /*

    Iterates through all .2dr (512x512 files)

     */

    public void WorldIterator(String path) {

        //Get the path from the String.
        Path file = Paths.get(path);

        //Check if file exists.
        if (!Files.exists(file)) {
            return;
        }

        Path region2d = file.resolve("region2d");

    }

}
