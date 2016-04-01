package info.jbcs.minecraft.waypoints;

import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import info.jbcs.minecraft.waypoints.gui.GuiHandler;
import info.jbcs.minecraft.waypoints.item.ItemWaypoint;
import info.jbcs.minecraft.waypoints.proxy.Proxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;
import java.io.IOException;

import static net.minecraftforge.fml.common.registry.GameRegistry.addRecipe;

@Mod(modid = Waypoints.MODID, name = Waypoints.MODNAME, version = Waypoints.VERSION)
public class Waypoints {
    public static final String MODID = "Waypoints";
    public static final String MODNAME = "Waypoints";
    public static final String VERSION = "1.8.9-1.2.0";
    public static boolean compactView;
    public static boolean craftable;
    public static boolean allowActivation;
    public static boolean playSounds;
    public static boolean playSoundEnderman;
    public static int maxSize;
    public static int minSize;
    public static boolean allowNotSquare;
    public static BlockWaypoint blockWaypoint;
    @Mod.Instance("Waypoints")
    public static Waypoints instance;
    public static CreativeTabs tabWaypoints;
    @SidedProxy(clientSide = "info.jbcs.minecraft.waypoints.proxy.ProxyClient", serverSide = "info.jbcs.minecraft.waypoints.proxy.Proxy")
    public static Proxy proxy;
    static Configuration config;
    private File loadedWorldDir;

    public static PotionEffect potionEffects[];
    public static int potionEffectsChances[];
    public static int teleportationExhaustion;

    public Waypoints() {
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        tabWaypoints = CreativeTabs.tabDecorations;

        blockWaypoint = new BlockWaypoint();
        GameRegistry.registerBlock(blockWaypoint, ItemWaypoint.class, "waypoint");
        proxy.init();
        compactView = config.get("general", "compact view", true, "Only show one line in Waypoint GUI, in order to fit more waypoints on the screen").getBoolean();
        craftable = config.get("general", "craftable", true, "Set to false to completely disable crafting recipe").getBoolean();
        if (craftable)
            addRecipe(new ItemStack(blockWaypoint, 1), "SSS", "SES", 'S', Blocks.stone, 'E', Items.ender_pearl);
        allowActivation = config.get("general", "can_no_ops_activate", true, "If set to false only ops can enable Waypoins").getBoolean();
        playSounds = config.get("general", "play sounds", true, "Set to false to disable teleportation sounds").getBoolean();
        playSoundEnderman = config.get("general", "play sound enderman", true, "Set to false to play custom sound").getBoolean();
        maxSize = config.get("general", "max size", 3, "Set maximum size of waypoints (default 3)").getInt();
        minSize = config.get("general", "min size", 2, "Set minimum size of waypoints (default 2)").getInt();
        allowNotSquare = config.get("general", "allow not square", false, "Set to true to allow not square (rectangular) waypoints").getBoolean();
        // Effects and exhaustion
        teleportationExhaustion = config.get("general", "teleportationExhaustion", 20, "Exhaustion caused by using waypoint").getInt();
        int[] effects = config.get("general", "effects ids", new int[]{9, 9, 15}, "List of effectts id").getIntList();
        int[] effectsDurations = config.get("general", "effects durations", new int[]{20, 30, 25}, "List off durations").getIntList();
        potionEffectsChances = config.get("general", "effects chances", new int[]{30, 10, 5}, "List of probabilities (0-100)% that effect will appear").getIntList();
        potionEffects = new PotionEffect[Math.min(effects.length, effectsDurations.length)];
        for (int i = 0; i < potionEffects.length; i++)
            potionEffects[i] = new PotionEffect(effects[i], effectsDurations[i] * 10);
        //
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
        return ((SaveHandler) handler).getWorldDirectory();
    }

    @EventHandler
    public void onServerStop(FMLServerStoppingEvent evt) {
        File file = loadedWorldDir;
        if (file == null) return;
        try {
            Waypoint.write(new File(file, "waypoints.dat"));
            WaypointPlayerInfo.writeAll();
        } catch (IOException e) {
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
        File waypointsLocation = new File(file, "waypoints.dat");

        if (!waypointsLocation.exists()) return;

        try {
            Waypoint.read(waypointsLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



