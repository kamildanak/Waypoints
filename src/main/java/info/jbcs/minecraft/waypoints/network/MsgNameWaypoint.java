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
    private Waypoint w;
    private String name;

    public MsgNameWaypoint() {
    }

    public MsgNameWaypoint(Waypoint w, String name) {
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

    public static class Handler implements IMessageHandler<MsgNameWaypoint, IMessage> {

        @Override
        public IMessage onMessage(MsgNameWaypoint message, MessageContext ctx) {
            FMLCommonHandler.instance().showGuiScreen(new GuiNameWaypoint(message.w, message.name));
            return null;
        }
    }
}