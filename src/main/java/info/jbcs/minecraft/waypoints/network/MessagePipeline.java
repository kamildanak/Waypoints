package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class MessagePipeline extends SimpleNetworkWrapper {
    public MessagePipeline() {
        super("Waypoints");
    }
}