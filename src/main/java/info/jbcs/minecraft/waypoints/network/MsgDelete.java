package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class MsgDelete extends Message {
    private Waypoint w;

    public MsgDelete() {
    }

    public MsgDelete(Waypoint w) {
        this.w = w;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        w = Waypoint.getWaypoint(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(w.id);
    }

    public static class Handler implements IMessageHandler<MsgDelete, IMessage> {

        @Override
        public IMessage onMessage(MsgDelete message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (message.w == null) return null;

            WaypointPlayerInfo info = WaypointPlayerInfo.get(player.getDisplayName());
            if (info == null) return null;
            info.removeWaypoint(message.w.id);

            return null;
        }
    }
}