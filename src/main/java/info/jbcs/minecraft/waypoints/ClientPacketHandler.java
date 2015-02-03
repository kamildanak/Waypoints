package info.jbcs.minecraft.waypoints;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ReadOnlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ClientPacketHandler extends ServerPacketHandler{

    @SubscribeEvent
    public void onClientPacket(ClientCustomPacketEvent event) throws IOException {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ByteBuf bbis = new ReadOnlyByteBuf(event.packet.payload());
        int type = bbis.readInt();
        if(type == 1) {
            ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
            final int yourCurrentWaypointId = bbis.readInt();
            final int count = bbis.readInt();

            for (int i = 0; i < count; i++) {
                Waypoint wp = new Waypoint(bbis);
                waypoints.add(wp);
            }

            FMLCommonHandler.instance().showGuiScreen(new GuiWaypoints(yourCurrentWaypointId, waypoints));
        }
        else if(type == 2) {
            int id = bbis.readInt();
            String name = ByteBufUtils.readUTF8String(bbis);

            FMLCommonHandler.instance().showGuiScreen(new GuiNameWaypoint(id, name));
        }
    }

}