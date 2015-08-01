package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.gui.GuiEditWaypoint;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;

public class MsgEditWaypoint extends Message {
    private int id;
    private String name;
    private Integer linked_id;
    private ArrayList<Waypoint> waypoints;

    public MsgEditWaypoint() {
    }

    public MsgEditWaypoint(int id, String name, int linked_id, ArrayList<Waypoint> waypoints) {
        this.id = id;
        this.name = name;
        this.linked_id = linked_id;
        this.waypoints = waypoints;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
        linked_id = buf.readInt();
        int count = buf.readInt();

        waypoints = new ArrayList<Waypoint>();
        for (int i = 0; i < count; i++) {
            try {
                waypoints.add(new Waypoint(buf));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(linked_id);
        buf.writeInt(waypoints.size());
        for (Waypoint w : waypoints) {
            try {
                w.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Handler implements IMessageHandler<MsgEditWaypoint, IMessage> {

        @Override
        public IMessage onMessage(MsgEditWaypoint message, MessageContext ctx) {
            FMLCommonHandler.instance().showGuiScreen(new GuiEditWaypoint(message.id, message.name, message.linked_id, message.waypoints));
            return null;
        }
    }
}