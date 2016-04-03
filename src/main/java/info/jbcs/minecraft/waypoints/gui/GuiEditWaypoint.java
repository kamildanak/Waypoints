package info.jbcs.minecraft.waypoints.gui;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.network.MsgEdit;
import info.jbcs.minecraft.waypoints.network.PacketDispatcher;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;

public class GuiEditWaypoint extends GuiScreenPlus {
    GuiEdit nameEdit;
    GuiExButton select_button, way_button;
    ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
    int waypointId;
    int linkedId;


    public GuiEditWaypoint(final int currentWaypointId, String suggestedName, final Integer linked_id, ArrayList<Waypoint> waypoints) {
        super(117, 106, "waypoints:textures/gui-edit-waypoint.png");
        this.waypoints = waypoints;

        waypointId = currentWaypointId;
        linkedId = linked_id;
        addChild(new GuiLabel(9, 9, "Waypoint name:"));
        addChild(nameEdit = new GuiEdit(8, 23, 101, 13));
        addChild(new GuiLabel(9, 42, "Linked waypoint:"));
        nameEdit.setText(suggestedName);

        addChild(select_button = new GuiExButton(9, 54, 100, 17, "") {
            @Override
            public void onClick() {
                aaa();
            }
        });
        refreshLinked();

        addChild(new GuiExButton(7, 77, 49, 20, "OK") {
            @Override
            public void onClick() {
                MsgEdit msg = new MsgEdit(currentWaypointId, nameEdit.getText(), linkedId);
                PacketDispatcher.sendToServer(msg);
                close();
            }
        });

        addChild(new GuiExButton(61, 77, 49, 20, "Cancel") {
            @Override
            public void onClick() {
                close();
            }
        });
    }

    public void aaa() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiPickWaypoint(waypointId, waypoints, this));
    }

    public void setLinkedWaypoint(Integer linked_id) {
        linkedId = linked_id;
        refreshLinked();
    }

    public void refreshLinked() {
        if (Waypoint.getWaypoint(linkedId - 1) != null) {
            String name = Waypoint.getWaypoint(linkedId - 1).name;
            if (name.length() > 16)
                name = name.substring(0, 13) + "...";
            select_button.caption = name;

        } else {
            linkedId = 0;
            select_button.caption = "None selected";
        }
    }

}
