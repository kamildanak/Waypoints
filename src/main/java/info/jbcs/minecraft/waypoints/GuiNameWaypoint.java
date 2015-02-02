package info.jbcs.minecraft.waypoints;

import info.jbcs.minecraft.gui.GuiEdit;
import info.jbcs.minecraft.gui.GuiExButton;
import info.jbcs.minecraft.gui.GuiLabel;
import info.jbcs.minecraft.gui.GuiScreenPlus;
import info.jbcs.minecraft.utilities.packets.PacketData;

import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.packet.Packet;

public class GuiNameWaypoint extends GuiScreenPlus {
	GuiEdit nameEdit;
	
	public GuiNameWaypoint(final int currentWaypointId,String suggestedName) {
		super(117, 73, "waypoints:textures/gui-name-waypoint.png");
		
		addChild(new GuiLabel(9, 9, "Name waypoint:"));
		addChild(nameEdit = new GuiEdit(8, 23, 101, 13));
		nameEdit.setText(suggestedName);
		
		addChild(new GuiExButton(7, 45, 49, 20, "OK") {
			@Override
			public void onClick() {
				Packets.waypointsMenu.sendToServer(new PacketData(){
					@Override
					public void data(DataOutputStream stream) throws IOException {
						stream.writeInt(currentWaypointId);
						stream.writeInt(2);
						stream.writeInt(currentWaypointId);
						Packet.writeString(nameEdit.getText(),stream);
					}
				});
				close();
			}
		});

		addChild(new GuiExButton(61, 45, 49, 20, "Cancel") {
			@Override
			public void onClick() {
				close();
			}
		});


	}

}
