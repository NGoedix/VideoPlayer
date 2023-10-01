package com.github.NGoedix.watchvideo.mixin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.AccessorInfo;

import java.util.Set;

@Mixin(World.class)
public interface WorldMixin {

    @Accessor("blockEntitiesToUnload")
    public Set<TileEntity> getBlockEntitiesToUnload();
}