package info.jbcs.minecraft.waypoints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;

public class WaypointPlayerInfo {
    static HashMap<String, WaypointPlayerInfo> objects = new HashMap<String, WaypointPlayerInfo>();
    static File location;

    public HashMap<Integer, Integer> discoveredWaypoints = new HashMap<Integer, Integer>();
    String username;
    boolean changed = false;

    WaypointPlayerInfo(String n) {
        username = n.replaceAll("[^\\p{L}\\p{Nd}_]", "");
    }

    public static WaypointPlayerInfo get(String nn) {
        WaypointPlayerInfo info = objects.get(nn);
        if (info != null) return info;

        if (location == null) return null;
        location.mkdirs();

        info = new WaypointPlayerInfo(nn);
        objects.put(nn, info);

        File file = info.getFile();
        if (!file.exists()) return info;

        try {
            info.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return info;
    }

    public static void writeAll() throws IOException {
        for (WaypointPlayerInfo info : objects.values()) {
            if (info.changed)
                info.write(info.getFile());
        }
    }

    File getFile() {
        return new File(location, username + ".dat");
    }

    public void addWaypoint(int id) {
        discoveredWaypoints.put(id, 1);
        changed = true;
    }

    public void removeWaypoint(int id) {
        discoveredWaypoints.remove(id);
        changed = true;
    }

    private void read(File file) throws IOException {
        changed = false;
        objects.clear();
        discoveredWaypoints.clear();

        byte[] bytes = Files.readAllBytes(file.toPath());
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bytes);
        NBTTagCompound tag = ByteBufUtils.readTag(buffer);

        int count = tag.getInteger("count");
        for (int i = 0; i < count; i++) {
            int id = tag.getInteger("" + i);
            addWaypoint(id);
        }
    }

    private void write(File file) throws IOException {
        int index = 0;


        NBTTagCompound tag = new NBTTagCompound();
        Set<Integer> keys = discoveredWaypoints.keySet();
        tag.setInteger("count", keys.size());
        for (Integer id : keys) {
            tag.setInteger("" + (index++), id);
        }
        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeTag(buffer, tag);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        Files.write(file.toPath(), bytes);

        changed = false;
    }
}
