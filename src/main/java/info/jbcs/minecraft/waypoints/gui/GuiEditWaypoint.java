package info.jbcs.minecraft.waypoints.gui;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.Waypoints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.ArrayList;

public class GuiEditWaypoint extends GuiScreenPlus {
    GuiEdit nameEdit;
    GuiExButton select_button, way_button;
    ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
    int waypointId;
    int linkedId;

    public GuiEditWaypoint(final int currentWaypointId, String suggestedName, Integer linked_id, final EntityPlayer player, final ByteBuf bbis) {
        super(117, 106, "waypoints:textures/gui-edit-waypoint.png");

        final int count = bbis.readInt();
        for (int i = 0; i < count; i++) {
            Waypoint wp = null;
            try {
                wp = new Waypoint(bbis);
            } catch (IOException e) {
                e.printStackTrace();
            }
            waypoints.add(wp);
        }
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
                ByteBuf buffer = Unpooled.buffer();
                buffer.writeInt(currentWaypointId);
                buffer.writeInt(3);
                buffer.writeInt(currentWaypointId);
                ByteBufUtils.writeUTF8String(buffer, nameEdit.getText());
                buffer.writeInt(linkedId);
                FMLProxyPacket packet = new FMLProxyPacket(buffer.copy(), "Waypoints");

                Waypoints.Channel.sendToServer(packet);
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
