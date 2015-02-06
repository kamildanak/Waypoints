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
	IIcon sideIcons[]=new IIcon[3];
	IIcon topIcons[]=new IIcon[9];
	
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
        if(checkSize(world, ox, oy, oz)==1) return false;
		if(world.getBlock(ox,oy,oz)!=this) return false; //checkSize do not check this one
        //here we can check metadata it _should_ be always true so let omit this until some bug happen
		
		return true;
	}
    @Override
	public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMeta){
        super.breakBlock(world, x, y, z, oldBlock, oldMeta);
        int size = checkSize(world, x, y, z);
		while(world.getBlock(x - 1, y, z)==this) x--;
		while(world.getBlock(x, y, z - 1)==this) z--;

		final Waypoint wp=Waypoint.getWaypoint(x,y,z,world.provider.dimensionId);


        for(int px=0; px<size; px++){
            for(int pz=0; pz<size; pz++){
                world.setBlockMetadataWithNotify(x+px,y,z+pz,0,3);
            }
        }

		if(wp==null) return;
        Waypoint.removeWaypoint(wp);
    }
    
    static public boolean isPlayerOnWaypoint(World world, int x, int y, int z, EntityPlayer player){
        int size = checkSize(world, x, y, z);
        if(player.posX<x) return false;
		if(player.posX>x+size) return false;
		if(player.posZ<z) return false;
		if(player.posZ>z+size) return false;
		
		return true;
    }

	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(world.isRemote) return true;

		while(world.getBlock(x - 1, y, z)==this) x--;
		while(world.getBlock(x, y, z - 1)==this) z--;

        activateStructure(world, x, y, z);

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
    static public int checkSize(World world, int ox, int oy, int oz){
        int x = ox;
        int z = oz;
        while(world.getBlock(ox - 1, oy, oz)==Waypoints.blockWaypoint) ox--;
        while(world.getBlock(ox, oy, oz - 1)==Waypoints.blockWaypoint) oz--;
        outerloop:
        for(int px=0; px<3; px++){
            for(int pz=0; pz<3; pz++){
                if((world.getBlock(ox+px, oy, oz+pz)==Waypoints.blockWaypoint || (ox+px==x && oz+pz==z)) && px==2 && pz==2) {
                    return 3;
                }
                if(world.getBlock(ox+px, oy, oz+pz)!=Waypoints.blockWaypoint && !((ox+px)==x && (oz+pz)==z)) {
                    break outerloop;
                }
            }
        }
        outerloop:
        for(int px=0; px<2; px++){
            for(int pz=0; pz<2; pz++){
                if((world.getBlock(ox+px, oy, oz+pz)==Waypoints.blockWaypoint || (ox+px==x && oz+pz==z)) && px==1 && pz==1) {
                    return 2;
                }
                if(world.getBlock(ox+px, oy, oz+pz)!=Waypoints.blockWaypoint && !((ox+px)==x && (oz+pz)==z)) {
                    break outerloop;
                }
            }
        }
        return 1;
    }

    public void activateStructure(World world, int ox, int oy, int oz){
        if(checkSize(world, ox, oy, oz)==3 && world.getBlock(ox, oy, oz)==this){
            world.setBlockMetadataWithNotify(ox+0,oy,oz+0,1,3);
            world.setBlockMetadataWithNotify(ox+1,oy,oz+0,2,3);
            world.setBlockMetadataWithNotify(ox+2,oy,oz+0,3,3);
            world.setBlockMetadataWithNotify(ox+2,oy,oz+1,4,3);
            world.setBlockMetadataWithNotify(ox+2,oy,oz+2,5,3);
            world.setBlockMetadataWithNotify(ox+1,oy,oz+2,6,3);
            world.setBlockMetadataWithNotify(ox+0,oy,oz+2,7,3);
            world.setBlockMetadataWithNotify(ox+0,oy,oz+1,8,3);
            world.setBlockMetadataWithNotify(ox+1,oy,oz+1,9,3);
        }
        ox--;
        oz--;
        for(int xp=0; xp<4; xp++){
            for(int zp=0; zp<4; zp++){
                if((xp==0 | xp==3 | zp==0 | zp==3)&& xp!=zp && !(xp==0 && zp ==3) && !(xp==3 && zp==0)){
                    if(world.getBlock(ox+xp,oy,oz+zp)==this) return;
                }else if(!(xp==0 | xp==3 | zp==0 | zp==3)){
                    if(world.getBlock(ox+xp,oy,oz+zp)!=this) return;
                }
            }
        }
        world.setBlockMetadataWithNotify(ox+1,oy,oz+1,1,3);
        world.setBlockMetadataWithNotify(ox+2,oy,oz+1,3,3);
        world.setBlockMetadataWithNotify(ox+2,oy,oz+2,5,3);
        world.setBlockMetadataWithNotify(ox+1,oy,oz+2,7,3);
    }
	
    @Override
	public IIcon getIcon(int side, int meta){
        return side<2?topIcon:sideIcon;
    }

    @Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side){
    	int meta=world.getBlockMetadata(x, y, z);
    	
    	if(meta==0) return this.getIcon(side,0);
    	
    	switch(side){

            case 0:
            case 1:
                return topIcons[(meta-1)%9];
            case 2:
            case 3:
                switch((meta-1)%9){
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                        return sideIcons[2];
                    case 0:
                    case 4:
                        return sideIcons[1];
                    case 2:
                    case 6:
                        return sideIcons[0];
                }
            case 4:
            case 5:
                switch((meta-1)%9){
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                        return sideIcons[2];
                    case 0:
                    case 4:
                        return sideIcons[0];
                    case 2:
                    case 6:
                        return sideIcons[1];
                }
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
        topIcons[6]=reg.registerIcon("waypoints:waypoint-top-a");
        topIcons[7]=reg.registerIcon("waypoints:waypoint-top-ab");
        topIcons[0]=reg.registerIcon("waypoints:waypoint-top-b");
        topIcons[1]=reg.registerIcon("waypoints:waypoint-top-bc");
        topIcons[2]=reg.registerIcon("waypoints:waypoint-top-c");
        topIcons[3]=reg.registerIcon("waypoints:waypoint-top-cd");
        topIcons[4]=reg.registerIcon("waypoints:waypoint-top-d");
        topIcons[5]=reg.registerIcon("waypoints:waypoint-top-da");
        topIcons[8]=reg.registerIcon("waypoints:waypoint-top-m");
        sideIcons[0]=reg.registerIcon("waypoints:waypoint-side-a");
        sideIcons[1]=reg.registerIcon("waypoints:waypoint-side-b");
        sideIcons[2]=reg.registerIcon("waypoints:waypoint-side-m");
    }

    

}
