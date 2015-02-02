package info.jbcs.minecraft.waypoints;

import info.jbcs.minecraft.utilities.packets.PacketHandler;

import java.io.File;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid="Waypoints", name="Waypoints", version="1.0.2")
@NetworkMod(clientSideRequired=true, serverSideRequired=true)
public class Waypoints{
	public static Configuration config;
	public static boolean compactView;
	
	public static BlockWaypoint blockWaypoint;

	int getBlock(String name,int id){
		return config.getBlock(name, id).getInt(id);
	}
	
	@Instance("Waypoints")
	public static Waypoints instance;

	@SidedProxy(clientSide = "info.jbcs.minecraft.waypoints.ProxyClient", serverSide = "info.jbcs.minecraft.waypoints.Proxy")
	public static Proxy proxy;

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		proxy.preInit();
	}
	
	@Init
	public void init(FMLInitializationEvent event) {
		blockWaypoint=(BlockWaypoint) new BlockWaypoint(getBlock("waypoint",2950)).setUnlocalizedName("waypoint").setHardness(2.0F).setResistance(10F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabTransport);
		LanguageRegistry.addName(blockWaypoint, "Waypoint block");
		GameRegistry.registerBlock(blockWaypoint, ItemWaypoint.class, "waypoint");
		
		compactView=config.get("general", "compact view", true, "Only show one line in Waypoint GUI, in order to fit more waypoints on the screen").getBoolean(true);
		
		proxy.init();
		
		Packets.waypointName.create();
		Packets.waypointsMenu.create();
		PacketHandler.register(this);
		
		CraftingManager.getInstance().addRecipe(new ItemStack(blockWaypoint, 1), new Object[] { "***", "*X*", '*', new ItemStack(Block.stone, 1), 'X', new ItemStack(Item.enderPearl, 1)});
		
        MinecraftForge.EVENT_BUS.register(this);
        
        config.save();
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
	}
	
	public File getWorldDir(World world){
		ISaveHandler handler=world.getSaveHandler();
		if(! (handler instanceof SaveHandler)) return null;
		return ((SaveHandler)handler).getWorldDirectory();
	}
	
	@ForgeSubscribe
	public void onSavingWorld(WorldEvent.Save evt){
		File file=getWorldDir(evt.world);
		if(file==null) return;
		
		try {
			Waypoint.write(new File(file,"waypoints.dat"));
			WaypointPlayerInfo.writeAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@ForgeSubscribe
	public void onLoadingWorld(WorldEvent.Load evt){
		File file=getWorldDir(evt.world);
		if(file==null) return;
		
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



