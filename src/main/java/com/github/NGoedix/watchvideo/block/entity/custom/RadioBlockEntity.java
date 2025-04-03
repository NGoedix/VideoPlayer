package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenRadioScreenPacket;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;

public class RadioBlockEntity extends VideoPlayerBlockEntity {

    public RadioBlockEntity() {
        super(ModBlockEntities.RADIO_BLOCK_ENTITY.get(), Display.DisplayType.MUSIC);
    }

    @Override
    protected void onUsedBy(BlockPos blockPos, PlayerEntity player) {
        PacketHandler.sendTo(new OpenRadioScreenPacket(blockPos, getUrl(), getVolume(), isPlaying()), player);
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && isPlaying() && getTick() % 10 == 0)
            level.addParticle(ParticleTypes.NOTE, (double)getBlockPos().getX() + 0.5D, (double)getBlockPos().getY() + 0.5D, (double)getBlockPos().getZ() + 0.5D, 1.0f, 0.0D, 0.0D);
    }
}
