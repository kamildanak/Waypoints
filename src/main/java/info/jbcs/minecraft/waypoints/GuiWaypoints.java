package info.jbcs.minecraft.waypoints;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class GuiWaypoints extends GuiScreenPlus {
	ArrayList<Waypoint> waypoints;
	int currentWaypointId;
	
	ArrayList<GuiWaypointButton> waypointButtons=new ArrayList<GuiWaypointButton>();
	GuiWaypointButton selectedButton;
	GuilScrolledBox scroller;
	
	GuiExButton gotoButton;
	GuiExButton deleteButton;
	
	public GuiWaypoints(int cid,ArrayList<Waypoint> l) {
		super(227, 227, "waypoints:textures/gui-waypoints.png");
		
		waypoints=l;
		currentWaypointId=cid;

		addChild(scroller=new GuilScrolledBox(0, 22, 227, 199-12-22));
		
		int buttonHeight=Waypoints.compactView?14:36;
		
		for(final Waypoint w: waypoints){
			GuiWaypointButton button;
			
			scroller.addChild(button=new GuiWaypointButton(0, 4+(buttonHeight+4)*waypointButtons.size(), 220, buttonHeight, w) {
				@Override
				public void onClick() {
					if(selectedButton==this){
						if(send(0))
							closeWaypoints();
						
						return;
					}
					
					for(GuiWaypointButton button: waypointButtons)
						button.selected=button==this;
					
					selectedButton=this;
				}
			});
			
			if(w.id==currentWaypointId){
				button.selected=true;
				selectedButton=button;
				
				scroller.offset=50-buttonHeight*waypointButtons.size();
			}
			
			waypointButtons.add(button);
		}
		
		
		addChild(gotoButton=new GuiExButton(12, 199, 64, 20, "Go to") {
			@Override
			public void onClick() {
				if(send(0))
					closeWaypoints();
			}
		});
		
		addChild(deleteButton=new GuiExButton(82, 199, 64, 20, "Delete") {
			@Override
			public void onClick() {
				send(1);
				selectedButton.waypoint=null;
			}
		});
		
		addChild(new GuiExButton(152, 199, 64, 20, "Cancel") {
			@Override
			public void onClick() {
				closeWaypoints();
			}
		});
	}
	
	boolean send(final int action){
		if(selectedButton==null) return false;
		final Waypoint wp=selectedButton.waypoint;
		if(wp==null) return false;

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(currentWaypointId);
        buffer.writeInt(action);
        buffer.writeInt(wp.id);
        FMLProxyPacket packet = new FMLProxyPacket(buffer.copy(), "Waypoints");
        Waypoints.Channel.sendToServer(packet);

		return true;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
        GL11.glPushMatrix();
        GL11.glTranslatef(screenX, screenY, 0);
        root.render();
        GL11.glPopMatrix();
		
		drawCenteredStringWithShadow("Waypoints", screenX+114, screenY+12, 0xffffffff);
	}
	

	@Override
	protected void drawGuiContainerForegroundLayer(int fx, int fy) {
	}
	
	public void closeWaypoints(){
		Minecraft.getMinecraft().thePlayer.closeScreen();
		inventorySlots.onContainerClosed(Minecraft.getMinecraft().thePlayer);
		close();		
	}

}
