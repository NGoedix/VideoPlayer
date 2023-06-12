package com.github.NGoedix.videoplayer.block.entity;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.block.ModBlocks;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<TVBlockEntity> TV_BLOCK_ENTITY;

    public static void registerAllBlockEntities() {
        TV_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Constants.MOD_ID, "tv_block_entity"),
                FabricBlockEntityTypeBuilder.create(TVBlockEntity::new,
                        ModBlocks.TV_BLOCK).build(null));
    }
}
