package info.jbcs.minecraft.waypoints.proxy;

import info.jbcs.minecraft.waypoints.Waypoints;
import info.jbcs.minecraft.waypoints.network.ClientPacketHandler;

public class ProxyClient extends Proxy {
    @Override
    public void preInit() {
    }

    @Override
    public void init() {
        Waypoints.Channel.register(new ClientPacketHandler());
    }
}
