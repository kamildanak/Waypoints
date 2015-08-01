package info.jbcs.minecraft.waypoints.item;

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
            switch (side) {
                case 0:
                    y--;
                    break;
                case 1:
                    y++;
                    break;
                case 2:
                    z--;
                    break;
                case 3:
                    z++;
                    break;
                case 4:
                    x--;
                    break;
                case 5:
                    x++;
                    break;
            }
        }

        if (stack.stackSize == 0) {
            return false;
        } else if (!player.canPlayerEdit(x, y, z, side, stack)) {
            return false;
        } else if (world.canPlaceEntityOnSide(blockWaypoint, x, y, z, false, side, player, stack)) {
            boolean exp = world.getBlock(x + 1, y, z) == blockWaypoint;
            boolean expp = world.getBlock(x + 2, y, z) == blockWaypoint;
            boolean exppp = world.getBlock(x + 3, y, z) == blockWaypoint;
            boolean exm = world.getBlock(x - 1, y, z) == blockWaypoint;
            boolean exmm = world.getBlock(x - 2, y, z) == blockWaypoint;
            boolean exmmm = world.getBlock(x - 3, y, z) == blockWaypoint;
            boolean ezp = world.getBlock(x, y, z + 1) == blockWaypoint;
            boolean ezpp = world.getBlock(x, y, z + 2) == blockWaypoint;
            boolean ezppp = world.getBlock(x, y, z + 3) == blockWaypoint;
            boolean ezm = world.getBlock(x, y, z - 1) == blockWaypoint;
            boolean ezmm = world.getBlock(x, y, z - 2) == blockWaypoint;
            boolean ezmmm = world.getBlock(x, y, z - 3) == blockWaypoint;

            int mexp = world.getBlockMetadata(x + 1, y, z);
            int mexm = world.getBlockMetadata(x - 1, y, z);
            int mezp = world.getBlockMetadata(x, y, z + 1);
            int mezm = world.getBlockMetadata(x, y, z - 1);


            if ((exp && expp && exppp) || (exp && expp && exm) || (exp && exm && exmm) || (exm && exmm && exmmm))
                return false;
            if ((ezp && ezpp && ezppp) || (ezp && ezpp && ezm) || (ezp && ezm && ezmm) || (ezm && ezmm && ezmmm))
                return false;
            if ((mexp != 0 && exp) || (mexm != 0 && exm) || (mezp != 0 && ezp) || (mezm != 0 && ezm)) return false;

            Block block = blockWaypoint;
            int oldMeta = this.getMetadata(stack.getItemDamage());
            int meta = blockWaypoint.onBlockPlaced(world, x, y, z, side, hx, hy, hz, oldMeta);

            int ox = x, oz = z;
            while (world.getBlock(ox - 1, y, oz) == blockWaypoint) ox--;
            while (world.getBlock(ox, y, oz - 1) == blockWaypoint) oz--;

            if (placeBlockAt(stack, player, world, x, y, z, side, hx, hy, hz, meta)) {
                world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
            }
        }
        return true;
    }

}
