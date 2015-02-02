package info.jbcs.minecraft.waypoints;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemWaypoint extends ItemBlock {
	int blockId;
	
	public ItemWaypoint(int id) {
		super(id);
		blockId=id+256;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
		int id = world.getBlockId(x, y, z);

		if (id != Block.vine.blockID && id != Block.tallGrass.blockID && id != Block.deadBush.blockID && (Block.blocksList[id] == null || !Block.blocksList[id].isBlockReplaceable(world, x, y, z))) {
			switch(side){
			case 0: y--; break;
			case 1: y++; break;
			case 2: z--; break;
			case 3: z++; break;
			case 4: x--; break;
			case 5: x++; break;
			}
		}

		if (stack.stackSize == 0) {
			return false;
		} else if (!player.canPlayerEdit(x, y, z, side, stack)) {
			return false;
		} else if (y == 255 && Block.blocksList[blockId].blockMaterial.isSolid()) {
			return false;
		} else if (world.canPlaceEntityOnSide(blockId, x, y, z, false, side, player, stack)) {
			boolean exp=world.getBlockId(x+1, y, z)==blockId;
			boolean exm=world.getBlockId(x-1, y, z)==blockId;
			boolean ezp=world.getBlockId(x, y, z+1)==blockId;
			boolean ezm=world.getBlockId(x, y, z-1)==blockId;
			
			if(exp && exm) return false;
			if(ezp && ezm) return false;
			
			if(exp && world.getBlockId(x+2, y, z)==blockId) return false;
			if(exm && world.getBlockId(x-2, y, z)==blockId) return false;
			if(ezp && world.getBlockId(x, y, z+2)==blockId) return false;
			if(ezm && world.getBlockId(x, y, z-2)==blockId) return false;
			
			Block block = Block.blocksList[blockId];
			int oldMeta = this.getMetadata(stack.getItemDamage());
			int meta = Block.blocksList[blockId].onBlockPlaced(world, x, y, z, side, hx, hy, hz, oldMeta);
	       	
			int ox=x,oy=y,oz=z;
	       	while(world.getBlockId(ox-1,oy,oz)==blockId) ox--;
	   		while(world.getBlockId(ox,oy,oz-1)==blockId) oz--;

			if (placeBlockAt(stack, player, world, x, y, z, side, hx, hy, hz, meta)) {
				world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
				--stack.stackSize;
			}

	   		if(
	   				(world.getBlockId(ox+0,oy,oz+0)==blockId) &&
	   				(world.getBlockId(ox+1,oy,oz+0)==blockId) &&
	   				(world.getBlockId(ox+0,oy,oz+1)==blockId) &&
	   				(world.getBlockId(ox+1,oy,oz+1)==blockId)
	   		){
	   			world.setBlockMetadataWithNotify(ox+0,oy,oz+0,1,3);
	   			world.setBlockMetadataWithNotify(ox+1,oy,oz+0,2,3);
	   			world.setBlockMetadataWithNotify(ox+0,oy,oz+1,3,3);
	   			world.setBlockMetadataWithNotify(ox+1,oy,oz+1,4,3);
	   		}
	   		
			return true;
		} else {
			return false;
		}
	}

}
