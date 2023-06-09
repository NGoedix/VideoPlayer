package com.github.NGoedix.videoplayer.client.render.components;


import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class CustomSlider extends SliderWidget {

    private final Text text;

    public CustomSlider(int x, int y, int width, int height, Text text, double defaultValue) {
        super(x, y, width, height, text, defaultValue);
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        String formattedValue = String.format("%d", (int) (value * 100f));
        setMessage(new TranslatableText("customslider.videoplayer.value", this.text, formattedValue));
    }

    @Override
    protected void applyValue() {}

    public int getValue() {
        return (int) (value * 100f);
    }
}