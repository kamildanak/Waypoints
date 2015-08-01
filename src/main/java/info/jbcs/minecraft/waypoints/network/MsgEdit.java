package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class MsgEdit extends Message {
    private Waypoint w;
    private String name;
    private int linked_id;

    public MsgEdit() {
    }

    public MsgEdit(Waypoint w, String name, int linked_id) {
        this.w = w;
        this.name = name;
        this.linked_id = linked_id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        w = Waypoint.getWaypoint(buf.readInt());
        name = ByteBufUtils.readUTF8String(buf);
        linked_id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(w.id);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(linked_id);
    }

    public static class Handler implements IMessageHandler<MsgEdit, IMessage> {

        @Override
        public IMessage onMessage(MsgEdit message, MessageContext ctx) {
            if (message.w == null) return null;

            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, message.w.x, message.w.y, message.w.z, player))
                return null;

            message.w.name = message.name;
            message.w.linked_id = message.linked_id;
            message.w.changed = true;
            return null;
        }
    }
}