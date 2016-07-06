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
    private int x, y, z;
    private int waypointId;
    private String name;

    public MsgName() {
    }

    public MsgName(int x, int y, int z, int waypointId, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.waypointId = waypointId;
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        waypointId = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(waypointId);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    public static class Handler implements IMessageHandler<MsgName, IMessage> {

        @Override
        public IMessage onMessage(MsgName message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            //if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, message.x, message.y, message.z, player))
            //    return null;


            Waypoint w = Waypoint.getWaypoint(message.x, message.y, message.z, player.dimension);
            if (w == null) return null;

            w.name = message.name;
            w.changed = true;
            return null;
        }
    }
}