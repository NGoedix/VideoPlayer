package com.github.NGoedix.videoplayer.block;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.VideoPlayer;
import com.github.NGoedix.videoplayer.block.custom.RadioBlock;
import com.github.NGoedix.videoplayer.block.custom.TVBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final Block TV_BLOCK = registerBlock("tv_block",
            new TVBlock(BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).noOcclusion().requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(litBlockEmission(12)).strength(3.5F, 6.0F)),
            VideoPlayer.VIDEO_PLAYER_TAB);

    public static final Block RADIO_BLOCK = registerBlock("radio_block",
            new RadioBlock(BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).noOcclusion().requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(litBlockEmission(12)).strength(3.5F, 6.0F)),
            VideoPlayer.VIDEO_PLAYER_TAB);

    private static Block registerBlockWithoutBlockItem(String name, Block block, CreativeModeTab group) {
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name), block);
    }

    private static ToIntFunction<BlockState> litBlockEmission(int pLightValue) {
        return (blockstate) -> blockstate.getValue(RedstoneTorchBlock.LIT) ? pLightValue : 0;
    }

    private static Block registerBlock(String name, Block block, CreativeModeTab group) {
        registerBlockItem(name, block, group);
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, CreativeModeTab group) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name),
                new BlockItem(block, new Item.Properties()));
    }

    public static void registerModBlocks() {
        Reference.LOGGER.info("Registering block for " + Reference.MOD_ID);
    }
}
