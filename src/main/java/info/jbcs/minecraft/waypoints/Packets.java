package info.jbcs.minecraft.waypoints;

import info.jbcs.minecraft.utilities.packets.PacketData;
import info.jbcs.minecraft.utilities.packets.PacketHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet;
import cpw.mods.fml.common.FMLCommonHandler;

public class Packets {

	static PacketHandler waypointsMenu = new PacketHandler("Waypoints menu") {
		@Override
		public void onData(DataInputStream stream, EntityPlayer player) throws IOException {
			switch(FMLCommonHandler.instance().getEffectiveSide()){
			case SERVER:
				final int currentWaypointId=stream.readInt();
				final int action=stream.readInt();
				final int destinationWaypointId=stream.readInt();
				
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
					WaypointPlayerInfo info=WaypointPlayerInfo.get(player.username);
					if(info==null) return;
					info.removeWaypoint(destinationWaypointId);
					break;
				case 2:
					if(! src.name.isEmpty()) return;
					src.name=Packet.readString(stream,32);
					src.changed=true;
					
					WaypointPlayerInfo info1=WaypointPlayerInfo.get(player.username);
					if(info1==null) return;
					info1.addWaypoint(src.id);
					
					sendWaypointsToPlayer((EntityPlayerMP) player,src.id);
					break;
				}
				
				break;
			case CLIENT:
				ArrayList<Waypoint> waypoints=new ArrayList<Waypoint>();
				final int yourCurrentWaypointId=stream.readInt();
				final int count=stream.readInt();

				for(int i=0;i<count;i++){
					Waypoint wp=new Waypoint(stream);
					waypoints.add(wp);
				}
				
		        FMLCommonHandler.instance().showGuiScreen(new GuiWaypoints(yourCurrentWaypointId,waypoints));
				
				break;
			default:
				break;
			}
		}
	};


	public static void sendWaypointsToPlayer(EntityPlayerMP player,final int srcWaypointId) {
		final WaypointPlayerInfo info=WaypointPlayerInfo.get(player.username);
		if(info==null) return;
		
		info.addWaypoint(srcWaypointId);
		
		waypointsMenu.sendToPlayer(player,new PacketData(){
			@Override
			public void data(DataOutputStream stream) throws IOException {
				stream.writeInt(srcWaypointId);
				int count=0;
				for(Waypoint w: Waypoint.existingWaypoints)
					if(info.discoveredWaypoints.containsKey(w.id))
						count++;
				
				stream.writeInt(count);
				
				for(Waypoint w: Waypoint.existingWaypoints)
					if(info.discoveredWaypoints.containsKey(w.id))
						w.write(stream);
			}
		});
	}
	
	static PacketHandler waypointName = new PacketHandler("Name waypoint") {

		@Override
		public void onData(DataInputStream stream, EntityPlayer player) throws IOException {
			int id=stream.readInt();
			String name=Packet.readString(stream,32);

	        FMLCommonHandler.instance().showGuiScreen(new GuiNameWaypoint(id,name));			
		}
	
	};

}
