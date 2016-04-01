package info.jbcs.minecraft.waypoints;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class General {
    public static boolean isOP(EntityPlayer player) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(player.getGameProfile());
    }
}
