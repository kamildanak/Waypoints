package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import info.jbcs.minecraft.waypoints.Waypoints;
import info.jbcs.minecraft.waypoints.gui.GuiEditWaypoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

public class MsgEditWaypoint extends AbstractMessage.AbstractClientMessage<MsgEditWaypoint> {
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
    protected void read(PacketBuffer buffer) throws IOException {
        id = buffer.readInt();
        name = ByteBufUtils.readUTF8String(buffer);
        linked_id = buffer.readInt();
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
        buffer.writeInt(id);
        ByteBufUtils.writeUTF8String(buffer, name);
        buffer.writeInt(linked_id);
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
        if(side.isServer()) return;
        FMLCommonHandler.instance().showGuiScreen(new GuiEditWaypoint(this.id, this.name, this.linked_id, this.waypoints));

    }
}