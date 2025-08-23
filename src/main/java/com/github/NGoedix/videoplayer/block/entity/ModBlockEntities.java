package com.github.NGoedix.videoplayer.block.entity;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.block.ModBlocks;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static BlockEntityType<TVBlockEntity> TV_BLOCK_ENTITY;
    public static BlockEntityType<TVBlockEntity> RADIO_BLOCK_ENTITY;
    public static BlockEntityType<TVBlockEntity> HAND_RADIO_BLOCK_ENTITY;

    public static void registerAllBlockEntities() {
        TV_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "tv_block_entity"),
                FabricBlockEntityTypeBuilder.create(TVBlockEntity::new,
                        ModBlocks.TV_BLOCK).build(null));

        RADIO_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "radio_block_entity"),
                FabricBlockEntityTypeBuilder.create(TVBlockEntity::new,
                        ModBlocks.RADIO_BLOCK).build(null));
    }
}
