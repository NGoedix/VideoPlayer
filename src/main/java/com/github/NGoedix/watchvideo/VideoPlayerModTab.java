package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VideoPlayerModTab {

    public static final ItemGroup ALL = new ItemGroup("video_player_mod") {
        @Override
        public ITextComponent getDisplayName() {
            return new TranslationTextComponent("itemGroup.videoplayer.items");
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.TV_BLOCK.get());
        }
    };
}
