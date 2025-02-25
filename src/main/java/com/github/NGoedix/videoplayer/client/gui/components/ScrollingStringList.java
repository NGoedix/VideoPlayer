package com.github.NGoedix.videoplayer.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.List;

public class ScrollingStringList extends ScrollingList<ScrollingStringList.PlayerSlot> {
    private static final int SLOT_HEIGHT = 30;

    public interface PlayerSlotClickListener {
        void onClick(String text);
    }

    private PlayerSlotClickListener playerSlotClickListener;

    public ScrollingStringList(int x, int y, int width, int height, List<String> text) {
        super(x, y, width, height, SLOT_HEIGHT);
        this.updateEntries(text);
    }

    public void setPlayerSlotClickListener(PlayerSlotClickListener playerSlotClickListener) {
        this.playerSlotClickListener = playerSlotClickListener;
    }

    public String getSelectedText() {
        return this.getSelected().getText();
    }

    public void setSelected(String entry) {
        for (int i = 0; i < this.children().size(); i++) {
            PlayerSlot slot = (PlayerSlot) this.children().get(i);
            if (slot.getText().equals(entry)) {
                this.setSelected(slot);
                break;
            }
        }
    }

    @Override
    public void setSelected(PlayerSlot entry) {
        super.setSelected(entry);
        if (entry != null && this.playerSlotClickListener != null) {
            this.playerSlotClickListener.onClick(entry.getText());
        }
    }

    public void updateEntries(List<String> texts) {
        this.clearEntries();
        texts.forEach(text -> this.addEntry(new PlayerSlot(text, this)));
    }

    @Override
    protected void renderListBackground(GuiGraphics pGuiGraphics) {
        // Render background of the list
        super.renderListBackground(pGuiGraphics);

        // Render background of the slots
        int i = this.getRowLeft();
        int j = this.getRowTop(this.getItemCount());
        int k = this.getRowTop(0);

        // Render container background color gray
        pGuiGraphics.fillGradient(i, k - 4, i + this.getRowWidth(), j + 4, -1072689136, -804253680);

        // Render container border color black
        pGuiGraphics.fillGradient(i, k - 4, i + 1, j + 4, -804253680, -804253680);
    }

    public class PlayerSlot extends Entry<PlayerSlot> {

        private final String text;
        private final ScrollingStringList parent;

        PlayerSlot(String text, ScrollingStringList parent) {
            this.text = text;
            this.parent = parent;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            this.parent.setSelected(this);
            return false;
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean p_93531_, float pPartialTick) {
            Font font = this.parent.minecraft.font;

            pGuiGraphics.fillGradient(pLeft, pTop, pLeft + pWidth, pTop + pHeight, -435154928, -435154928);

            // If the mouse is hovering over the slot, render the background
            if (pMouseX >= parent.getRowLeft() && pMouseX <= parent.getRowRight() && pMouseY >= pTop && pMouseY <= pTop + pHeight) {
                pGuiGraphics.fillGradient(pLeft, pTop, pLeft + pWidth, pTop + pHeight, -1072689136, -804253680);
            }

            pGuiGraphics.drawString(font, this.text, pLeft + 65, pTop + 10, Color.WHITE.getRGB());
        }
    }
}