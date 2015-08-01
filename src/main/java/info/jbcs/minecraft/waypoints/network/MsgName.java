package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class MsgName extends Message {
    private Waypoint w;
    private String name;

    public MsgName() {
    }

    public MsgName(Waypoint w, String name) {
        this.w = w;
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        w = Waypoint.getWaypoint(buf.readInt());
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(w.id);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    public static class Handler implements IMessageHandler<MsgName, IMessage> {

        @Override
        public IMessage onMessage(MsgName message, MessageContext ctx) {
            if (message.w == null) return null;

            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, message.w.x, message.w.y, message.w.z, player))
                return null;

            message.w.name = message.name;
            message.w.changed = true;
            return null;
        }
    }
}