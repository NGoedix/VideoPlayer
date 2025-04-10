package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenRadioScreenPacket;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;


public class RadioBlockEntity extends VideoPlayerBlockEntity {


    public RadioBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.RADIO_BLOCK_ENTITY.get(), pWorldPosition, pBlockState, Display.DisplayType.MUSIC);
    }

    @Override
    protected void onUsedBy(BlockPos blockPos, Player player) {
        PacketHandler.sendTo(new OpenRadioScreenPacket(blockPos, getUrl(), getVolume(), isPlaying()), player);
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && isPlaying() && getTick() % 10 == 0)
            level.addParticle(ParticleTypes.NOTE, (double)getBlockPos().getX() + 0.5D, (double)getBlockPos().getY() + 0.5D, (double)getBlockPos().getZ() + 0.5D, 1.0f, 0.0D, 0.0D);
    }
}
