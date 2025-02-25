package com.github.NGoedix.videoplayer.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CustomSlider extends AbstractSliderButton {
    private static final ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider");
    private static final ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_highlighted");
    private static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");
    private static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted");

    public interface OnSlide {
        void onSlide(double value);
    }

    private final Component text;
    private final boolean progressBar;
    private OnSlide onSlideListener;

    private boolean active;

    public CustomSlider(int x, int y, int width, int height, Component text, double defaultValue, boolean progressBar) {
        super(x, y, width, height, text == null ? Component.literal("") : text, defaultValue);
        this.text = text;
        this.progressBar = progressBar;
        this.active = true;
        updateMessage();
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setOnSlideListener(OnSlide onSlide) {
        this.onSlideListener = onSlide;
    }

    @Override
    protected void updateMessage() {
        if (text != null) {
            String formattedValue = String.format("%d", (int) (value * 100f));
            setMessage(Component.translatable("customslider.videoplayer.value", this.text, formattedValue));
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font fontrenderer = minecraft.font;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = 0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(SLIDER_SPRITE, this.getX(), this.getY(), this.width / 2, this.height);
        guiGraphics.blitSprite(SLIDER_SPRITE, this.getX() + this.width / 2, this.getY(), this.width / 2, this.height);

        if (progressBar) {
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 0.2F);
            int progressBarWidth = (int)(this.width * this.value);
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + progressBarWidth, this.getY() + this.height, 0x3300FF00);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        this.renderBg(guiGraphics, minecraft, pMouseX, pMouseY);
        int j = this.active ? 16777215 : 10526880;
        guiGraphics.drawCenteredString(fontrenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    protected void renderBg(GuiGraphics guiGraphics, Minecraft pMinecraft, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isHovered ? 2 : 1) * 20;

        var texture = this.isHovered ? SLIDER_HANDLE_HIGHLIGHTED_SPRITE : SLIDER_HANDLE_SPRITE;

        guiGraphics.blitSprite(texture, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 4, height);
        guiGraphics.blitSprite(texture, this.getX() + (int) (this.value * (double) (this.width - 8)) + 4, this.getY(), 4, height);
    }

    @Override
    protected void applyValue() {
        if (onSlideListener != null)
            onSlideListener.onSlide(value * 100f);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    protected boolean isValidClickButton(int pButton) {
        if (!active) return false;

        return super.isValidClickButton(pButton);
    }

    public double getValue() {
        return value * 100f;
    }
}