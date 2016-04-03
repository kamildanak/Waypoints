package info.jbcs.minecraft.waypoints.gui;

import info.jbcs.minecraft.waypoints.network.MsgName;
import info.jbcs.minecraft.waypoints.network.PacketDispatcher;
import net.minecraft.util.BlockPos;

public class GuiNameWaypoint extends GuiScreenPlus {
    GuiEdit nameEdit;

    public GuiNameWaypoint(final BlockPos pos, final int waypointId, String suggestedName) {
        super(117, 73, "waypoints:textures/gui-name-waypoint.png");

        addChild(new GuiLabel(9, 9, "Name waypoint:"));
        addChild(nameEdit = new GuiEdit(8, 23, 101, 13));
        nameEdit.setText(suggestedName);

        addChild(new GuiExButton(7, 45, 49, 20, "OK") {
            @Override
            public void onClick() {
                MsgName msg = new MsgName(pos, waypointId, nameEdit.getText());
                PacketDispatcher.sendToServer(msg);
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
