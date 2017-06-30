package info.jbcs.minecraft.waypoints.init;

import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import net.minecraft.block.Block;

public class WaypointsBlocks {
    public static final BlockWaypoint WAYPOINT;
    static final Block[] BLOCKS;

    static {
        WAYPOINT = new BlockWaypoint("waypoint");
        BLOCKS = new Block[]{WAYPOINT};
    }
}
