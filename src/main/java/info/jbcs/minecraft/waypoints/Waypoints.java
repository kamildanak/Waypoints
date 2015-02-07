package info.jbcs.minecraft.waypoints;

import java.io.File;
import java.io.IOException;

import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
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
    public static String default_recipe = "3x2,minecraft:stone:1,minecraft:stone:1,minecraft:stone:1,minecraft:stone:1,minecraft:ender_pearl:1,minecraft:stone:1";
    public static String recipe;
    public static boolean craftable;
    public static boolean allowActivation;
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
		
		compactView=config.get("general", "compact view", true, "Only show one line in Waypoint GUI, in order to fit more waypoints on the screen").getBoolean();
        recipe=config.get("general", "recipe", default_recipe, "You can change crafting recipe here").getString();
        craftable=config.get("general", "craftable", true, "Set to false to completely disable crafting recipe").getBoolean();
        if(craftable) addRecipe(new ItemStack(blockWaypoint, 1), recipe);
        allowActivation=config.get("general", "can_no_ops_activate", true, "If set to false only ops can enable Waypoins").getBoolean();
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
    public void addRecipe(ItemStack itemStack, String string){
        String[] string_array = string.split(",");
        String[] itemstr;
        ItemStack[] itemstack_array = new ItemStack[9];
        int recipe_column = Integer.parseInt(string_array[0].split("x")[0]);
        int recipe_row = Integer.parseInt(string_array[0].split("x")[1]);
        String a="",b="",c = "";
        for(int i=0; i<recipe_row; i++){
            for(int j=0; j<recipe_column; j++){
                itemstr = string_array[i * recipe_column + j + 1].split(":");
                if(!(itemstr.length<3)) {
                    itemstack_array[i * recipe_column + j] = GameRegistry.findItemStack(itemstr[0], itemstr[1], Integer.parseInt(itemstr[2]));
                    if(i==0) a+= Character.toString ((char) (i*recipe_column+j+65));
                    if(i==1) b+= Character.toString ((char) (i*recipe_column+j+65));
                    if(i==2) c+= Character.toString ((char) (i*recipe_column+j+65));
                }else{
                    itemstack_array[i * recipe_column + j] = null;
                    if(i==0) a+= " ";
                    if(i==1) b+= " ";
                    if(i==2) c+= " ";
                }
            }
        }
        if(recipe_row==1)
            CraftingManager.getInstance().addRecipe(itemStack,
                    a,
                    (char) 65, itemstack_array[0],
                    (char) 66, itemstack_array[1],
                    (char) 67, itemstack_array[2]
            );
        if(recipe_row==2)
            CraftingManager.getInstance().addRecipe(itemStack,
                    a, b,
                    (char) 65, itemstack_array[0],
                    (char) 66, itemstack_array[1],
                    (char) 67, itemstack_array[2],
                    (char) 68, itemstack_array[3],
                    (char) 69, itemstack_array[4],
                    (char) 70, itemstack_array[5]
            );
        if(recipe_row==3)
            CraftingManager.getInstance().addRecipe(itemStack,
                    a, b, c,
                    (char) 65, itemstack_array[0],
                    (char) 66, itemstack_array[1],
                    (char) 67, itemstack_array[2],
                    (char) 68, itemstack_array[3],
                    (char) 69, itemstack_array[4],
                    (char) 70, itemstack_array[5],
                    (char) 71, itemstack_array[6],
                    (char) 72, itemstack_array[7],
                    (char) 73, itemstack_array[8]
            );
    }

}



