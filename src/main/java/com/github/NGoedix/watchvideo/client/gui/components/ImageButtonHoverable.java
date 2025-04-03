package com.github.NGoedix.watchvideo.client.gui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ImageButtonHoverable extends Button {
    private final ResourceLocation resourceLocation;
    private final ResourceLocation hoverResourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;

    public ImageButtonHoverable(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, ResourceLocation pHoverLocation, int pTextureWidth, int pTextureHeight, Button.IPressable pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pHoverLocation, pTextureWidth, pTextureHeight, pOnPress, StringTextComponent.EMPTY);
    }

    public ImageButtonHoverable(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, ResourceLocation pHoverLocation, int pTextureWidth, int pTextureHeight, Button.IPressable pOnPress, ITextComponent pMessage) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pHoverLocation, pTextureWidth, pTextureHeight, pOnPress, NO_TOOLTIP, pMessage);
    }

    public ImageButtonHoverable(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, ResourceLocation pHoverLocation, int pTextureWidth, int pTextureHeight, Button.IPressable pOnPress, Button.ITooltip pOnTooltip, ITextComponent pMessage) {
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
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(isHovered ? this.hoverResourceLocation : this.resourceLocation);
        int i = this.yTexStart;
        if (this.isHovered()) {
            i += this.yDiffTex;
        }

        RenderSystem.enableDepthTest();
        blit(pMatrixStack, this.x, this.y, (float)this.xTexStart, (float) i, this.width, this.height, this.textureWidth, this.textureHeight);
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
