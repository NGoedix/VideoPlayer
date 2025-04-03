package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenRadioScreenPacket;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class HandRadioBlockEntity extends VideoPlayerBlockEntity {

    public HandRadioBlockEntity() {
        super(ModBlockEntities.RADIO_BLOCK_ENTITY.get(), Display.DisplayType.MUSIC);
    }

    @Override
    protected void onUsedBy(BlockPos blockPos, PlayerEntity player) {
        PacketHandler.sendTo(new OpenRadioScreenPacket(blockPos, getUrl(), getVolume(), isPlaying()), player);
    }
}
