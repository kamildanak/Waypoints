package info.jbcs.minecraft.waypoints;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiPickWaypoint extends GuiScreenPlus {
	ArrayList<Waypoint> waypoints;
	int currentWaypointId;
    GuiEditWaypoint parent;

	ArrayList<GuiWaypointButton> waypointButtons=new ArrayList<GuiWaypointButton>();
	GuiWaypointButton selectedButton;
	GuilScrolledBox scroller;

	GuiExButton gotoButton;
	GuiExButton deleteButton;

	public GuiPickWaypoint(int cid, ArrayList<Waypoint> l, GuiEditWaypoint screen) {
		super(227, 227, "waypoints:textures/gui-waypoints.png");
		
		waypoints=l;
		currentWaypointId=cid;
        parent = screen;
        final GuiEditWaypoint pscreen = screen;

		addChild(scroller=new GuilScrolledBox(0, 22, 227, 199-12-22));
		
		int buttonHeight=Waypoints.compactView?14:36;
		
		for(final Waypoint w: waypoints){
			GuiWaypointButton button;
			
			scroller.addChild(button=new GuiWaypointButton(0, 4+(buttonHeight+4)*waypointButtons.size(), 220, buttonHeight, w) {
				@Override
				public void onClick() {
					if(selectedButton==this){
                        if(!(selectedButton==null)){
                            Waypoint wp=selectedButton.waypoint;
                            pscreen.setLinkedWaypoint(wp.id+1);
                        }else{
                            pscreen.setLinkedWaypoint(-1);
                        }
                        Minecraft.getMinecraft().displayGuiScreen(pscreen);
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
		


		addChild(new GuiExButton(152, 199, 64, 20, "Cancel") {
            @Override
            public void onClick() {
                closeWaypoints();
            }
        });
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


    @Override
    protected void actionPerformed(GuiButton button)
    {
        switch (button.id)
        {
            case 100:
                if(!(selectedButton==null)){
                    Waypoint wp=selectedButton.waypoint;
                    parent.setLinkedWaypoint(wp.id+1);
                }else{
                    parent.setLinkedWaypoint(-1);
                }
                Minecraft.getMinecraft().displayGuiScreen(parent);

        }
    }

    @Override
    public void initGui()
    {
        super.initGui();
        {
            buttonList.add(new GuiButton(100, guiLeft + 12, guiTop + 199, 64, 20, "Select"));
        }
    }

}
