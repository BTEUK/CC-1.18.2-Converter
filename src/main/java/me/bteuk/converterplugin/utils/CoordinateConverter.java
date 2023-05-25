package me.bteuk.converterplugin.utils;

import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;

public class CoordinateConverter {

    private static final EarthGeneratorSettings PROJECTION = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);

    //Coords must be in longitude,latitude format.
    public static double[] toMCCoordinates(Coordinate coords) throws OutOfProjectionBoundsException {
        return PROJECTION.projection().fromGeo(coords.longitude, coords.latitude);
    }

    //Coord must be in x,z format.
    public static Coordinate toIRLCoordinates(int[] coords) throws OutOfProjectionBoundsException {
        double[] coordinate = PROJECTION.projection().toGeo(coords[0], coords[1]);
        return new Coordinate(coordinate[0], coordinate[1]);
    }
}
