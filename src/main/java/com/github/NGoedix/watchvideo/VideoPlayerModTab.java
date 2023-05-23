package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.block.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class VideoPlayerModTab {

    public static final CreativeModeTab ALL = new CreativeModeTab("video_player_mod") {
        @Override
        public Component getDisplayName() {
            return Component.translatable("itemGroup.videoplayer.items");
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.TV_BLOCK.get());
        }
    };
}
