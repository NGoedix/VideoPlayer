package com.github.NGoedix.watchvideo.block;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayerModTab;
import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

    public static final RegistryObject<Block> TV_BLOCK = registerBlock("tv_block",
            () -> new TVBlock(AbstractBlock.Properties.of(Material.HEAVY_METAL).noOcclusion().requiresCorrectToolForDrops().sound(SoundType.METAL).strength(3.5F, 6.0F)), VideoPlayerModTab.ALL);

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block,
                                                                            ItemGroup tab) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().tab(tab)));
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block,
                                                                     ItemGroup tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
