package me.bteuk.converterplugin.utils;

import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;

public class Region {

    private int x;
    private int z;

    public Region(int x, int z) {
        this.x = x;
        this.z = z;
    }

    //Get the region bounds as coordinates.
    public Coordinate[] getBounds() throws OutOfProjectionBoundsException {

        return new Coordinate[]{
                CoordinateConverter.toIRLCoordinates(new int[]{x,z}),
                CoordinateConverter.toIRLCoordinates(new int[]{x+511,z+511})
        };
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Region r)) {
            return false;
        }

        return equals(r.getX(), r.getZ());

    }

    public boolean equals(int x, int z) {
        return (x == getX() && z == getZ());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
