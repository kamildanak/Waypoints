package info.jbcs.minecraft.waypoints.gui;

import com.kamildanak.minecraft.foamflower.gui.elements.GuiExButton;
import info.jbcs.minecraft.waypoints.Waypoint;

public class GuiWaypointButton extends GuiExButton {
    Waypoint waypoint;
    boolean selected = false;

    public GuiWaypointButton(int x, int y, int w, int h, Waypoint wp) {
        super(x, y, w, h, wp.name, "waypoints:textures/widgets.png");

        waypoint = wp;
    }

    /*
    @Override
    public void preRender()
    {
        disabled = selected;
        if(waypoint == null)
        {
            setCaption("Deleted");
        } else {
            String dimName = "" + waypoint.dimension;
            if (!Waypoints.compactView) {
                try {
                    dimName = DimensionManager.getProvider(waypoint.dimension).getDimensionType().getName();
                } catch (Throwable e) {
                }
                gui.drawCenteredString("Dimension: " + dimName, x + 4, y + 14, 0xff808080);
                gui.drawCenteredString("Coordinates: (" + waypoint.pos.getX() + ", " + waypoint.pos.getY() + ", " + waypoint.pos.getZ() + ")", x + 4, y + 25, 0xff808080);
            }
        }
    }*/

}
