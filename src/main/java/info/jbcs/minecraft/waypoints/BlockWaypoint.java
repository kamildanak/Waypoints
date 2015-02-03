package info.jbcs.minecraft.waypoints;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

import java.io.DataOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.lwjgl.Sys;

public class BlockWaypoint extends Block {
	IIcon topIcon,sideIcon;
	IIcon sideIcons[]=new IIcon[2];
	IIcon topIcons[]=new IIcon[4];
	
	public BlockWaypoint() {
		super(Material.rock);
        setBlockName("waypoint");
        this.setCreativeTab(CreativeTabs.tabTransport);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		setLightOpacity(255);
        this.setLightOpacity(0);

        this.setResistance(10F).setStepSound(Blocks.stone.stepSound).setHardness(2.0F);

	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	public boolean isValidWaypoint(World world, int ox, int oy, int oz){
		if(world.getBlock(ox+0,oy,oz+0)!=this) return false;
		if(world.getBlock(ox+1,oy,oz+0)!=this) return false;
		if(world.getBlock(ox+0,oy,oz+1)!=this) return false;
		if(world.getBlock(ox+1,oy,oz+1)!=this) return false;
		if(world.getBlockMetadata(ox+0,oy,oz+0)!=1) return false;
		if(world.getBlockMetadata(ox+1,oy,oz+0)!=2) return false;
		if(world.getBlockMetadata(ox+0,oy,oz+1)!=3) return false;
		if(world.getBlockMetadata(ox+1,oy,oz+1)!=4) return false;
		
		return true;
	}
	

    @Override
	public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMeta){
    	super.breakBlock(world, x, y, z, oldBlock, oldMeta);
    	
		while(world.getBlock(x - 1, y, z)==this) x--;
		while(world.getBlock(x, y, z - 1)==this) z--;
   	
		final Waypoint wp=Waypoint.getWaypoint(x,y,z,world.provider.dimensionId);
		if(wp==null) return;
		
		Waypoint.removeWaypoint(wp);
    }
    
    static public boolean isPlayerOnWaypoint(World world, int x, int y, int z, EntityPlayer player){
		if(player.posX<x) return false;
		if(player.posX>x+2) return false;
		if(player.posZ<z) return false;
		if(player.posZ>z+2) return false;
		
		return true;
    }

	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(world.isRemote) return true;
   	
		while(world.getBlock(x - 1, y, z)==this) x--;
		while(world.getBlock(x, y, z - 1)==this) z--;
		
		if(! isValidWaypoint(world,x,y,z)) return true;
		
		final Waypoint src=Waypoint.getWaypoint(x,y,z,player.dimension);
		if(src==null) return true;

		if(! isPlayerOnWaypoint(world,x,y,z,player)) return true;
		
		if(src.name.isEmpty()){
            int type = 2;
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeInt(type);
            buffer.writeInt(src.id);
            ByteBufUtils.writeUTF8String(buffer, "Waypoint #" + src.id);
            FMLProxyPacket packet = new FMLProxyPacket(buffer.copy(), "Waypoints");

            Waypoints.Channel.sendTo(packet, (EntityPlayerMP) player);
		} else{
            try {
                Packets.sendWaypointsToPlayer((EntityPlayerMP) player, src.id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
		return true;
	}
	
    @Override
	public IIcon getIcon(int side, int meta){
        return side<2?topIcon:sideIcon;
    }

    @Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side){
    	int meta=world.getBlockMetadata(x, y, z);
    	
    	if(meta==0) return this.getIcon(side,0);
    	
    	switch(((meta-1)&3)|(side<<2)){
    	case 0|(0<<2):
    	case 0|(1<<2): return topIcons[1];
    	case 1|(0<<2):
    	case 1|(1<<2): return topIcons[2];
    	case 2|(0<<2):
    	case 2|(1<<2): return topIcons[0];
    	case 3|(0<<2):
    	case 3|(1<<2): return topIcons[3];
    	
    	case 0|(2<<2): return sideIcons[1];
    	case 0|(3<<2): return sideIcons[0];
    	case 1|(2<<2): return sideIcons[0];
    	case 1|(3<<2): return sideIcons[1];
    	case 2|(2<<2): return sideIcons[1];
    	case 2|(3<<2): return sideIcons[0];
    	case 3|(2<<2): return sideIcons[0];
    	case 3|(3<<2): return sideIcons[1];
    	
    	case 0|(4<<2): return sideIcons[0];
    	case 0|(5<<2): return sideIcons[1];
       	case 1|(4<<2): return sideIcons[0];
    	case 1|(5<<2): return sideIcons[1];
    	case 2|(4<<2): return sideIcons[1];
    	case 2|(5<<2): return sideIcons[0];
    	case 3|(4<<2): return sideIcons[1];
    	case 3|(5<<2): return sideIcons[0];
    	}
    	
    	return topIcon;
    }

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

    @Override
	public void registerBlockIcons(IIconRegister reg){
    	topIcon=reg.registerIcon("waypoints:waypoint-top");
        sideIcon=reg.registerIcon("waypoints:waypoint-side");
        topIcons[0]=reg.registerIcon("waypoints:waypoint-top-a");
        topIcons[1]=reg.registerIcon("waypoints:waypoint-top-b");
        topIcons[2]=reg.registerIcon("waypoints:waypoint-top-c");
        topIcons[3]=reg.registerIcon("waypoints:waypoint-top-d");
        sideIcons[0]=reg.registerIcon("waypoints:waypoint-side-a");
        sideIcons[1]=reg.registerIcon("waypoints:waypoint-side-b");
    }

    

}
