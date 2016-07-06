package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.gui.GuiNameWaypoint;
import io.netty.buffer.ByteBuf;

public class MsgNameWaypoint extends Message {
    private int x, y, z;
    private int waypointId;
    private String name;

    public MsgNameWaypoint() {
    }

    public MsgNameWaypoint(int x, int y, int z, int waypointId, String name) {
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

    public static class Handler implements IMessageHandler<MsgNameWaypoint, IMessage> {

        @Override
        public IMessage onMessage(MsgNameWaypoint message, MessageContext ctx) {
            FMLCommonHandler.instance().showGuiScreen(new GuiNameWaypoint(message.x, message.y, message.z, message.waypointId, message.name));
            return null;
        }
    }
}