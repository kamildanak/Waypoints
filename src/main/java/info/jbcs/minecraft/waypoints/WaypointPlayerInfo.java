package info.jbcs.minecraft.waypoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class WaypointPlayerInfo {
    private static HashMap<String, WaypointPlayerInfo> objects = new HashMap<>();
    static File location;

    public HashMap<Integer, Integer> discoveredWaypoints = new HashMap<>();
    private String username;
    private boolean changed = false;

    private WaypointPlayerInfo(String n) {
        username = n.replaceAll("[^\\p{L}\\p{Nd}_]", "");
    }

    public static WaypointPlayerInfo get(String nn) {
        if(Waypoints.commonDiscoveryList) nn = "WaypointsCommonDiscoveryList";
        WaypointPlayerInfo info = objects.get(nn);
        if (info != null) return info;

        if (location == null) return null;
        //noinspection ResultOfMethodCallIgnored
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

    static void writeAll() throws IOException {
        for (WaypointPlayerInfo info : objects.values()) {
            if (info.changed)
                info.write(info.getFile());
        }
    }

    private File getFile() {
        return new File(location, username + ".dat");
    }

    public void addWaypoint(int id) {
        Waypoints.log("User " + username + " learned about Waypoint #" + id);
        discoveredWaypoints.put(id, 1);
        changed = true;
    }

    public void removeWaypoint(int id) {
        Waypoints.log("User " + username + " forgot about Waypoint #" + id);
        discoveredWaypoints.remove(id);
        changed = true;
    }

    private void read(File file) throws IOException {
        changed = false;
        discoveredWaypoints.clear();
        int count = 0;

        JsonParser jsonParser = new JsonParser();
        try {
            JsonArray jsonArray = jsonParser.parse(new FileReader(file)).getAsJsonArray();
            for (JsonElement jsonWaypoint : jsonArray)
            {
                int id = jsonWaypoint.getAsJsonObject().get("id").getAsInt();
                addWaypoint(id);
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Waypoints.log("User " + username + " read about " + count + " Waypoints");
    }

    private void write(File file) throws IOException {
        JsonArray jsonArray = new JsonArray();
        Set<Integer> keys = discoveredWaypoints.keySet();
        for (Integer id : keys) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", id);
            jsonArray.add(obj);
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            String str = jsonArray.toString();
            fileWriter.write(str);
        }
        Waypoints.log("User " + username + " wrote down about " + keys.size() + " Waypoints");
        changed = false;
    }

    static void clear() {
        WaypointPlayerInfo.location = null;
        WaypointPlayerInfo.objects.clear();
    }
}
