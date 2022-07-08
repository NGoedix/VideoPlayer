package com.lapaman.watchvideo.client.gui;

import com.lapaman.watchvideo.Reference;
import com.lapaman.watchvideo.util.VideoManager;
import com.lapaman.watchvideo.util.VideoTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class VideoContainer extends GuiContainer {

    public static final ResourceLocation LOADING_IMAGE = new ResourceLocation(Reference.MOD_ID, "loading.jpg");


    public VideoContainer() {
        super(new ContainerImage());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        try {
            drawImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void drawImage() throws IOException {
        if (!VideoManager.isStartedVideo()) {
            renderLoading();
            return;
        }

        // Render video
        createTexture(VideoTools.getFrame(VideoManager.getVideo()));
        render();
    }

    public int createTexture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];

                buffer.put((byte) ((pixel >> 16) & 0xFF)); // red
                buffer.put((byte) ((pixel >> 8) & 0xFF)); // green
                buffer.put((byte) (pixel & 0xFF)); // blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // alpha
            }
        }

        buffer.flip();

        int textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL_TEXTURE_2D, textureID);

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA8,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL_UNSIGNED_BYTE,
                buffer);

//        GL11.glBindTexture(GL_TEXTURE_2D, 0);

        return textureID;
    }

    public void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glTexCoord2f(0.0f, 0.0f);
            GL11.glVertex2f(0.0f, 0.0f);

            GL11.glTexCoord2f(1.0f, 0.0f);
            GL11.glVertex2f(Minecraft.getMinecraft().displayWidth, 0.0f);

            GL11.glTexCoord2f(1.0f, 1.0f);
            GL11.glVertex2f(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

            GL11.glTexCoord2f(0.0f, 1.0f);
            GL11.glVertex2f(0.0f, Minecraft.getMinecraft().displayHeight);
        }
        GL11.glEnd();
    }

    private void renderLoading() {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.pushMatrix();

        float imageWidth = 12F;
        float imageHeight = 8F;

        mc.getTextureManager().bindTexture(LOADING_IMAGE);

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

        left += ((1F - scale) * ws) / 2F;
        top += ((1F - scale) * hs) / 2F;

        buffer.pos(left, top, zLevel).tex(0D, 0D).endVertex();
        buffer.pos(left, top + hnew, zLevel).tex(0D, 1D).endVertex();
        buffer.pos(left + wnew, top + hnew, zLevel).tex(1D, 1D).endVertex();
        buffer.pos(left + wnew, top, zLevel).tex(1D, 0D).endVertex();

        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
