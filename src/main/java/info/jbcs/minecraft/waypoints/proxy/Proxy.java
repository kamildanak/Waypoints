package info.jbcs.minecraft.waypoints.proxy;

import cpw.mods.fml.relauncher.Side;
import info.jbcs.minecraft.waypoints.network.*;

public class Proxy {
    public void preInit() {
    }

    public void init() {
    }

    public void registerPackets(MessagePipeline pipeline) {
        pipeline.registerMessage(MsgEditWaypoint.Handler.class, MsgEditWaypoint.class, 0, Side.CLIENT);
        pipeline.registerMessage(MsgNameWaypoint.Handler.class, MsgNameWaypoint.class, 1, Side.CLIENT);
        pipeline.registerMessage(MsgRedDust.Handler.class, MsgRedDust.class, 2, Side.CLIENT);
        pipeline.registerMessage(MsgWaypointsList.Handler.class, MsgWaypointsList.class, 3, Side.CLIENT);
        pipeline.registerMessage(MsgEdit.Handler.class, MsgEdit.class, 4, Side.SERVER);
        pipeline.registerMessage(MsgName.Handler.class, MsgName.class, 5, Side.SERVER);
        pipeline.registerMessage(MsgDelete.Handler.class, MsgDelete.class, 6, Side.SERVER);
        pipeline.registerMessage(MsgTeleport.Handler.class, MsgTeleport.class, 7, Side.SERVER);
    }
}
