package info.jbcs.minecraft.waypoints;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class Waypoint {
    public int id;
    public int x, y, z, dimension;
    public String name;
    public int linked_id;
    public boolean powered;

    static HashMap<String, Waypoint> waypointsLocationMap = new HashMap<String, Waypoint>();
    static Waypoint waypoints[] = new Waypoint[0x400];
    static int nextId = 0;
    public static ArrayList<Waypoint> existingWaypoints = new ArrayList<Waypoint>();
    public static boolean changed;

    public Waypoint() {
        id = -1;
    }

    protected Waypoint(int id) {
        this.id = id;
        waypoints[id] = this;
        changed = true;
    }

    public Waypoint(ByteBuf stream) throws IOException {
        id = -1;
        read(stream);
    }

    public void write(ByteBuf stream) throws IOException {
        stream.writeInt(id);
        stream.writeInt(x);
        stream.writeInt(y);
        stream.writeInt(z);
        stream.writeInt(dimension);
        ByteBufUtils.writeUTF8String(stream, name);
        stream.writeInt(linked_id);
        stream.writeBoolean(powered);
    }

    void read(ByteBuf stream) throws IOException {
        if (id != -1) return;

        id = stream.readInt();
        x = stream.readInt();
        y = stream.readInt();
        z = stream.readInt();
        dimension = stream.readInt();
        name = ByteBufUtils.readUTF8String(stream);
        linked_id = stream.readInt();
        powered = stream.readBoolean();
    }

    void write(NBTTagCompound tag) {
        tag.setInteger("id", id);
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        tag.setInteger("dim", dimension);
        tag.setString("name", name);
        tag.setInteger("linked_id", linked_id);
        tag.setBoolean("powered", powered);
    }

    void read(NBTTagCompound tag) {
        if (id != -1) return;

        initialize(tag.getInteger("id"), tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getInteger("dim"));
        name = tag.getString("name");
        linked_id = tag.getInteger("linked_id");
        powered = tag.getBoolean("powered");
    }

    public static String locKey(int x, int y, int z, int dimension) {
        return x + "|" + y + "|" + z + ":" + dimension;
    }

    public static Waypoint getWaypoint(int id) {
        if (id < 0 || id >= waypoints.length)
            return null;

        return waypoints[id];
    }

    public static void removeWaypoint(Waypoint wp) {
        waypoints[wp.id] = null;
        waypointsLocationMap.remove(locKey(wp.x, wp.y, wp.z, wp.dimension));
        existingWaypoints.remove(wp);

        changed = true;
    }

    void initialize(int id, int x, int y, int z, int dimension) {
        String key = locKey(x, y, z, dimension);

        this.id = id;
        waypoints[id] = this;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        waypointsLocationMap.put(key, this);
        existingWaypoints.add(this);
        changed = true;
    }

    public static Waypoint getWaypoint(int x, int y, int z, int dimension) {
        String key = locKey(x, y, z, dimension);
        Waypoint wp = waypointsLocationMap.get(key);

        if (wp == null) {
            int startId = nextId;
            while (waypoints[nextId] != null) {
                nextId = (nextId + 1) % waypoints.length;
                if (nextId == startId) return null;
            }

            wp = new Waypoint();
            wp.initialize(nextId, x, y, z, dimension);
            wp.name = "";
        }

        return wp;
    }

    public static void write(File file) throws IOException {
        if (!changed) return;
        changed = false;

        int index = 0;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("count", existingWaypoints.size());
        for (Waypoint w : existingWaypoints) {
            NBTTagCompound wtag = new NBTTagCompound();
            w.write(wtag);
            tag.setTag("" + (index++), wtag);
        }

        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeTag(buffer, tag);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        Files.write(file.toPath(), bytes);
    }

    public static void read(File file) throws IOException {
        existingWaypoints.clear();
        waypointsLocationMap.clear();
        nextId = 0;


        byte[] bytes = Files.readAllBytes(file.toPath());
        ;
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bytes);
        NBTTagCompound tag = ByteBufUtils.readTag(buffer);

        int count = tag.getInteger("count");
        for (int i = 0; i < count; i++) {
            Waypoint w = new Waypoint();
            w.read(tag.getCompoundTag("" + i));
            if (nextId <= w.id) nextId = w.id + 1;
        }
    }
}
