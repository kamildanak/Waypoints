package info.jbcs.minecraft.waypoints;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ReadOnlyByteBuf;
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
                if(player.dimension!=w.dimension) player.travelToDimension(w.dimension);
                player.setLocationAndAngles(w.x+1.1, w.y+0.5, w.z+1.1, player.rotationYaw, 0);
                player.setPositionAndUpdate(w.x+1.1, w.y+0.5, w.z+1.1);
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
