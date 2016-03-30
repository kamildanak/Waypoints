package info.jbcs.minecraft.waypoints.block;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import info.jbcs.minecraft.waypoints.WaypointTeleporter;
import info.jbcs.minecraft.waypoints.Waypoints;
import info.jbcs.minecraft.waypoints.network.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockWaypoint extends Block {

    public static final PropertyEnum TYPE = PropertyEnum.create("type", EnumType.class);

    public BlockWaypoint() {
        super(Material.rock);
        setUnlocalizedName("waypoint");
        this.setCreativeTab(CreativeTabs.tabTransport);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        setLightOpacity(255);
        this.setLightOpacity(0);

        this.setResistance(10F).setStepSound(Blocks.stone.stepSound).setHardness(2.0F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.BASE));
    }

    static public BlockPos getCorner(World world, BlockPos pos) {
        while (world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Waypoints.blockWaypoint) pos = pos.add(-1, 0, 0);
        while (world.getBlockState(pos.add(0, 0, -1)).getBlock() == Waypoints.blockWaypoint) pos = pos.add(0, 0, -1);
        return pos;
    }

    static public boolean isEntityOnWaypoint(World world, BlockPos pos, Entity entity) {
        int size = checkSize(world, pos, 0);
        return entity.posX >= pos.getX() && entity.posX <= pos.getX() + size && entity.posZ >= pos.getZ() && entity.posZ <= pos.getZ() + size;
    }

    static public int checkSize(World world, BlockPos pos, int broken) {
        BlockPos corner = getCorner(world, pos);
        int c1 = 0, c2 = 0;
        for (int px = 0; px < 2; px++)
            for (int pz = 0; pz < 2; pz++)
                if (world.getBlockState(corner.add(px, 0, pz)).getBlock().equals(Waypoints.blockWaypoint)) c1++;
        if (c1 < 4 - broken) return 1;

        for (int px = 0; px < 3; px++)
            for (int pz = 0; pz < 3; pz++)
                if (world.getBlockState(corner.add(px, 0, pz)).getBlock().equals(Waypoints.blockWaypoint)) c2++;

        if (c2 < 9 - broken)
            if (c2 > c1) return 1;
            else
                return 2;
        return 3;
    }

    public boolean isValidWaypoint(World world, BlockPos pos) {
        if (checkSize(world, pos, 0) == 1) return false;
        if (world.getBlockState(pos).getBlock() != this) return false; //checkSize do not check this one
        if (world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == 0)
            return false; //check if activated
        //here we can check metadata it _should_ be always true so let omit this until some bug happen
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState oldBlock) {
        super.breakBlock(world, pos, oldBlock);
        BlockPos corner = getCorner(world, pos);
        int size = checkSize(world, pos, 1);

        Waypoint wp = null;
        if (isValidWaypoint(world, pos))
            wp = Waypoint.getWaypoint(pos, world.provider.getDimensionId());

        for (int px = 0; px < size; px++)
            for (int pz = 0; pz < size; pz++)
                if (!corner.add(px, 0, pz).equals(pos))
                    world.setBlockState(corner.add(px, 0, pz), Waypoints.blockWaypoint.getStateFromMeta(0), 3);

        if (wp == null) return;
        Waypoint.removeWaypoint(wp);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        BlockPos corner = getCorner(world, pos);
        boolean isOP = MinecraftServer.getServer().getConfigurationManager().canSendCommands(player.getGameProfile());
        if (Waypoints.allowActivation || isOP) activateStructure(world, corner);

        if (!isValidWaypoint(world, corner)) return true;

        Waypoint src = null;
        if (isValidWaypoint(world, pos))
            src = Waypoint.getWaypoint(corner, player.dimension);
        if (src == null) return true;

        if (!isEntityOnWaypoint(world, corner, player)) return true;
        if (src.name.isEmpty()) {
            MsgNameWaypoint msg = new MsgNameWaypoint(src, "Waypoint #" + src.id);
            PacketDispatcher.sendTo(msg, (EntityPlayerMP) player);

        } else if (player.isSneaking() && (Waypoints.allowActivation || isOP)) {
            //Add waypoints
            ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
            WaypointPlayerInfo info = WaypointPlayerInfo.get(player.getUniqueID().toString());
            if (info == null) return false;
            info.addWaypoint(src.id);

            for (Waypoint w : Waypoint.existingWaypoints)
                if (info.discoveredWaypoints.containsKey(w.id))
                    waypoints.add(w);

            MsgEditWaypoint msg = new MsgEditWaypoint(src.id, src.name, src.linked_id, waypoints);
            PacketDispatcher.sendTo(msg, (EntityPlayerMP) player);
        } else {
            try {
                Packets.sendWaypointsToPlayer((EntityPlayerMP) player, src.id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void activateStructure(World world, BlockPos pos) {
        BlockPos corner = getCorner(world, pos);
        if (checkSize(world, corner, 0) == 3)
            for (int i = 1, z = 0; z < 3; z++)
                for (int x = 0; x < 3; x++, i++)
                    world.setBlockState(corner.add(x, 0, z), Waypoints.blockWaypoint.getStateFromMeta(i), 3);

        if (checkSize(world, corner, 0) == 2)
            for (int i = 1, z = 0; z < 2; z++, i += 2)
                for (int x = 0; x < 2; x++, i += 2)
                    world.setBlockState(corner.add(x, 0, z), Waypoints.blockWaypoint.getStateFromMeta(i), 3);
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
        BlockWaypoint blockWaypoint = (BlockWaypoint) state.getBlock();
        if (!blockWaypoint.isValidWaypoint(world, pos)) return;

        float fx = (float) pos.getX() + 0.5F;
        float fy = (float) pos.getY() + 1.0F + rand.nextFloat() * 6.0F / 16.0F;
        float fz = (float) pos.getZ() + 0.5F;
        BlockPos corner = getCorner(world, pos);
        Waypoint src = Waypoint.getWaypoint(corner, world.provider.getDimensionId());
        if (src.powered) {
            Waypoint w = Waypoint.getWaypoint(src.linked_id - 1);
            if (w == null) return;
            world.spawnParticle(EnumParticleTypes.PORTAL, (double) fx + rand.nextFloat() % 1 - 0.5, (double) fy, (double) fz + rand.nextFloat() % 1 - 0.5, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.PORTAL, (double) fx, (double) fy, (double) fz, 0.0D, 0.0D, 0.0D);
        }
    }

    public boolean isPowered(World world, BlockPos pos) {
        BlockPos corner = getCorner(world, pos);

        int size = checkSize(world, corner, 0);
        for (int xp = 0; xp < size; xp++) {
            for (int zp = 0; zp < size; zp++) {
                if (world.isBlockIndirectlyGettingPowered(corner.add(xp, 0, zp)) > 0) return true;
            }
        }
        return false;
    }

    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
        BlockPos corner = getCorner(world, pos);
        Random rand = new Random();
        if (!world.isRemote) {
            Waypoint src = null;
            if (isValidWaypoint(world, pos))
                src = Waypoint.getWaypoint(corner, entity.dimension);
            else
                return;
            if (BlockWaypoint.isEntityOnWaypoint(world, corner, entity)) {
                Waypoint w = Waypoint.getWaypoint(src.linked_id - 1);
                if (w == null) return;
                ByteBuf buffer1 = Unpooled.buffer();
                buffer1.writeInt(3);
                buffer1.writeDouble(entity.posX);
                buffer1.writeDouble(entity.posY);
                buffer1.writeDouble(entity.posZ);
                FMLProxyPacket packet1 = new FMLProxyPacket(new PacketBuffer(buffer1.copy()), "Waypoints");

                int size = BlockWaypoint.checkSize(entity.worldObj, w.pos, 0);
                boolean teleported = false;
                if (entity instanceof EntityPlayer || entity instanceof EntityLiving) {
                    if (entity.timeUntilPortal > 0 && entity.timeUntilPortal <= entity.getPortalCooldown()) {
                        entity.timeUntilPortal = entity.getPortalCooldown();
                    } else if (entity.timeUntilPortal > entity.getPortalCooldown() && entity.timeUntilPortal < 2 * entity.getPortalCooldown()) {
                        MinecraftServer minecraftServer = MinecraftServer.getServer();
                        entity.timeUntilPortal = entity.getPortalCooldown();
                        teleported = new WaypointTeleporter(minecraftServer.worldServerForDimension(world.provider.getDimensionId())).teleport(entity, world, w);
                    } else if (src.powered && entity instanceof EntityPlayer && entity.timeUntilPortal == 0) {
                        entity.timeUntilPortal = 2 * entity.getPortalCooldown() + 20;
                    } else if (src.powered && entity.timeUntilPortal == 0) {
                        entity.timeUntilPortal = 2 * entity.getPortalCooldown();
                    }
                }
                if (teleported) {
                    MsgRedDust msg1 = new MsgRedDust(src.dimension, entity.posX, entity.posY, entity.posZ);
                    MsgRedDust msg2 = new MsgRedDust(w.dimension, w.pos.getX() + size / 2.0, w.pos.getY() + 0.5, w.pos.getZ() + size / 2.0);
                    PacketDispatcher.sendToAllAround(msg1, new NetworkRegistry.TargetPoint(src.dimension, entity.posX, entity.posY, entity.posZ, 25));
                    PacketDispatcher.sendToAllAround(msg2, new NetworkRegistry.TargetPoint(w.dimension, w.pos.getX() + size / 2.0, w.pos.getY(), w.pos.getZ() + size / 2.0, 25));
                }
            }
        }
    }

    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
        if (!isValidWaypoint(world, pos)) return;
        BlockPos corner = getCorner(world, pos);
        if (isPowered(world, corner)) {
            Waypoint waypoint = Waypoint.getWaypoint(corner, world.provider.getDimensionId());
            waypoint.powered = true;
            waypoint.changed = true;
        } else {
            Waypoint waypoint = Waypoint.getWaypoint(corner, world.provider.getDimensionId());
            waypoint.powered = false;
            waypoint.changed = true;
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        list.add(new ItemStack(itemIn, 1, 0)); //Meta 0
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, new IProperty[]{TYPE});
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
