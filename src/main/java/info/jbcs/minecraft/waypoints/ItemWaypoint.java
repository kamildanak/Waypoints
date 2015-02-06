package info.jbcs.minecraft.waypoints;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemWaypoint extends ItemBlock {
	Block blockWaypoint;
	
	public ItemWaypoint(Block block) {
		super(block);
        blockWaypoint = block;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
		Block block1 = world.getBlock(x, y, z);

        if (block1 != Blocks.vine && block1 != Blocks.tallgrass && block1 != Blocks.deadbush) { //&& (block1 == null || block1.isReplaceable(world, x, y, z))) {
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
        }else if (!player.canPlayerEdit(x, y, z, side, stack)) {
            return false;
        }else if (world.canPlaceEntityOnSide(blockWaypoint, x, y, z, false, side, player, stack)) {
            boolean exp=world.getBlock(x+1, y, z)==blockWaypoint;
            boolean expp=world.getBlock(x+2, y, z)==blockWaypoint;
            boolean exppp=world.getBlock(x+3, y, z)==blockWaypoint;
            boolean exm=world.getBlock(x - 1, y, z)==blockWaypoint;
            boolean exmm=world.getBlock(x - 2, y, z)==blockWaypoint;
            boolean exmmm=world.getBlock(x - 3, y, z)==blockWaypoint;
            boolean ezp=world.getBlock(x, y, z + 1)==blockWaypoint;
            boolean ezpp=world.getBlock(x, y, z + 2)==blockWaypoint;
            boolean ezppp=world.getBlock(x, y, z + 3)==blockWaypoint;
            boolean ezm=world.getBlock(x, y, z - 1)==blockWaypoint;
            boolean ezmm=world.getBlock(x, y, z - 2)==blockWaypoint;
            boolean ezmmm=world.getBlock(x, y, z - 3)==blockWaypoint;

            int mexp=world.getBlockMetadata(x + 1, y, z);
            int mexpp=world.getBlockMetadata(x + 2, y, z);
            int mexppp=world.getBlockMetadata(x + 3, y, z);
            int mexm=world.getBlockMetadata(x - 1, y, z);
            int mexmm=world.getBlockMetadata(x - 2, y, z);
            int mexmmm=world.getBlockMetadata(x - 3, y, z);
            int mezp=world.getBlockMetadata(x, y, z + 1);
            int mezpp=world.getBlockMetadata(x, y, z + 2);
            int mezppp=world.getBlockMetadata(x, y, z + 3);
            int mezm=world.getBlockMetadata(x, y, z - 1);
            int mezmm=world.getBlockMetadata(x, y, z - 2);
            int mezmmm=world.getBlockMetadata(x, y, z - 3);


            if((exp && expp && exppp) || (exp && expp && exm) || (exp && exm && exmm) || (exm && exmm && exmmm)) return false;
            if((ezp && ezpp && ezppp) || (ezp && ezpp && ezm) || (ezp && ezm && ezmm) || (ezm && ezmm && ezmmm)) return false;
            if((mexp!=0 && exp) || (mexpp!=0 && expp) || (mexppp!=0 && exppp) || (mexm!=0 && exm) ||
                    (mexmm!=0 && exmm) || (mexmmm!=0 && exmmm)) return false;
            if((mezp!=0 && ezp) || (mezpp!=0 && ezpp) || (mezppp!=0 && ezppp) || (mezm!=0 && ezm) ||
                    (mezmm!=0 && ezmm) || (mezmmm!=0 && ezmmm)) return false;

            Block block = blockWaypoint;
            int oldMeta = this.getMetadata(stack.getItemDamage());
            int meta = blockWaypoint.onBlockPlaced(world, x, y, z, side, hx, hy, hz, oldMeta);

            int ox = x, oy = y, oz = z;
            while (world.getBlock(ox - 1, oy, oz) == blockWaypoint) ox--;
            while (world.getBlock(ox, oy, oz - 1) == blockWaypoint) oz--;

            if (placeBlockAt(stack, player, world, x, y, z, side, hx, hy, hz, meta)) {
                world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
            }
        }
        return true;
	}

}
