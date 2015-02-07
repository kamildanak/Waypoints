package info.jbcs.minecraft.waypoints;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ReadOnlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;

public class ServerPacketHandler {

    @SubscribeEvent
    public void onServerPacket(ServerCustomPacketEvent event) throws IOException {
        EntityPlayerMP player = ((NetHandlerPlayServer)event.handler).playerEntity;
        ByteBuf bbis = new ReadOnlyByteBuf(event.packet.payload());
        final int currentWaypointId=bbis.readInt();
        final int action=bbis.readInt();
        final int destinationWaypointId=bbis.readInt();

        Waypoint src=Waypoint.getWaypoint(currentWaypointId);
        if(src==null) return;

        if(! BlockWaypoint.isPlayerOnWaypoint(player.worldObj,src.x,src.y,src.z,player))
            return;

        Waypoint w=Waypoint.getWaypoint(destinationWaypointId);
        if(w==null) return;

        switch(action){
            case 0:
                player.mountEntity((Entity)null);
                ByteBuf buffer1 = Unpooled.buffer();
                buffer1.writeInt(3);
                buffer1.writeDouble(player.posX);
                buffer1.writeDouble(player.posY);
                buffer1.writeDouble(player.posZ);
                FMLProxyPacket packet1 = new FMLProxyPacket(buffer1.copy(), "Waypoints");

                if(player.dimension!=w.dimension) player.travelToDimension(w.dimension);
                int size = BlockWaypoint.checkSize(player.worldObj, w.x, w.y, w.z);
                player.setLocationAndAngles(w.x+size/2.0, w.y+0.5, w.z+size/2.0, player.rotationYaw, 0);
                player.setPositionAndUpdate(w.x+size/2.0, w.y+0.5, w.z+size/2.0);

                ByteBuf buffer = Unpooled.buffer();
                buffer.writeInt(3);
                buffer.writeDouble(player.posX);
                buffer.writeDouble(player.posY);
                buffer.writeDouble(player.posZ);
                FMLProxyPacket packet = new FMLProxyPacket(buffer.copy(), "Waypoints");
                Waypoints.Channel.sendToAllAround(packet1, new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 100));
                Waypoints.Channel.sendToAllAround(packet, new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 100));

                break;
            case 1:
                WaypointPlayerInfo info=WaypointPlayerInfo.get(player.getDisplayName());
                if(info==null) return;
                info.removeWaypoint(destinationWaypointId);
                break;
            case 2:
                if(! src.name.isEmpty()) return;
                src.name = ByteBufUtils.readUTF8String(bbis);
                src.changed=true;

                WaypointPlayerInfo info1=WaypointPlayerInfo.get(player.getDisplayName());
                if(info1==null) return;
                info1.addWaypoint(src.id);

                Packets.sendWaypointsToPlayer((EntityPlayerMP) player, src.id);
                break;
        }
    }
}
