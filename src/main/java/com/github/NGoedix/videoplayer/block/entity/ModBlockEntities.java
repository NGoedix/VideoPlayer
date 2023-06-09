package com.github.NGoedix.videoplayer.block.entity;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.block.ModBlocks;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlockEntities {
    public static BlockEntityType<TVBlockEntity> TV_BLOCK_ENTITY;

    public static void registerAllBlockEntities() {
        TV_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Constants.MOD_ID, "mythril_blaster"),
                FabricBlockEntityTypeBuilder.create(TVBlockEntity::new,
                        ModBlocks.TV_BLOCK).build(null));
    }
}
