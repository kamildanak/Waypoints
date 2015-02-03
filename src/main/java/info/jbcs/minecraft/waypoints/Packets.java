package info.jbcs.minecraft.waypoints;

import cpw.mods.fml.common.network.ByteBufUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.FMLCommonHandler;

public class Packets {


	public static void sendWaypointsToPlayer(EntityPlayerMP player,final int srcWaypointId) throws IOException {
		final WaypointPlayerInfo info=WaypointPlayerInfo.get(player.getDisplayName());
		if(info==null) return;
		
		info.addWaypoint(srcWaypointId);

        int type = 1;
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(type);

        buffer.writeInt(srcWaypointId);
        int count=0;
        for(Waypoint w: Waypoint.existingWaypoints)
            if(info.discoveredWaypoints.containsKey(w.id))
                count++;

        buffer.writeInt(count);

        for(Waypoint w: Waypoint.existingWaypoints)
            if(info.discoveredWaypoints.containsKey(w.id))
                w.write(buffer);

        FMLProxyPacket packet = new FMLProxyPacket(buffer.copy(), "Waypoints");
        Waypoints.Channel.sendTo(packet, player);
	}

}
