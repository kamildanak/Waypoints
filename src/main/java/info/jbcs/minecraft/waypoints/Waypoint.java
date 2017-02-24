package info.jbcs.minecraft.waypoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Waypoint {
    public static ArrayList<Waypoint> existingWaypoints = new ArrayList<>();
    public static boolean changed;
    private static HashMap<String, Waypoint> waypointsLocationMap = new HashMap<>();
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
        this();
        read(stream);
    }

    public Waypoint(JsonObject jsonObject) throws IOException {
        this();
        read(jsonObject);
    }

    private static String locKey(BlockPos pos, int dimension) {
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

        JsonArray jsonArray = new JsonArray();
        for (Waypoint w : existingWaypoints) {
            JsonObject jsonObject = new JsonObject();
            w.write(jsonObject);
            jsonArray.add(jsonObject);
            System.out.println(w.pos.getX() + "_" + w.pos.getY() + "_" + w.pos.getZ());
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            String str = jsonArray.toString();
            fileWriter.write(str);
        }
    }

    public static void read(File file) throws IOException {
        existingWaypoints.clear();
        waypointsLocationMap.clear();
        waypoints = new Waypoint[0x400];
        changed = false;
        nextId = 0;

        JsonParser jsonParser = new JsonParser();
        try {
            JsonArray jsonArray = jsonParser.parse(new FileReader(file)).getAsJsonArray();
            for (JsonElement jsonWaypoint : jsonArray)
            {
                Waypoint w = new Waypoint(jsonWaypoint.getAsJsonObject());
                if (nextId <= w.id) nextId = w.id + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(JsonObject jsonObject) throws IOException {
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("x", pos.getX());
        jsonObject.addProperty("y", pos.getY());
        jsonObject.addProperty("z", pos.getZ());
        jsonObject.addProperty("dimension", dimension);
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("linked_id", linked_id);
        jsonObject.addProperty("powered", powered);
    }

    private void read(JsonObject jsonObject) throws IOException {
        if (id != -1) return;

        initialize(jsonObject.get("id").getAsInt(),
                new BlockPos(jsonObject.get("x").getAsInt(),
                        jsonObject.get("y").getAsInt(),
                        jsonObject.get("z").getAsInt()),
                jsonObject.get("dimension").getAsInt());

        name = jsonObject.get("name").getAsString();
        linked_id = jsonObject.get("linked_id").getAsInt();
        powered = jsonObject.get("powered").getAsBoolean();
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

    private void read(ByteBuf stream) throws IOException {
        if (id != -1) return;

        id = stream.readInt();
        pos = new BlockPos(stream.readInt(), stream.readInt(), stream.readInt());
        dimension = stream.readInt();
        name = ByteBufUtils.readUTF8String(stream);
        linked_id = stream.readInt();
        powered = stream.readBoolean();
    }

    private void initialize(int id, BlockPos pos, int dimension) {
        String key = locKey(pos, dimension);

        this.id = id;
        waypoints[id] = this;
        this.pos = pos;
        this.dimension = dimension;
        waypointsLocationMap.put(key, this);
        existingWaypoints.add(this);
        changed = true;
    }

    static void clear() {
        existingWaypoints.clear();
        waypointsLocationMap.clear();
        nextId = 0;
        waypoints = new Waypoint[0x400];
        changed = false;
    }

}
