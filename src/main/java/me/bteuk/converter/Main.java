package me.bteuk.converter;

import cubicchunks.regionlib.impl.EntryLocation2D;
import me.bteuk.converter.cc.CubicChunkReader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    //This is where the program will start.
    //This is cli application and thus the arguments will be provided through the args variable.

    /*
    The format of args is:
        args[0]: path of input world file folder
        args[1]: path of output folder
        args[2]: y min
        args[3]: y max
     */

    public static void main(String[] args) {

        System.out.println(new EntryLocation2D(10,0).getId());

    }
}
