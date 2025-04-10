package com.github.NGoedix.watchvideo.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class CustomSlider extends AbstractSliderButton {

    public interface OnSlide {
        void onSlide(double value);
    }

    private final Component text;
    private final boolean progressBar;
    private OnSlide onSlideListener;

    private boolean active;

    public CustomSlider(int x, int y, int width, int height, Component text, double defaultValue, boolean progressBar) {
        super(x, y, width, height, text == null ? new TextComponent("") : text, defaultValue);
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
            setMessage(new TranslatableComponent("customslider.videoplayer.value", this.text, formattedValue));
        }
    }

    @Override
    public void renderButton(@Nonnull PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font fontrenderer = minecraft.font;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem._setShaderTexture(0, WIDGETS_LOCATION);
        int i = this.getYImage(this.isHovered);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(pMatrixStack, this.x, this.y, this.width / 2, this.height, 0, 46 + i * 20, this.width / 2, 20, 256, 256);
        blit(pMatrixStack, this.x + this.width / 2, this.y, this.width / 2, this.height, 200 - this.width / 2f, 46 + i * 20, this.width / 2, 20, 256, 256);

        if (progressBar) {
            RenderSystem.disableTexture();
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 0.2F);
            double clampedValue = Mth.clamp(this.value, 0.0, 0.99);
            int progressBarWidth = (int) (this.width * clampedValue);
            GuiComponent.fill(pMatrixStack, this.x, this.y, this.x + progressBarWidth, this.y + this.height, 0x3300FF00);
            RenderSystem.enableTexture();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
        int j = getFGColor();
        drawCenteredString(pMatrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    protected void renderBg(@Nonnull PoseStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
        RenderSystem._setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isHovered ? 2 : 1) * 20;

        double clampedValue = Mth.clamp(this.value, 0.0, 0.99);
        int thumbX = this.x + (int) (clampedValue * (double) (this.width - 8));
        blit(pMatrixStack, thumbX, this.y, 4, height, 0, 46 + i, 4, 20, 256, 256);
        blit(pMatrixStack, thumbX + 4, this.y, 4, height, 196, 46 + i, 4, 20, 256, 256);
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