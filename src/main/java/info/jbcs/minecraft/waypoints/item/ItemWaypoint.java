package info.jbcs.minecraft.waypoints.item;

import info.jbcs.minecraft.waypoints.Waypoints;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWaypoint extends ItemBlock {
    Block blockWaypoint;

    public ItemWaypoint(Block block) {
        super(block);
        blockWaypoint = block;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hx, float hy, float hz) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        Block block = world.getBlockState(pos).getBlock();

        if (!block.isReplaceable(world, pos)) pos = pos.add(side.getDirectionVec());
        if (stack.stackSize == 0) return EnumActionResult.SUCCESS;
        if (!player.canPlayerEdit(pos, side, stack)) return EnumActionResult.SUCCESS;

        int north = countWaypointBlocks(world, pos, 0, 0, 1, Waypoints.maxSize - 1);
        int south = countWaypointBlocks(world, pos, 0, 0, -1, Waypoints.maxSize - 1);
        int east = countWaypointBlocks(world, pos, 1, 0, 0, Waypoints.maxSize - 1);
        int west = countWaypointBlocks(world, pos, -1, 0, 0, Waypoints.maxSize - 1);

        if (north == -1 || south == -1 || east == -1 || west == -1) return EnumActionResult.SUCCESS;
        if (north + south + 1 > Waypoints.maxSize || east + west + 1 > Waypoints.maxSize)
            return EnumActionResult.SUCCESS;
        if (isActivated(world, pos.add(1, 0, 0)) || isActivated(world, pos.add(-1, 0, 0)) ||
                isActivated(world, pos.add(0, 0, 1)) || isActivated(world, pos.add(0, 0, -1)))
            return EnumActionResult.SUCCESS;

        int oldMeta = this.getMetadata(stack.getItemDamage());
        IBlockState meta = blockWaypoint.onBlockPlaced(world, pos, side, hx, hy, hz, oldMeta, player);


        int ox = x, oz = z;
        while (world.getBlockState(new BlockPos(ox - 1, y, oz)).getBlock() == blockWaypoint) ox--;
        while (world.getBlockState(new BlockPos(ox, y, oz - 1)).getBlock() == blockWaypoint) oz--;

        if (placeBlockAt(stack, player, world, pos, side, hx, hy, hz, meta)) {
            world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.MASTER,
                    (blockWaypoint.getSoundType().getVolume() + 1.0F) / 2.0F, blockWaypoint.getSoundType().getPitch() * 0.8F, true);
            --stack.stackSize;
        }

        return EnumActionResult.SUCCESS;
    }

    int countWaypointBlocks(World world, BlockPos pos, int px, int py, int pz, int maxSize) {
        for (int c = 0; c < maxSize + 1; c++) {
            if (world.getBlockState(pos.add((c + 1) * px, (c + 1) * py, (c + 1) * pz)).getBlock() != blockWaypoint)
                return c;
        }
        return -1;
    }

    boolean isActivated(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) != 0 && world.getBlockState(pos).getBlock() == blockWaypoint;
    }

}
