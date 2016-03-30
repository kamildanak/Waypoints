package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.gui.GuiWaypoints;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.ArrayList;

public class MsgWaypointsList extends AbstractMessage.AbstractClientMessage<MsgWaypointsList> {
    private ArrayList<Waypoint> waypoints;
    private int srcWaypointId;

    public MsgWaypointsList() {
    }

    public MsgWaypointsList(int srcWaypointId, ArrayList<Waypoint> waypoints) {
        this.srcWaypointId = srcWaypointId;
        this.waypoints = waypoints;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException {
        srcWaypointId = buffer.readInt();
        int count = buffer.readInt();

        waypoints = new ArrayList<Waypoint>();
        for (int i = 0; i < count; i++) {
            try {
                waypoints.add(new Waypoint(buffer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(srcWaypointId);
        buffer.writeInt(waypoints.size());
        for (Waypoint w : waypoints) {
            try {
                w.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void process(EntityPlayer player, Side side) {
        FMLCommonHandler.instance().showGuiScreen(new GuiWaypoints(srcWaypointId, waypoints));
    }
}