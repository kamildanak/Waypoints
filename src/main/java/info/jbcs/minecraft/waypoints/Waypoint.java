package info.jbcs.minecraft.waypoints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class Waypoint {
    public static ArrayList<Waypoint> existingWaypoints = new ArrayList<Waypoint>();
    public static boolean changed;
    private static HashMap<String, Waypoint> waypointsLocationMap = new HashMap<String, Waypoint>();
    private static Waypoint waypoints[] = new Waypoint[0x400];
    private static int nextId = 0;
    public int id;
    public BlockPos pos;
    public int dimension;
    public String name;
    public int linked_id;
    public boolean powered;

    public Waypoint() {
        id = -1;
    }

    protected Waypoint(int id) {
        this.id = id;
        waypoints[id] = this;
        changed = true;
        powered = false;
    }

    public Waypoint(ByteBuf stream) throws IOException {
        id = -1;
        read(stream);
    }

    public static String locKey(BlockPos pos, int dimension) {
        return pos.getX() + "|" + pos.getY() + "|" + pos.getZ() + ":" + dimension;
    }

    public static Waypoint getWaypoint(int id) {
        if (id < 0 || id >= waypoints.length)
            return null;

        return waypoints[id];
    }

    public static void removeWaypoint(Waypoint wp) {
        waypoints[wp.id] = null;
        waypointsLocationMap.remove(locKey(wp.pos, wp.dimension));
        existingWaypoints.remove(wp);

        changed = true;
        Waypoints.log("Waypoint :" + wp.name + ": (" + wp.pos.toString() + ") removed");
    }

    public static boolean isWaypoint(World world, BlockPos pos) {
        return isWaypoint(pos, world.provider.getDimension());
    }

    private static boolean isWaypoint(BlockPos pos, int dimension) {
        String key = locKey(pos, dimension);
        return waypointsLocationMap.containsKey(key);
    }

    public static Waypoint getOrMakeWaypoint(World world, BlockPos pos) {
        return getWaypoint(pos, world.provider.getDimension());
    }

    public static Waypoint getWaypoint(World world, BlockPos pos) {
        if (!isWaypoint(world, pos)) return null;
        return getWaypoint(pos, world.provider.getDimension());
    }

    private static Waypoint getWaypoint(BlockPos pos, int dimension) {
        String key = locKey(pos, dimension);
        Waypoint wp = waypointsLocationMap.get(key);

        if (wp == null) {
            int startId = nextId;
            while (waypoints[nextId] != null) {
                nextId = (nextId + 1) % waypoints.length;
                if (nextId == startId) return null;
            }

            wp = new Waypoint();
            wp.initialize(nextId, pos, dimension);
            wp.name = "";
            Waypoints.log("Waypoint :" + wp.name + ": (" + wp.pos.toString() + ") created");
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
            System.out.println(w.pos.getX() + "_" + w.pos.getY() + "_" + w.pos.getZ());
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
        waypoints = new Waypoint[0x400];
        changed = false;

        byte[] bytes = Files.readAllBytes(file.toPath());

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

    public void write(ByteBuf stream) throws IOException {
        stream.writeInt(id);
        stream.writeInt(pos.getX());
        stream.writeInt(pos.getY());
        stream.writeInt(pos.getZ());
        stream.writeInt(dimension);
        ByteBufUtils.writeUTF8String(stream, name);
        stream.writeInt(linked_id);
        stream.writeBoolean(powered);
    }

    void read(ByteBuf stream) throws IOException {
        if (id != -1) return;

        id = stream.readInt();
        pos = new BlockPos(stream.readInt(), stream.readInt(), stream.readInt());
        dimension = stream.readInt();
        name = ByteBufUtils.readUTF8String(stream);
        linked_id = stream.readInt();
        powered = stream.readBoolean();
    }

    void write(NBTTagCompound tag) {
        tag.setInteger("id", id);
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
        tag.setInteger("dim", dimension);
        tag.setString("name", name);
        tag.setInteger("linked_id", linked_id);
        tag.setBoolean("powered", powered);
    }

    void read(NBTTagCompound tag) {
        if (id != -1) return;

        initialize(tag.getInteger("id"), new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")),
                tag.getInteger("dim"));

        name = tag.getString("name");
        linked_id = tag.getInteger("linked_id");
        powered = tag.getBoolean("powered");
    }

    void initialize(int id, BlockPos pos, int dimension) {
        String key = locKey(pos, dimension);

        this.id = id;
        waypoints[id] = this;
        this.pos = pos;
        this.dimension = dimension;
        waypointsLocationMap.put(key, this);
        existingWaypoints.add(this);
        changed = true;
    }

    public static void clear() {
        existingWaypoints.clear();
        waypointsLocationMap.clear();
        nextId = 0;
        waypoints = new Waypoint[0x400];
        changed = false;
    }

}
