package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class MsgEdit extends AbstractMessage.AbstractServerMessage<MsgEdit> {
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
    protected void read(PacketBuffer buffer) throws IOException {
        waypointId = buffer.readInt();
        name = ByteBufUtils.readUTF8String(buffer);
        linked_id = buffer.readInt();
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(waypointId);
        ByteBufUtils.writeUTF8String(buffer, name);
        buffer.writeInt(linked_id);
    }

    @Override
    public void process(EntityPlayer player, Side side) {
        Waypoint w = Waypoint.getWaypoint(waypointId);
        if (w == null) return;

        if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, w.pos, player))
            return;

        w.name = name;
        w.linked_id = linked_id;
        w.changed = true;
    }

}