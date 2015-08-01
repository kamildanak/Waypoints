package info.jbcs.minecraft.waypoints.gui;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import info.jbcs.minecraft.waypoints.Waypoints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GuiNameWaypoint extends GuiScreenPlus {
    GuiEdit nameEdit;

    public GuiNameWaypoint(final int currentWaypointId, String suggestedName) {
        super(117, 73, "waypoints:textures/gui-name-waypoint.png");

        addChild(new GuiLabel(9, 9, "Name waypoint:"));
        addChild(nameEdit = new GuiEdit(8, 23, 101, 13));
        nameEdit.setText(suggestedName);

        addChild(new GuiExButton(7, 45, 49, 20, "OK") {
            @Override
            public void onClick() {
                ByteBuf buffer = Unpooled.buffer();
                buffer.writeInt(currentWaypointId);
                buffer.writeInt(2);
                buffer.writeInt(currentWaypointId);
                ByteBufUtils.writeUTF8String(buffer, nameEdit.getText());
                FMLProxyPacket packet = new FMLProxyPacket(buffer.copy(), "Waypoints");

                Waypoints.Channel.sendToServer(packet);
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
