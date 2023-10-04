package com.github.NGoedix.watchvideo.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelLoader;

public class CommonProxy {
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
    }

    public void openVideo(String url, int volume, boolean controlBlocked) {}
    public void openVideoGUI(BlockPos pos, String url, int tick, int volume, boolean loop) {}
    public void manageVideo(BlockPos pos, boolean playing, int tick) {}
}
