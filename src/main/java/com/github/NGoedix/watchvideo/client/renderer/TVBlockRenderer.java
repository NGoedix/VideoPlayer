package com.github.NGoedix.watchvideo.client.renderer;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.block.entity.TVBlockEntity;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.math.*;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TVBlockRenderer extends TileEntitySpecialRenderer<TVBlockEntity> {

    private static BufferedImage blackTextureBuffer = null;
    private static ImageRenderer blackTexture = null;

    private final Timer tickTimer;
    private int tick = 0;

    public TVBlockRenderer() {
        tickTimer = new Timer(20);
        if (blackTextureBuffer == null) {
            blackTextureBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            blackTextureBuffer.setRGB(0, 0, Color.BLACK.getRGB());
            blackTexture = ImageAPI.renderer(blackTextureBuffer);
        }
    }

    public static void applyDirection(EnumFacing direction) {
        int rotation = 0;
        switch (direction) {
            case EAST:
                rotation = 0;
                break;
            case NORTH:
                rotation = 90;
                break;
            case SOUTH:
                rotation = 270;
                break;
            case WEST:
                rotation = 180;
                break;
            case UP:
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(-90, 0, 0, 1);
                break;
            case DOWN:
                GL11.glRotated(-90, 1, 0, 0);
                GL11.glRotated(-90, 0, 0, 1);
                break;
            default:
                break;
        }
        GL11.glRotated(rotation, 0, 1, 0);
    }

    @Override
    public void render(TVBlockEntity frame, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (frame.isURLEmpty()) {
            if (frame.display != null) frame.display.release();
            return;
        }

        IDisplay display = frame.requestDisplay();
        if (display == null) {
            if (!frame.isPlaying()) return;
            tickTimer.updateTimer();
            tick += tickTimer.elapsedTicks;
            renderTexture(frame, x, y, z, null, ImageAPI.loadingGif().texture(tick, 1, true), true);
            return;
        }

        int texture = display.prepare(frame.getUrl(), frame.getVolume() * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER), frame.minDistance, frame.maxDistance, frame.isPlaying(), frame.isLoop(), frame.getTick());

        if (texture == -1) {
            return;
        }

        renderTexture(frame, x, y, z, display, blackTexture.texture(1, 1, false), false);
        renderTexture(frame, x, y, z, display, texture, true);
    }

    private void renderTexture(TVBlockEntity frame, double x, double y, double z, IDisplay display, int texture, boolean mustAspectRatio) {
        float sizeX = frame.getSizeX();
        float sizeY = frame.getSizeY();

        double originalPosX = -0.7 + sizeX / 2D;

        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.bindTexture(texture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GlStateManager.pushMatrix();

        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        EnumFacing direction = EnumFacing.getFront(frame.getBlockMetadata());
        applyDirection(direction);
        if (direction == EnumFacing.UP || direction == EnumFacing.DOWN)
            GL11.glRotatef(90, 0, 1, 0);

        // BEGIN ASPECT RATIO
        if (mustAspectRatio) {
            float videoAspectRatio = 1.0f;
            if (display != null) {
                Dimension dimensions = display.getDimensions();
                if (dimensions != null) {
                    // Adjust based on dimensions
                    videoAspectRatio = (float) (dimensions.getWidth() / (float) dimensions.getHeight());
                }
            }

            float height = frame.getSizeY();
            float width = frame.getSizeX();

            float screenAspectRatio = width / height;
            float w = height * videoAspectRatio;
            float h = width / videoAspectRatio;

            if(videoAspectRatio > screenAspectRatio) {
                sizeY = h;
                GL11.glTranslated(0, (height - h) / 2F, 0);
            } else {
                sizeX = w;
                GL11.glTranslated((width - w) / 2F, 0, (width - w) / 2F);
            }

            float difference = height - width;

            if (difference > 0) {
                sizeY = (frame.getSizeY() - difference / 2);  // Shrink the height
                sizeX = frame.getSizeX() + difference / 2;
            }
        }
        // END ASPECT RATIO

        // Adjust posX value
        double posX = -0.7 + sizeX / 2D;
        double posY = -0.43 + sizeY / 2D;

        GL11.glRotated(0, 1, 0, 0);
        GL11.glRotated(0, 0, 1, 0);
        GL11.glRotated(0, 0, 0, 1);

        GL11.glTranslated(mustAspectRatio ? -0.47 + (posX - originalPosX) : -0.4701, posY, posX);

        GlStateManager.enableRescaleNormal();
        GL11.glScaled(1, sizeY, sizeX);

        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glNormal3f(1.0f, 0.0F, 0.0f);

        GL11.glTexCoord3f(1, 1, 0);
        GL11.glVertex3f(0.5F, -0.5f, -0.5f);
        GL11.glTexCoord3f(1, 0, 0);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord3f(0, 0, 0);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord3f(0, 1, 0);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glEnd();

        GlStateManager.popMatrix();

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    @Override
    public boolean isGlobalRenderer(TVBlockEntity te) {
        return false;
    }
}
