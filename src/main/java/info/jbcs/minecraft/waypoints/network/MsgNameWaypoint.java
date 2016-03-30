package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.gui.GuiNameWaypoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class MsgNameWaypoint extends AbstractMessage.AbstractClientMessage<MsgNameWaypoint> {
    private Waypoint w;
    private String name;

    public MsgNameWaypoint() {
    }

    public MsgNameWaypoint(Waypoint w, String name) {
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
        FMLCommonHandler.instance().showGuiScreen(new GuiNameWaypoint(this.w, this.name));
    }
}