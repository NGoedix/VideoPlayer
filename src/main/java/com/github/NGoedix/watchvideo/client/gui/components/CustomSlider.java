package com.github.NGoedix.watchvideo.client.gui.components;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CustomSlider extends AbstractSlider {

    private final ITextComponent text;

    public CustomSlider(int x, int y, int width, int height, ITextComponent text, double defaultValue) {
        super(x, y, width, height, text, defaultValue);
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        String formattedValue = String.format("%d", (int) (value * 100f));
        setMessage(new TranslationTextComponent("customslider.videoplayer.value", this.text, formattedValue));
    }

    @Override
    protected void applyValue() {}

    public int getValue() {
        return (int) (value * 100f);
    }
}