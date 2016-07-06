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
    private int waypointId;
    private String name;
    private int linked_id;

    public MsgEdit() {
    }

    public MsgEdit(int waypointId, String name, int linked_id) {
        this.waypointId = waypointId;
        this.name = name;
        this.linked_id = linked_id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        waypointId = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
        linked_id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(waypointId);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(linked_id);
    }

    public static class Handler implements IMessageHandler<MsgEdit, IMessage> {

        @Override
        public IMessage onMessage(MsgEdit message, MessageContext ctx){
            Waypoint w = Waypoint.getWaypoint(message.waypointId);
            if(w == null) return null;

            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, w.x, w.y, w.z, player))
                return null;

            w.name = message.name;
            w.linked_id = message.linked_id;
            w.changed = true;
            return null;
        }
    }
}