package info.jbcs.minecraft.waypoints;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class General {
    public static boolean isOP(EntityPlayer player) {
        return MinecraftServer.getServer().getConfigurationManager().canSendCommands(player.getGameProfile());
    }
}
