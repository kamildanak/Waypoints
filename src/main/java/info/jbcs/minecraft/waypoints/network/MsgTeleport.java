package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.Waypoints;
import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

public class MsgTeleport extends Message {
    private int srcID, destID;

    public MsgTeleport() {
    }

    public MsgTeleport(int srcId, int destId) {
        this.srcID = srcId;
        this.destID = destId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        srcID = buf.readInt();
        destID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(srcID);
        buf.writeInt(destID);
    }

    public static class Handler implements IMessageHandler<MsgTeleport, IMessage> {

        @Override
        public IMessage onMessage(MsgTeleport message, MessageContext ctx) {
            Waypoint src = Waypoint.getWaypoint(message.srcID);
            Waypoint dest = Waypoint.getWaypoint(message.destID);
            if (src == null || dest == null) return null;

            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, src.x, src.y, src.z, player)) return null;
            player.mountEntity((Entity) null);

            MsgRedDust msg1 = new MsgRedDust(player.dimension, player.posX, player.posY, player.posZ);

            if (player.dimension != dest.dimension) player.travelToDimension(dest.dimension);
            int size = BlockWaypoint.checkSize(player.worldObj, dest.x, dest.y, dest.z);
            player.setLocationAndAngles(dest.x + size / 2.0, dest.y + 0.5, dest.z + size / 2.0, player.rotationYaw, 0);
            player.setPositionAndUpdate(dest.x + size / 2.0, dest.y + 0.5, dest.z + size / 2.0);

            MsgRedDust msg2 = new MsgRedDust(dest.dimension, dest.x + size / 2.0, dest.y + 0.5, dest.z + size / 2.0);
            Waypoints.instance.messagePipeline.sendToAllAround(msg1, new NetworkRegistry.TargetPoint(msg1.getDimension(), msg1.getX(), msg1.getY(), msg1.getZ(), 25));
            Waypoints.instance.messagePipeline.sendToAllAround(msg2, new NetworkRegistry.TargetPoint(msg2.getDimension(), msg2.getX(), msg2.getY(), msg2.getZ(), 25));

            return null;
        }
    }
}