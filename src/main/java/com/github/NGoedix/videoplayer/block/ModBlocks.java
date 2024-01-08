package com.github.NGoedix.videoplayer.block;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.VideoPlayer;
import com.github.NGoedix.videoplayer.block.custom.TVBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final Block TV_BLOCK = registerBlock("tv_block",
            new TVBlock(FabricBlockSettings.create().nonOpaque().requiresTool().sounds(BlockSoundGroup.METAL).luminance(litBlockEmission(12)).strength(3.5F, 6.0F)),
            VideoPlayer.VIDEO_PLAYER_TAB);

    private static Block registerBlockWithoutBlockItem(String name, Block block, ItemGroup group) {
        return Registry.register(Registries.BLOCK, new Identifier(Constants.MOD_ID, name), block);
    }

    private static ToIntFunction<BlockState> litBlockEmission(int pLightValue) {
        return (blockstate) -> blockstate.get(Properties.LIT) ? pLightValue : 0;
    }

    private static Block registerBlock(String name, Block block, ItemGroup group) {
        registerBlockItem(name, block, group);
        return Registry.register(Registries.BLOCK, new Identifier(Constants.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {
        return Registry.register(Registries.ITEM, new Identifier(Constants.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        Constants.LOGGER.info("Registering block for " + Constants.MOD_ID);
    }
}
