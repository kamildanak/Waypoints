package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import info.jbcs.minecraft.waypoints.Waypoints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;

public class Packets {


    public static void sendWaypointsToPlayer(EntityPlayerMP player, final int srcWaypointId) throws IOException {
        final WaypointPlayerInfo info = WaypointPlayerInfo.get(player.getDisplayName());
        if (info == null) return;

        info.addWaypoint(srcWaypointId);

        int type = 1;
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(type);

        buffer.writeInt(srcWaypointId);
        int count = 0;
        for (Waypoint w : Waypoint.existingWaypoints)
            if (info.discoveredWaypoints.containsKey(w.id))
                count++;

        buffer.writeInt(count);

        for (Waypoint w : Waypoint.existingWaypoints)
            if (info.discoveredWaypoints.containsKey(w.id))
                w.write(buffer);

        FMLProxyPacket packet = new FMLProxyPacket(buffer.copy(), "Waypoints");
        Waypoints.Channel.sendTo(packet, player);
    }

}
