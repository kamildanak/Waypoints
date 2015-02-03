package info.jbcs.minecraft.waypoints;

import java.io.File;
import java.io.IOException;

import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.Mod.EventHandler;

@Mod(modid="Waypoints", name="Waypoints", version="1.1.0")
public class Waypoints{
    static Configuration config;
	public static boolean compactView;
    public static FMLEventChannel Channel;
	
	public static BlockWaypoint blockWaypoint;


	public static Waypoints instance;
    public static CreativeTabs	tabWaypoints;
    private File loadedWorldDir;

	@SidedProxy(clientSide = "info.jbcs.minecraft.waypoints.ProxyClient", serverSide = "info.jbcs.minecraft.waypoints.Proxy")
	public static Proxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		proxy.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("Waypoints");
        Waypoints.Channel.register(new ServerPacketHandler());
        proxy.init();

        tabWaypoints = CreativeTabs.tabDecorations;

		blockWaypoint = new BlockWaypoint();
        GameRegistry.registerBlock(blockWaypoint, ItemWaypoint.class, "waypoint");
		
		compactView=config.get("general", "compact view", true, "Only show one line in Waypoint GUI, in order to fit more waypoints on the screen").getBoolean(true);

		CraftingManager.getInstance().addRecipe(new ItemStack(blockWaypoint, 1),
                new Object[] { "***", "*X*",
                        '*', new ItemStack(Blocks.stone, 1),
                        'X', new ItemStack(Items.ender_pearl, 1)});

        MinecraftForge.EVENT_BUS.register(this);
        config.save();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
	
	public File getWorldDir(World world){
		ISaveHandler handler=world.getSaveHandler();
		if(! (handler instanceof SaveHandler)) return null;
		return ((SaveHandler)handler).getWorldDirectory();
	}

    @EventHandler
    public void onServerStop(FMLServerStoppingEvent evt) {
        File file = loadedWorldDir;
		if(file==null) return;
		
		try {
			Waypoint.write(new File(file,"waypoints.dat"));
			WaypointPlayerInfo.writeAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onLoadingWorld(FMLServerStartingEvent evt){
		File file=getWorldDir(evt.getServer().getEntityWorld());
		if(file==null) return;
        loadedWorldDir =file;

		WaypointPlayerInfo.location=new File(file,"waypoints-discovery");
		File waypointsLocation=new File(file,"waypoints.dat");
		
		if(! waypointsLocation.exists()) return;
		
		try {
			Waypoint.read(waypointsLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}



