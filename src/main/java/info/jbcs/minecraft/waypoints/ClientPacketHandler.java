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
import net.minecraft.world.World;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ClientPacketHandler extends ServerPacketHandler{

    @SubscribeEvent
    public void onClientPacket(ClientCustomPacketEvent event) throws IOException {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        World world = player.getEntityWorld();
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
        }else if(type == 3) {
            double x = bbis.readDouble();
            double y = bbis.readDouble()+2;
            double z = bbis.readDouble();
            for(int ex=0; ex<8; ex++){
                for(int ey=0; ey<8; ey++){
                    for(int ez=0; ez<8; ez++){
                        world.spawnParticle("reddust", x-1+ex/4.0, y-1.8+ey/4.0, z-1+ez/4.0, 110.0D/250, 25.0D/250, 130.0D/250);
                    }
                }
            }
        }
    }

}