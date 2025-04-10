package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenRadioScreenPacket;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;


public class HandRadioBlockEntity extends VideoPlayerBlockEntity {

    public HandRadioBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.HAND_RADIO_BLOCK_ENTITY.get(), pWorldPosition, pBlockState, Display.DisplayType.MUSIC);
    }

    @Override
    protected void onUsedBy(BlockPos blockPos, Player player) {
        PacketHandler.sendTo(new OpenRadioScreenPacket(blockPos, getUrl(), getVolume(), isPlaying()), player);
    }
}
