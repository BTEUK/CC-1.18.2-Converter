package me.bteuk.converter.cc;

import java.util.Objects;

public class Vector3i {
    public static final Vector3i MIN_VECTOR = new Vector3i(Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2);
    public static final Vector3i MAX_VECTOR = new Vector3i(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);

    private final int x;
    private final int y;
    private final int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vector3i add(Vector3i vector3i) {
        return new Vector3i(
                this.x + vector3i.x,
                this.y + vector3i.y,
                this.z + vector3i.z
        );
    }

    public Vector3i sub(Vector3i vector3i) {
        return new Vector3i(
                this.x - vector3i.x,
                this.y - vector3i.y,
                this.z - vector3i.z
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3i vector3i = (Vector3i) o;
        return x == vector3i.x &&
                y == vector3i.y &&
                z == vector3i.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3i{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
