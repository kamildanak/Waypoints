package info.jbcs.minecraft.waypoints.proxy;

import info.jbcs.minecraft.waypoints.network.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Proxy {
    public void preInit() {
    }

    public void init() {
    }

    public void registerPackets() {
        PacketDispatcher.registerMessage(MsgEditWaypoint.class);
        PacketDispatcher.registerMessage(MsgNameWaypoint.class);
        PacketDispatcher.registerMessage(MsgRedDust.class);
        PacketDispatcher.registerMessage(MsgWaypointsList.class);
        PacketDispatcher.registerMessage(MsgEdit.class); //Server
        PacketDispatcher.registerMessage(MsgName.class);
        PacketDispatcher.registerMessage(MsgDelete.class);
        PacketDispatcher.registerMessage(MsgTeleport.class);
    }

    /**
     * Returns a side-appropriate EntityPlayer for use during message handling
     */
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity;
    }

    /**
     * Returns the current thread based on side during message handling,
     * used for ensuring that the message is being handled by the main thread
     */
    public IThreadListener getThreadFromContext(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity.getServer();
    }
}
