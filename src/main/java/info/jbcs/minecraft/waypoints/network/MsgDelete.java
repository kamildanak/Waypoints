package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class MsgDelete extends AbstractMessage.AbstractServerMessage<MsgDelete> {
    private Waypoint w;

    public MsgDelete() {
    }

    public MsgDelete(Waypoint w) {
        this.w = w;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException {
        w = Waypoint.getWaypoint(buffer.readInt());
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(w.id);
    }

    @Override
    public void process(EntityPlayer player, Side side) {
        if (w == null) return;

        WaypointPlayerInfo info = WaypointPlayerInfo.get(player.getUniqueID().toString());
        if (info == null) return;
        info.removeWaypoint(w.id);
    }
}