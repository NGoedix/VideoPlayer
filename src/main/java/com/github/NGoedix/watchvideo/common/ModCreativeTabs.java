package com.github.NGoedix.watchvideo.common;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);

    public static RegistryObject<CreativeModeTab> TRAVELERS_BACKPACK = CREATIVE_MODE_TABS.register("videplayer", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModBlocks.TV_BLOCK.get()))
            .title(Component.translatable("itemGroup.videoplayer.video_player_tab")).displayItems(ModCreativeTabs::displayItems).build());

    public static void displayItems(CreativeModeTab.ItemDisplayParameters displayParameters, CreativeModeTab.Output output)
    {
        output.accept(ModBlocks.TV_BLOCK.get());
    }
}
