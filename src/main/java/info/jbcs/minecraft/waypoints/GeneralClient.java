package info.jbcs.minecraft.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Random;

public class GeneralClient {
    public static Random rand = new Random();

    static HashMap<String, ResourceLocation> resources = new HashMap<String, ResourceLocation>();

    public static void bind(String textureName) {
        ResourceLocation res = resources.get(textureName);

        if (res == null) {
            res = new ResourceLocation(textureName);
            resources.put(textureName, res);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
    }
}
