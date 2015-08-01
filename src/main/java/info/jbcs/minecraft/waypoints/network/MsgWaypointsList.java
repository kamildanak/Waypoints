package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.gui.GuiWaypoints;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;

public class MsgWaypointsList extends Message {
    private ArrayList<Waypoint> waypoints;
    private int srcWaypointId;

    public MsgWaypointsList() {
    }

    public MsgWaypointsList(int srcWaypointId, ArrayList<Waypoint> waypoints) {
        this.srcWaypointId = srcWaypointId;
        this.waypoints = waypoints;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        srcWaypointId = buf.readInt();
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
        buf.writeInt(srcWaypointId);
        buf.writeInt(waypoints.size());
        for (Waypoint w : waypoints) {
            try {
                w.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Handler implements IMessageHandler<MsgWaypointsList, IMessage> {

        @Override
        public IMessage onMessage(MsgWaypointsList message, MessageContext ctx) {
            FMLCommonHandler.instance().showGuiScreen(new GuiWaypoints(message.srcWaypointId, message.waypoints));
            return null;
        }
    }
}