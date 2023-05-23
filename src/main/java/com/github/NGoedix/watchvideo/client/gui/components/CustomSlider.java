package com.github.NGoedix.watchvideo.client.gui.components;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class CustomSlider extends AbstractSliderButton {

    private final Component text;

    public CustomSlider(int x, int y, int width, int height, Component text, double defaultValue) {
        super(x, y, width, height, text, defaultValue);
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        String formattedValue = String.format("%d", (int) (value * 100f));
        setMessage(Component.translatable("customslider.videoplayer.value", this.text, formattedValue));
    }

    @Override
    protected void applyValue() {}

    public int getValue() {
        return (int) (value * 100f);
    }
}