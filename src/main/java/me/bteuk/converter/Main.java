package me.bteuk.converter;

import java.util.Date;

public class Main {

    //Y / 16 is the cube height
    public static final int MIN_Y_CUBE = -8;
    public static final int MAX_Y_CUBE = 85;

    //Default biome namespace
    public static final String DEFAULT_BIOME = "minecraft:forest";

    //This is where the program will start.
    //This is cli application and thus the arguments will be provided through the args variable.

    /*
    The format of args is:
        args[0]: path of input world file folder
        args[1]: path of output folder
        args[2]: y min
        args[3]: y max
        args[4]: number of threads to use.
     */

    public static void main(String[] args) {

        //Start time.
        Date date = new Date();
        Long start_time = date.getTime();

        if (args.length < 2) {
            System.out.println("You must provide arguments for the input and output folders.");
            System.out.println("Additionally the number of threads can be specified.");
            System.out.println("java -jar CC-1.18.2-Converter.jar <path to input> <path to output> [threads]");
            return;
        }

        int max_threads = 1;

        if (args.length == 3) {
            try {
                max_threads = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                max_threads = 1;
            }
        }

        new WorldIterator(args[0], args[1], max_threads);

        date = new Date();
        Long end_time = date.getTime();

        long durationInMillis = end_time-start_time;

        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        System.out.println("Time Taken: " + time);

    }
}
