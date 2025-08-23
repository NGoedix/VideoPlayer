package com.github.NGoedix.videoplayer.block.entity.custom;

import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class RadioBlockEntity extends VideoPlayerBlockEntity {

    private UUID playerUsing;

    public RadioBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.RADIO_BLOCK_ENTITY, pWorldPosition, pBlockState, true);
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
        PacketHandler.sendS2COpenRadioManagerScreen((ServerPlayer) player, blockPos, getUrl(), getVolume(), isPlaying());
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
        super.saveAdditional(pTag, provider);
        pTag.putUUID("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
        super.loadAdditional(pTag, provider);

        loadFromNBT(pTag);
    }

    @Override
    protected void loadFromNBT(CompoundTag nbt) {
        playerUsing = nbt.getUUID("beingUsed");
    }

    public void notifyPlayer() {
        if (this.level == null) return;
        if (!this.level.isClientSide)
            PacketHandler.sendS2CRadioMessage(getLevel().getChunkAt(getBlockPos()), getUrl(), getBlockPos(), isPlaying());
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && isPlaying() && getTick() % 10 == 0)
            level.addParticle(ParticleTypes.NOTE, (double)getBlockPos().getX() + 0.5D, (double)getBlockPos().getY() + 0.5D, (double)getBlockPos().getZ() + 0.5D, 1.0f, 0.0D, 0.0D);
    }
}
