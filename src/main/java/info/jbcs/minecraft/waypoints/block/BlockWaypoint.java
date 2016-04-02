package info.jbcs.minecraft.waypoints.block;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import info.jbcs.minecraft.waypoints.WaypointTeleporter;
import info.jbcs.minecraft.waypoints.Waypoints;
import info.jbcs.minecraft.waypoints.network.*;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static info.jbcs.minecraft.waypoints.General.isOP;

public class BlockWaypoint extends Block {

    public static final PropertyEnum TYPE = PropertyEnum.create("type", EnumType.class);

    public BlockWaypoint() {
        super(Material.rock);
        setUnlocalizedName("waypoint");
        this.setCreativeTab(CreativeTabs.tabTransport);
        setLightOpacity(255);
        this.setLightOpacity(0);

        this.setResistance(10F).setHardness(2.0F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.BASE));
        this.fullBlock = false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
    }

    @Override
    public SoundType getStepSound() {
        return Blocks.stone.getStepSound();
    }

    /* Function returns corner that is saved to Waypoints database */
    static private BlockPos getCorner(World world, BlockPos pos) {
        return getCorner(world, pos, -1, 0, -1);
    }

    /* Function returns corner of structure that block in position given as argument assuming that it is a rectangle. */
    static private BlockPos getCorner(World world, BlockPos pos, int x, int y, int z) {
        if (x != 0)
            while (world.getBlockState(pos.add(x, 0, 0)).getBlock() == Waypoints.blockWaypoint) pos = pos.add(x, 0, 0);
        if (y != 0)
            while (world.getBlockState(pos.add(0, y, 0)).getBlock() == Waypoints.blockWaypoint) pos = pos.add(0, y, 0);
        if (z != 0)
            while (world.getBlockState(pos.add(0, 0, z)).getBlock() == Waypoints.blockWaypoint) pos = pos.add(0, 0, z);
        return pos;
    }

    static public boolean isEntityOnWaypoint(World world, BlockPos pos, Entity entity) {
        BlockPos c1 = getCorner(world, pos, -1, 0, -1);
        BlockPos c2 = getCorner(world, pos, 1, 0, 1);
        return entity.posX >= c1.getX() && entity.posX <= c2.getX() + 1 && entity.posZ >= c1.getZ() && entity.posZ <= c2.getZ() + 1;
    }

    static public BlockPos checkSize(World world, BlockPos pos) {
        BlockPos c1 = getCorner(world, pos, -1, 0, -1);
        BlockPos c2 = getCorner(world, pos, 1, 0, 1);
        return new BlockPos(c2.getX() - c1.getX() + 1, 0, c2.getZ() - c1.getZ() + 1);
    }

    public boolean isValid(World world, BlockPos pos) {
        BlockPos c1 = getCorner(world, pos, -1, 0, -1);
        BlockPos size = checkSize(world, pos);
        if (size.getX() < Waypoints.minSize || size.getZ() < Waypoints.minSize) return false;
        if (size.getX() > Waypoints.maxSize || size.getZ() > Waypoints.maxSize) return false;
        if (!Waypoints.allowNotSquare && size.getX() != size.getZ()) return false;
        // check if all blocks in rectangle are correct type
        for (int px = 0; px < size.getX(); px++)
            for (int pz = 0; pz < size.getZ(); pz++)
                if (!(world.getBlockState(c1.add(px, 0, pz)).getBlock() == Waypoints.blockWaypoint)) return false;
        // check sides
        for (int px = 0; px < size.getX(); px++) {
            if (world.getBlockState(c1.add(px, 0, -1)).getBlock() == Waypoints.blockWaypoint) return false;
            if (world.getBlockState(c1.add(px, 0, size.getZ())).getBlock() == Waypoints.blockWaypoint) return false;
        }
        for (int pz = 0; pz < size.getZ(); pz++) {
            if (world.getBlockState(c1.add(-1, 0, pz)).getBlock() == Waypoints.blockWaypoint) return false;
            if (world.getBlockState(c1.add(size.getX(), 0, pz)).getBlock() == Waypoints.blockWaypoint) return false;
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState oldBlock) {
        super.breakBlock(world, pos, oldBlock);
        BlockPos c1a = pos, c1b = pos, c2a = pos, c2b = pos, c1, c2;
        int x = -1, z = -1;
        while (world.getBlockState(c1a.add(x, 0, 0)).getBlock() == Waypoints.blockWaypoint) c1a = c1a.add(x, 0, 0);
        while (world.getBlockState(c1a.add(0, 0, z)).getBlock() == Waypoints.blockWaypoint) c1a = c1a.add(0, 0, z);
        while (world.getBlockState(c1b.add(0, 0, z)).getBlock() == Waypoints.blockWaypoint) c1b = c1b.add(0, 0, z);
        while (world.getBlockState(c1b.add(x, 0, 0)).getBlock() == Waypoints.blockWaypoint) c1b = c1b.add(x, 0, 0);
        c1 = new BlockPos(Math.min(c1a.getX(), c1b.getX()), c1a.getY(), Math.min(c1a.getZ(), c1b.getZ()));
        if (!Waypoint.isWaypoint(world, c1)) return;
        Waypoint wp = Waypoint.getWaypoint(world, c1);
        if (wp != null) Waypoint.removeWaypoint(wp);
        x = 1;
        z = 1;
        while (world.getBlockState(c2a.add(x, 0, 0)).getBlock() == Waypoints.blockWaypoint) c2a = c2a.add(x, 0, 0);
        while (world.getBlockState(c2a.add(0, 0, z)).getBlock() == Waypoints.blockWaypoint) c2a = c2a.add(0, 0, z);
        while (world.getBlockState(c2b.add(0, 0, z)).getBlock() == Waypoints.blockWaypoint) c2b = c2b.add(0, 0, z);
        while (world.getBlockState(c2b.add(x, 0, 0)).getBlock() == Waypoints.blockWaypoint) c2b = c2b.add(x, 0, 0);
        c2 = new BlockPos(Math.max(c2a.getX(), c2b.getX()), c2a.getY(), Math.max(c2a.getZ(), c2b.getZ()));
        BlockPos size = new BlockPos(c2.getX() - c1.getX() + 1, 0, c2.getZ() - c1.getZ() + 1);
        for (int px = 0; px < size.getX(); px++)
            for (int pz = 0; pz < size.getZ(); pz++)
                if (world.getBlockState(c1.add(px, 0, pz)).getBlock() == Waypoints.blockWaypoint)
                    world.setBlockState(c1.add(px, 0, pz), Waypoints.blockWaypoint.getStateFromMeta(0), 3);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        if (!isValid(world, pos)) return true;
        if (!isEntityOnWaypoint(world, pos, player)) return true;
        if (!Waypoints.allowActivation && !isOP(player) && !Waypoint.isWaypoint(world, getCorner(world, pos)))
            return true;
        Waypoint src = Waypoint.getOrMakeWaypoint(world, getCorner(world, pos));
        if (src == null) return true;
        if (src.name.isEmpty()) {
            activateStructure(world, pos);
            MsgNameWaypoint msg = new MsgNameWaypoint(src.pos, src.id, "Waypoint #" + src.id);
            PacketDispatcher.sendTo(msg, (EntityPlayerMP) player);
        } else {
            ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
            WaypointPlayerInfo info = WaypointPlayerInfo.get(player.getUniqueID().toString());
            if (info == null) return false;
            info.addWaypoint(src.id);

            for (Waypoint w : Waypoint.existingWaypoints)
                if (info.discoveredWaypoints.containsKey(w.id))
                    waypoints.add(w);

            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            if (player.isSneaking() && (Waypoints.allowActivation || isOP(player)))
                PacketDispatcher.sendTo(new MsgEditWaypoint(src.id, src.name, src.linked_id, waypoints), playerMP);
            else
                PacketDispatcher.sendTo(new MsgWaypointsList(src.id, waypoints), playerMP);
        }
        return true;
    }

    public void activateStructure(World world, BlockPos pos) {
        if (!isValid(world, pos)) return;
        BlockPos size = checkSize(world, pos);
        BlockPos corner = getCorner(world, pos);
        // Set all middle block meta to 5
        for (int pz = 1; pz < size.getZ() - 1; pz++)
            for (int px = 1; px < size.getX() - 1; px++)
                world.setBlockState(corner.add(px, 0, pz), Waypoints.blockWaypoint.getStateFromMeta(5), 3);
        // Set close edge as 1, 2, 2, ...,  2, 2, 3
        // Set away edge as 7, 8, 8, ...,  8, 8, 9
        world.setBlockState(corner.add(0, 0, 0), Waypoints.blockWaypoint.getStateFromMeta(1), 3); //close
        world.setBlockState(corner.add(size.getX() - 1, 0, 0), Waypoints.blockWaypoint.getStateFromMeta(3), 3); //close
        world.setBlockState(corner.add(0, 0, size.getZ() - 1), Waypoints.blockWaypoint.getStateFromMeta(7), 3); //away
        world.setBlockState(corner.add(size.getX() - 1, 0, size.getZ() - 1), Waypoints.blockWaypoint.getStateFromMeta(9), 3); //away
        for (int px = 1; px < size.getX() - 1; px++) {
            world.setBlockState(corner.add(px, 0, 0), Waypoints.blockWaypoint.getStateFromMeta(2), 3); //close
            world.setBlockState(corner.add(px, 0, size.getZ() - 1), Waypoints.blockWaypoint.getStateFromMeta(8), 3); //away
        }
        // Set unset left edge to 4
        // Set unset right edge to 6
        for (int pz = 1; pz < size.getZ() - 1; pz++) {
            world.setBlockState(corner.add(0, 0, pz), Waypoints.blockWaypoint.getStateFromMeta(4), 3);
            world.setBlockState(corner.add(size.getX() - 1, 0, pz), Waypoints.blockWaypoint.getStateFromMeta(6), 3);
        }
    }

    @Override
    public void randomDisplayTick(IBlockState blockState, World world, BlockPos pos, Random rand) {
        float fx = (float) pos.getX() + 0.5F;
        float fy = (float) pos.getY() + 1.0F + rand.nextFloat() * 6.0F / 16.0F;
        float fz = (float) pos.getZ() + 0.5F;
        BlockPos corner = getCorner(world, pos);
        Waypoint src = Waypoint.getWaypoint(world, corner);
        if (src != null && src.powered) {
            Waypoint w = Waypoint.getWaypoint(src.linked_id - 1);
            if (w == null) return;
            world.spawnParticle(EnumParticleTypes.PORTAL, (double) fx + rand.nextFloat() % 1 - 0.5, (double) fy, (double) fz + rand.nextFloat() % 1 - 0.5, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.PORTAL, (double) fx, (double) fy, (double) fz, 0.0D, 0.0D, 0.0D);
        }
    }

    public boolean isPowered(World world, BlockPos pos) {
        BlockPos corner = getCorner(world, pos);
        BlockPos size = checkSize(world, corner);
        for (int xp = 0; xp < size.getX(); xp++) {
            for (int zp = 0; zp < size.getZ(); zp++) {
                if (world.isBlockIndirectlyGettingPowered(corner.add(xp, 0, zp)) > 0) return true;
            }
        }
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote) {
            Waypoint src = Waypoint.getWaypoint(world, getCorner(world, pos));
            if (src == null) return;
            if (BlockWaypoint.isEntityOnWaypoint(world, pos, entity)) {
                Waypoint w = Waypoint.getWaypoint(src.linked_id - 1);
                if (w == null) return;

                BlockPos size = BlockWaypoint.checkSize(entity.worldObj, w.pos);
                boolean teleported = false;
                if (entity instanceof EntityPlayer || entity instanceof EntityLiving) {
                    if (entity.timeUntilPortal > 0 && entity.timeUntilPortal <= entity.getPortalCooldown()) {
                        entity.timeUntilPortal = entity.getPortalCooldown();
                    } else if (entity.timeUntilPortal > entity.getPortalCooldown() && entity.timeUntilPortal < 2 * entity.getPortalCooldown()) {
                        MinecraftServer minecraftServer = world.getMinecraftServer();
                        entity.timeUntilPortal = entity.getPortalCooldown();
                        teleported = new WaypointTeleporter(minecraftServer.worldServerForDimension(world.provider.getDimension())).teleport(entity, world, w);
                    } else if (src.powered && entity instanceof EntityPlayer && entity.timeUntilPortal == 0) {
                        entity.timeUntilPortal = 2 * entity.getPortalCooldown() + 20;
                    } else if (src.powered && entity.timeUntilPortal == 0) {
                        entity.timeUntilPortal = 2 * entity.getPortalCooldown();
                    }
                }
                if (teleported) {
                    if(Waypoints.playSounds){
                        world.playSound(entity.posX, entity.posY, entity.posZ, Waypoints.soundEvent, SoundCategory.MASTER, 1.0f, 1.0f, true);
                    }
                    MsgRedDust msg1 = new MsgRedDust(src.dimension, entity.posX, entity.posY, entity.posZ);
                    MsgRedDust msg2 = new MsgRedDust(w.dimension, w.pos.getX() + size.getX() / 2.0, w.pos.getY() + 0.5, w.pos.getZ() + size.getZ() / 2.0);
                    PacketDispatcher.sendToAllAround(msg1, new NetworkRegistry.TargetPoint(src.dimension, entity.posX, entity.posY, entity.posZ, 25));
                    PacketDispatcher.sendToAllAround(msg2, new NetworkRegistry.TargetPoint(w.dimension, w.pos.getX() + size.getX() / 2.0, w.pos.getY(), w.pos.getZ() + size.getZ() / 2.0, 25));
                }
            }
        }
    }

    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
        Waypoint waypoint = Waypoint.getWaypoint(world, getCorner(world, pos));
        if (waypoint == null) return;
        BlockPos corner = getCorner(world, pos);
        if (isPowered(world, corner)) {
            waypoint.powered = true;
            waypoint.changed = true;
        } else {
            waypoint.powered = false;
            waypoint.changed = true;
        }
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        list.add(new ItemStack(itemIn, 1, 0)); //Meta 0
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{TYPE});
    }

    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        return (int) ((EnumType) state.getValue(TYPE)).getID();
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, EnumType.byID(meta));
    }

    public enum EnumType implements IStringSerializable {
        BASE(0, "base"),
        S1(1, "1"),
        S2(2, "2"),
        S3(3, "3"),
        S4(4, "4"),
        S5(5, "5"),
        S6(6, "6"),
        S7(7, "7"),
        S8(8, "8"),
        s9(9, "9");

        private int ID;
        private String name;

        private EnumType(int ID, String name) {
            this.ID = ID;
            this.name = name;
        }

        public static EnumType byID(int id) {
            if (id < 0 || id > values().length) {
                id = 0;
            }
            return values()[id];
        }

        @Override
        public String getName() {
            return name;
        }

        public int getID() {
            return ID;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
