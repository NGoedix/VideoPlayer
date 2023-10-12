package com.github.NGoedix.videoplayer.block;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.VideoPlayer;
import com.github.NGoedix.videoplayer.block.custom.TVBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final Block TV_BLOCK = registerBlock("tv_block",
            new TVBlock(FabricBlockSettings.of(Material.METAL).nonOpaque().requiresTool().sounds(BlockSoundGroup.METAL).luminance(litBlockEmission(12)).strength(3.5F, 6.0F)),
            VideoPlayer.VIDEO_PLAYER_TAB);

    private static ToIntFunction<BlockState> litBlockEmission(int pLightValue) {
        return (blockstate) -> blockstate.get(Properties.LIT) ? pLightValue : 0;
    }

    private static Block registerBlock(String name, Block block, ItemGroup group) {
        registerBlockItem(name, block, group);
        return Registry.register(Registry.BLOCK, new Identifier(Reference.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {
        return Registry.register(Registry.ITEM, new Identifier(Reference.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings().group(group)));
    }

    public static void registerModBlocks() {
        Reference.LOGGER.info("Registering block for " + Reference.MOD_ID);
    }
}
