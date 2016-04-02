package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointTeleporter;
import info.jbcs.minecraft.waypoints.Waypoints;
import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class MsgTeleport extends AbstractMessage.AbstractServerMessage<MsgTeleport> {
    private static int srcID, destID;

    public MsgTeleport() {
    }

    public MsgTeleport(int src, int dest) {
        this.srcID = src;
        this.destID = dest;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException {
        srcID = buffer.readInt();
        destID = buffer.readInt();

    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(srcID);
        buffer.writeInt(destID);

    }

    @Override
    public void process(EntityPlayer player, Side side) {
        Waypoint src = Waypoint.getWaypoint(srcID);
        Waypoint dest = Waypoint.getWaypoint(destID);
        if (src == null || dest == null) return;

        if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, src.pos, player)) return;
        player.dismountRidingEntity();

        MsgRedDust msg1 = new MsgRedDust(player.dimension, player.posX, player.posY, player.posZ);

        if (player.dimension != dest.dimension)
            player.worldObj.getMinecraftServer().getPlayerList().transferPlayerToDimension((EntityPlayerMP) player, dest.dimension,
                    new WaypointTeleporter(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dest.dimension)));
        BlockPos size = BlockWaypoint.checkSize(player.worldObj, dest.pos);
        player.setLocationAndAngles(dest.pos.getX() + size.getX() / 2.0, dest.pos.getY() + 0.5, dest.pos.getZ() + size.getZ() / 2.0, player.rotationYaw, 0);
        player.setPositionAndUpdate(dest.pos.getX() + size.getX() / 2.0, dest.pos.getY() + 0.5, dest.pos.getZ() + size.getZ() / 2.0);

        MsgRedDust msg2 = new MsgRedDust(dest.dimension, dest.pos.getX() + size.getX() / 2.0, dest.pos.getY() + 0.5, dest.pos.getZ() + size.getZ() / 2.0);
        PacketDispatcher.sendToAllAround(msg1, new NetworkRegistry.TargetPoint(msg1.getDimension(), msg1.getX(), msg1.getY(), msg1.getZ(), 25));
        PacketDispatcher.sendToAllAround(msg2, new NetworkRegistry.TargetPoint(msg2.getDimension(), msg2.getX(), msg2.getY(), msg2.getZ(), 25));

        player.getFoodStats().addExhaustion(Waypoints.teleportationExhaustion);

        for (int i = 0; i < Waypoints.potionEffects.length && i < Waypoints.potionEffectsChances.length; i++) {
            if (ThreadLocalRandom.current().nextInt(0, 99) < Waypoints.potionEffectsChances[i]) {
                player.addPotionEffect(new PotionEffect(Waypoints.potionEffects[i]));
            }
        }

        if(Waypoints.playSounds) {
            player.worldObj.playSound(player.posX, player.posY, player.posZ, Waypoints.soundEvent, SoundCategory.MASTER, 1.0f, 1.0f, true);
            player.worldObj.playSound(dest.pos.getX() + size.getX() / 2.0, dest.pos.getY() + 0.5, dest.pos.getZ() + size.getZ() / 2.0, Waypoints.soundEvent, SoundCategory.MASTER, 1.0f, 1.0f, true);
        }
    }

}