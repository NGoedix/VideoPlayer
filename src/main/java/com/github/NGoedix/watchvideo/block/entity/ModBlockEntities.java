package com.github.NGoedix.watchvideo.block.entity;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.ModBlocks;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlockEntities {

    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Reference.MOD_ID);

    public static final RegistryObject<TileEntityType<TVBlockEntity>> TV_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("tv_block_entity", () ->
                    TileEntityType.Builder.of(TVBlockEntity::new, ModBlocks.TV_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
