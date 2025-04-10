package com.github.NGoedix.watchvideo.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class ImageButtonHoverable extends Button {

    private final ResourceLocation resourceLocation;
    private final ResourceLocation hoverResourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;

    public ImageButtonHoverable(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, ResourceLocation pHoverLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pHoverLocation, pTextureWidth, pTextureHeight, pOnPress, TextComponent.EMPTY);
    }

    public ImageButtonHoverable(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, ResourceLocation pHoverLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Component pMessage) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pHoverLocation, pTextureWidth, pTextureHeight, pOnPress, NO_TOOLTIP, pMessage);
    }

    public ImageButtonHoverable(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, ResourceLocation pHoverLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Button.OnTooltip pOnTooltip, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.yDiffTex = pYDiffTex;
        this.resourceLocation = pResourceLocation;
        this.hoverResourceLocation = pHoverLocation;
    }

    @Override
    public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        ResourceLocation texture = getTextureLocation();
        if (texture != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem._setShaderTexture(0, texture);

            int i = this.yTexStart;
            if (this.isHovered) {
                i += this.yDiffTex;
            }

            RenderSystem.enableDepthTest();
            blit(pMatrixStack, this.x, this.y, (float)this.xTexStart, (float) i, this.width, this.height, this.textureWidth, this.textureHeight);
        }
    }

    public ResourceLocation getTextureLocation() {
        return isHovered ? this.hoverResourceLocation : this.resourceLocation;
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
