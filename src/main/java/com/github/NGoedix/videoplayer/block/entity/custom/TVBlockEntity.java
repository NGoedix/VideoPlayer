package com.github.NGoedix.videoplayer.block.entity.custom;

import com.github.NGoedix.videoplayer.block.custom.TVBlock;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.util.math.geo.AlignedBox;
import com.github.NGoedix.videoplayer.util.math.geo.Axis;
import com.github.NGoedix.videoplayer.util.math.geo.Facing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class TVBlockEntity extends VideoPlayerBlockEntity {

    private UUID playerUsing;

    public TVBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TV_BLOCK_ENTITY, pos, state, false);
    }

    public void tryOpen(Level level, BlockPos blockPos, Player player) {
        // If none is using the block, open the GUI
        if (playerUsing == null) {
            setBeingUsed(player.getUUID());
            openVideoManagerGUI(blockPos, player);
            return;
        }

        // If the player that use the block is connected, don't open the GUI
        for (Player p : level.players())
            if (p.getUUID() == playerUsing)
                return;

        // Open the GUI
        openVideoManagerGUI(blockPos, player);
    }

    public void openVideoManagerGUI(BlockPos blockPos, Player player) {
        setBeingUsed(player.getUUID());
        PacketHandler.sendS2COpenVideoManagerScreen((ServerPlayer) player, blockPos, getUrl(), getVolume(), getTick(), isPlaying());
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        nbt.putUUID("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);

        loadFromNBT(nbt);
    }

    @Override
    protected void loadFromNBT(CompoundTag nbt) {
        playerUsing = nbt.getUUID("beingUsed");
    }

    public void notifyPlayer() {
        PacketHandler.sendS2CFrameVideoMessage(getLevel().getChunkAt(getBlockPos()), getUrl(), getBlockPos(), isPlaying(), getTick());
    }

    public float getSizeX() {
        return 1.4F;
    }

    public float getSizeY() {
        return 0.77F;
    }

    public AlignedBox getBox() {
        Direction direction = getBlockState().getValue(TVBlock.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = TVBlock.box(direction);

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, 0);
        box.setMax(one, getSizeX());

        box.setMin(two, 0);
        box.setMax(two, getSizeY());
        return box;
    }

    @Override
    public void tick() {
        super.tick();
        level.setBlock(getBlockPos(), getBlockState().setValue(TVBlock.LIT, isPlaying()), 3);
    }
}
