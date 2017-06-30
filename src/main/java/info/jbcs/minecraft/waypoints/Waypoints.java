package info.jbcs.minecraft.waypoints;

import com.kamildanak.minecraft.foamflower.gui.GuiHandler;
import info.jbcs.minecraft.waypoints.proxy.Proxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

@Mod(modid = Waypoints.MOD_ID, name = Waypoints.MODNAME, version = Waypoints.VERSION,
        acceptedMinecraftVersions = "[1.12]")
public class Waypoints {
    public static final String MOD_ID = "waypoints";
    public static final String MODNAME = "waypoints";
    public static final String VERSION = "1.12-1.2.4";
    public static boolean compactView;
    public static boolean craftable;
    public static boolean allowActivation;
    public static boolean commonDiscoveryList;
    public static boolean allowWaypointDeletion;
    public static boolean logEvents;
    public static int maxSize;
    public static int minSize;
    public static boolean allowNotSquare;
    public static CreativeTabs tabWaypoints;
    @SidedProxy(clientSide = "info.jbcs.minecraft.waypoints.proxy.ProxyClient", serverSide = "info.jbcs.minecraft.waypoints.proxy.Proxy")
    public static Proxy proxy;
    public static PotionEffect potionEffects[];
    public static int potionEffectsChances[];
    public static int teleportationExhaustion;
    static Configuration config;
    private static Logger logger;
    private File loadedWorldDir;

    public Waypoints() {
    }

    public static void log(String message) {
        if (logEvents) logger.info(message);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger = LogManager.getLogger(Waypoints.MODNAME);
        Waypoints.log("Hello");
        tabWaypoints = CreativeTabs.DECORATIONS;

        proxy.init();
        loadConfigOptions();

        MinecraftForge.EVENT_BUS.register(this);
        config.save();
        proxy.registerPackets();
        GuiHandler.register(this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    public File getWorldDir(World world) {
        ISaveHandler handler = world.getSaveHandler();
        if (!(handler instanceof SaveHandler)) return null;
        return handler.getWorldDirectory();
    }

    @EventHandler
    public void onServerStop(FMLServerStoppingEvent evt) {
        File file = loadedWorldDir;
        if (file == null) return;
        try {
            Waypoint.write(new File(file, "waypoints.json"));
            WaypointPlayerInfo.writeAll();
            Waypoints.log("Saving waypoints data seems successful");
        } catch (IOException e) {
            Waypoints.log("Error saving waypoints data");
            e.printStackTrace();
        }
        loadedWorldDir = null;
        WaypointPlayerInfo.location = null;
    }

    @EventHandler
    public void onLoadingWorld(FMLServerStartingEvent evt) {
        Waypoint.clear();
        WaypointPlayerInfo.clear();

        File file = getWorldDir(evt.getServer().getEntityWorld());
        if (file == null) return;
        loadedWorldDir = file;

        WaypointPlayerInfo.location = new File(file, "waypoints-discovery");
        File waypointsLocation = new File(file, "waypoints.json");

        if (!waypointsLocation.exists()) return;

        try {
            Waypoint.read(waypointsLocation);
            Waypoints.log("Loading waypoints data seems successful");
        } catch (IOException e) {
            e.printStackTrace();
            Waypoints.log("Error loading waypoints data");
        }
    }

    private void loadConfigOptions()
    {
        compactView = config.get("general", "compact view", true,
                "Only show one line in Waypoint GUI, in order to fit more waypoints on the screen").getBoolean();
        craftable = config.get("general", "craftable", true,
                "Set to false to completely disable crafting recipe").getBoolean();
        allowActivation = config.get("general", "can_no_ops_activate", true,
                "If set to false only ops can enable Waypoins").getBoolean();
        maxSize = config.get("general", "max size", 3,
                "Set maximum size of waypoints (default 3)").getInt();
        minSize = config.get("general", "min size", 2,
                "Set minimum size of waypoints (default 2)").getInt();
        allowNotSquare = config.get("general", "allow not square", false,
                "Set to true to allow not square (rectangular) waypoints").getBoolean();
        teleportationExhaustion = config.get("general", "teleportationExhaustion", 20,
                "Exhaustion caused by using waypoint").getInt();
        commonDiscoveryList = config.get("general", "commonDiscoveryList", false,
                "Share discovered Waypoints between players").getBoolean();
        allowWaypointDeletion = config.get("general", "allowWaypointDeletion", true,
                "Allow deleting waypoints from list of discovered waypoints (usefull with commonDiscoveryList")
                .getBoolean();
        logEvents = config.get("general", "logEvents", false,
                "Log events such as waypoint creation, discovery, waypoint lists send to players...").getBoolean();

        // Effects and exhaustion
        int[] effects = config.get("general", "effects ids", new int[]{9, 9, 15},
                "List of effects id").getIntList();
        int[] effectsDurations = config.get("general", "effects durations", new int[]{20, 30, 25},
                "List off durations").getIntList();
        potionEffectsChances = config.get("general", "effects chances", new int[]{30, 10, 5},
                "List of probabilities (0-100)% that effect will appear").getIntList();
        potionEffects = new PotionEffect[Math.min(effects.length, effectsDurations.length)];
        for (int i = 0; i < potionEffects.length; i++)
            potionEffects[i] = new PotionEffect(Potion.getPotionById(effects[i]), effectsDurations[i] * 10);
    }

}



