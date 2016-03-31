package info.jbcs.minecraft.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class GeneralClient {
    private static HashMap<String, ResourceLocation> resources = new HashMap<String, ResourceLocation>();

    public static void bind(String textureName) {
        ResourceLocation res = resources.get(textureName);

        if (res == null) {
            res = new ResourceLocation(textureName);
            resources.put(textureName, res);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
    }
}
