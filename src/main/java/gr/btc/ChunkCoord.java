package gr.btc;

import java.io.Serializable;
import java.util.Objects;

public class ChunkCoord implements Serializable {
    private int x;
    private int z;
    private String world;

    public ChunkCoord(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    // Getters and setters for the properties

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    // Override equals and hashCode methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkCoord that = (ChunkCoord) o;
        return x == that.x && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, world);
    }
}
