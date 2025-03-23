package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.OpenRadioManagerScreen;
import com.github.NGoedix.watchvideo.network.message.RadioMessage;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class HandRadioBlockEntity extends VideoPlayerBlockEntity {

    private UUID playerUsing;

    public HandRadioBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.HAND_RADIO_BLOCK_ENTITY.get(), pWorldPosition, pBlockState, Display.DisplayType.MUSIC);
    }

    public void tryOpen(Level level, BlockPos blockPos, Player player) {
        // If none is using the block, open the GUI
        if (playerUsing == null) {
            setBeingUsed(player.getUUID());
            openRadioManagerGUI(blockPos, player);
            return;
        }

        // If the player that use the block is connected, don't open the GUI
        for (Player p : level.players())
            if (p.getUUID() == playerUsing)
                return;

        // Open the GUI
        openRadioManagerGUI(blockPos, player);
    }

    public void openRadioManagerGUI(BlockPos blockPos, Player player) {
        setBeingUsed(player.getUUID());
        PacketHandler.sendTo(new OpenRadioManagerScreen(blockPos, getUrl(), getVolume(), isPlaying()), player);
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putUUID("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        loadFromNBT(pTag);
    }

    @Override
    protected void loadFromNBT(CompoundTag nbt) {
        playerUsing = nbt.getUUID("beingUsed");
    }

    public void notifyPlayer() {
        if (this.level == null) return;
        PacketHandler.sendToClient(new RadioMessage(getUrl(), worldPosition, isPlaying()), level, worldPosition);
    }
}
