package com.lapaman.watchvideo.client.gui;

import com.lapaman.watchvideo.Reference;
import com.lapaman.watchvideo.WatchVideoMod;
import com.lapaman.watchvideo.util.TextureCache;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class VideoContainer extends GuiContainer {

    public static final ResourceLocation LOADING_IMAGE = new ResourceLocation(Reference.MOD_ID, "loading.jpg");


    public VideoContainer() {
        super(new ContainerImage());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawImage();
    }

    public void drawImage() {
        // TODO TESTEAR RENDIMIENTO
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.pushMatrix();

        ResourceLocation location = TextureCache.instance().getImage();

        float imageWidth = 12F;
        float imageHeight = 8F;

        if (location == null) {
            mc.getTextureManager().bindTexture(LOADING_IMAGE);
        } else {
            mc.getTextureManager().bindTexture(location);
            BufferedImage image = TextureCache.instance().getBufferedImage();
            imageWidth = (float) image.getWidth();
            imageHeight = (float) image.getHeight();
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float scale = 1F;

        float ws = (float) width * scale;
        float hs = (float) height * scale;

        float rs = ws / hs;
        float ri = imageWidth / imageHeight;

        float hnew;
        float wnew;

        if (rs > ri) {
            wnew = imageWidth * hs / imageHeight;
            hnew = hs;
        } else {
            wnew = ws;
            hnew = imageHeight * ws / imageWidth;
        }

        float top = (hs - hnew) / 2F;
        float left = (ws - wnew) / 2F;

        left += ((1F-scale) * ws) / 2F;
        top += ((1F-scale) * hs) / 2F;

        buffer.pos(left, top, zLevel).tex(0D, 0D).endVertex();
        buffer.pos(left, top + hnew, zLevel).tex(0D, 1D).endVertex();
        buffer.pos(left + wnew, top + hnew, zLevel).tex(1D, 1D).endVertex();
        buffer.pos(left + wnew, top, zLevel).tex(1D, 0D).endVertex();

        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
