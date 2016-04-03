package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.gui.GuiNameWaypoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class MsgNameWaypoint extends AbstractMessage.AbstractClientMessage<MsgNameWaypoint> {
    private BlockPos pos;
    private int waypointId;
    private String name;

    public MsgNameWaypoint() {
    }

    public MsgNameWaypoint(BlockPos pos, int waypointId, String name) {
        this.pos = pos;
        this.waypointId = waypointId;
        this.name = name;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        pos = new BlockPos(x, y, z);
        waypointId = buffer.readInt();
        name = ByteBufUtils.readUTF8String(buffer);

    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
        buffer.writeInt(waypointId);
        ByteBufUtils.writeUTF8String(buffer, name);

    }

    @Override
    public void process(EntityPlayer player, Side side) {
        FMLCommonHandler.instance().showGuiScreen(new GuiNameWaypoint(this.pos, this.waypointId, this.name));
    }
}