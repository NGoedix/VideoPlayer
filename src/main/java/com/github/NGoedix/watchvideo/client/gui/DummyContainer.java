package com.github.NGoedix.watchvideo.client.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DummyContainer extends AbstractContainerMenu {

    protected DummyContainer() {
        super(null, 0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
}
