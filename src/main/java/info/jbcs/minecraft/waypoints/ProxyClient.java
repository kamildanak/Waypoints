package info.jbcs.minecraft.waypoints;

public class ProxyClient extends Proxy {
	@Override
	public void preInit() {
	}

	@Override
	public void init() {
        Waypoints.Channel.register(new ClientPacketHandler());
	}
}
