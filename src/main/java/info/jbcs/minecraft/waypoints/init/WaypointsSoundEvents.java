package info.jbcs.minecraft.waypoints.init;

import info.jbcs.minecraft.waypoints.Waypoints;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class WaypointsSoundEvents {
    static final SoundEvent[] SOUNDS;
    public static SoundEvent TELEPORT;

    static {
        ResourceLocation res_sound_teleport = new ResourceLocation(Waypoints.MOD_ID, "teleport");
        TELEPORT = new SoundEvent(res_sound_teleport).setRegistryName(res_sound_teleport);
        SOUNDS = new SoundEvent[]{TELEPORT};
    }
}
