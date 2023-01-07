package me.bteuk.converter;

import me.bteuk.converter.cc.WorldIterator;

import java.util.Date;

public class Main {

    //Y / 16 is the cube height
    public static final int MIN_Y_CUBE = -8;
    public static final int MAX_Y_CUBE = 85;

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

        //Start time.
        Date date = new Date();
        Long start_time = date.getTime();

        new WorldIterator(args[0], args[1]);

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
