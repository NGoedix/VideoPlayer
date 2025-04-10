package com.github.NGoedix.watchvideo.util;

import com.github.NGoedix.watchvideo.util.math.VideoMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import org.lwjgl.opengl.GL11;

public class VideoRenderer {

    public static void renderTexture(PoseStack matrix, int texture, float alpha, int x, int y, int offsetX, int offsetY, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        GuiComponent.blit(matrix, x, y, offsetX, offsetY, width, height, width, height);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    public static void drawString(PoseStack stack, String text, int height) {
        GuiComponent.drawString(stack, Minecraft.getInstance().font, text, 5, height, 0xffffff);
    }

    public static double applyEasing(int mode, double start, double end, double t) {
        switch (mode) {
            case 0:
                return VideoMathUtil.easeIn(start, end, t);
            case 1:
                return VideoMathUtil.easeOut(start, end, t);
            default:
                return end;
        }
    }
}
