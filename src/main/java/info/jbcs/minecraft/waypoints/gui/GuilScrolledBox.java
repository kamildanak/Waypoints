package info.jbcs.minecraft.waypoints.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class GuilScrolledBox extends GuiElement {
    public int offset = 0;
    int contentHeight = 0;
    int scrollingStart = -1;

    public GuilScrolledBox(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    int x0, x1;

    protected void overlayBackground(int start, int end, int color, int a1, int a2) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float a = 225;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;


        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        worldrenderer.pos(x0, end, 0.0D).tex(0.0D, end / f).color(r, g, b, a).endVertex();
        worldrenderer.pos(x1, end, 0.0D).tex(gui.width / f, end / f).color(r, g, b, a).endVertex();
        worldrenderer.pos(x1, start, 0.0D).tex(gui.width / f, start / f).color(r, g, b, a).endVertex();
        worldrenderer.pos(x0, start, 0.0D).tex(0.0D, start / f).color(r, g, b, a).endVertex();

        tessellator.draw();
    }


    @Override
    public GuiElement addChild(GuiElement e) {
        int childBottom = e.y + e.h - 18;
        if (childBottom > contentHeight)
            contentHeight = childBottom;

        return super.addChild(e);
    }


    @Override
    public void render() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.optionsBackground);
        x0 = -gui.screenX;
        x1 = -gui.screenX + gui.width;

        overlayBackground(y, h, 0x202020, 0xff, 0xff);

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, offset, 0.0f);
        super.render();
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        overlayBackground(y, y + 8, 0x000000, 0xff, 0x00);
        overlayBackground(h - 8, h, 0x000000, 0x00, 0xff);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.optionsBackground);
        overlayBackground(-gui.screenY, y, 0x404040, 0xff, 0xff);
        overlayBackground(h, -gui.screenY + gui.height, 0x404040, 0xff, 0xff);
    }

    @Override
    public void mouseMove(InputMouseEvent ev) {
        if (scrollingStart != -1) {
            offset = ev.y - scrollingStart;
            if (offset > contentHeight) offset = contentHeight;
            if (offset < -contentHeight) offset = -contentHeight;
        }

        ev.y -= offset;
        super.mouseMove(ev);
        ev.y += offset;
    }

    @Override
    public void mouseDown(InputMouseEvent ev) {
        scrollingStart = ev.y - offset;

        if (ev.y >= y && ev.y < y + h) {
            ev.y -= offset;
            super.mouseDown(ev);
            ev.y += offset;
        }
    }

    @Override
    public void mouseUp(InputMouseEvent ev) {
        scrollingStart = -1;

        if (ev.y >= y && ev.y < y + h) {
            ev.y -= offset;
            super.mouseUp(ev);
            ev.y += offset;
        }
    }
}


