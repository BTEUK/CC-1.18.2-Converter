package me.bteuk.converter;

import java.util.ArrayList;
import java.util.List;

public class RegionTask {
    private boolean isEnd = false;
    private String region = "";

    public static RegionTask regionTask(String region){
        RegionTask regionTask = new RegionTask();
        regionTask.region = region;
        return regionTask;
    }

    public static RegionTask endTask(){
        RegionTask regionTask = new RegionTask();
        regionTask.isEnd = true;
        return regionTask;
    }

    public boolean isEndTask(){ return isEnd; }

    public String getRegion(){ return region; }
}
