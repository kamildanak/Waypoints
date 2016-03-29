package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class MsgName extends AbstractMessage.AbstractServerMessage<MsgName> {
    private Waypoint w;
    private String name;

    public MsgName() {
    }

    public MsgName(Waypoint w, String name) {
        this.w = w;
        this.name = name;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException {
        w = Waypoint.getWaypoint(buffer.readInt());
        name = ByteBufUtils.readUTF8String(buffer);

    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(w.id);
        ByteBufUtils.writeUTF8String(buffer, name);

    }

    @Override
    public void process(EntityPlayer player, Side side) {
        if (w == null) return ;

        if (!BlockWaypoint.isEntityOnWaypoint(player.worldObj, w.pos, player))
            return;

        w.name = name;
        w.changed = true;
    }

}