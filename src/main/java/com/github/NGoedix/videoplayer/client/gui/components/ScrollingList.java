package com.github.NGoedix.videoplayer.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.lwjgl.opengl.GL11;

public class ScrollingList<E extends AbstractSelectionList.Entry<E>> extends AbstractSelectionList<E> {

    public ScrollingList(int x, int y, int width, int height, int slotHeightIn) {
        super(Minecraft.getInstance(), width, height, y - (height / 2), slotHeightIn);
        this.setX(x - (width / 2));
        this.setRenderHeader(false, 0);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(this.getX()  * scale), (int)(Minecraft.getInstance().getWindow().getHeight() - ((this.getY() + this.height) * scale)),
                (int)(this.width * scale), (int)(this.height * scale));

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override // @mcp: getScrollbarPosition = getScrollbarPosition
    protected int getScrollbarPosition() {
        return (this.getX() + this.width) - 6;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
