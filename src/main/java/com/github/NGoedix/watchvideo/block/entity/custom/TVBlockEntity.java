package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.FrameVideoMessage;
import com.github.NGoedix.watchvideo.network.message.OpenVideoManagerScreen;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.math.AlignedBox;
import com.github.NGoedix.watchvideo.util.math.Axis;
import com.github.NGoedix.watchvideo.util.math.Facing;
import com.github.NGoedix.watchvideo.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.UUID;

public class TVBlockEntity extends TileEntity implements ITickableTileEntity {

    private String url = "";
    private boolean playing = false;
    private int tick = 0;

    private float volume = 1;

    public float minDistance = 5;
    public float maxDistance = 20;

    private boolean loop = true;

    private UUID playerUsing;

    @OnlyIn(Dist.CLIENT)
    public IDisplay display;

    @OnlyIn(Dist.CLIENT)
    public TextureCache cache;


    public TVBlockEntity() {
        super(ModBlockEntities.TV_BLOCK_ENTITY.get());
    }

    public TVBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public void setVolume(int volume) {
        this.volume = volume / 100F;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public float getVolume() {
        return volume;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isLoop() {
        return loop;
    }

    public IDisplay requestDisplay() {
        String url = getUrl();
        if (cache == null || !cache.url.equals(url)) {
            cache = TextureCache.get(url);
            if (display != null)
                display.release();
            display = null;
        }
        if (!cache.isVideo() && (!cache.ready() || cache.getError() != null))
            return null;
        if (display != null)
            return display;
        return display = cache.createDisplay(new Vec3d(worldPosition), url, volume, minDistance, maxDistance, loop, playing);
    }

    public void tryOpen(World level, BlockPos blockPos, PlayerEntity player) {
        // If none is using the block, open the GUI
        if (playerUsing == null) {
            setBeingUsed(player.getUUID());
            openVideoManagerGUI(blockPos, player);
            return;
        }

        // If the player that use the block is connected, don't open the GUI
        for (PlayerEntity p : level.players())
            if (p.getUUID() == playerUsing)
                return;

        // Open the GUI
        openVideoManagerGUI(blockPos, player);
    }

    public void openVideoManagerGUI(BlockPos blockPos, PlayerEntity player) {
        setBeingUsed(player.getUUID());
        PacketHandler.sendTo(new OpenVideoManagerScreen(blockPos, url, tick, (int) (volume * 100), loop), player);
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        setChanged();
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getBlockPos(), 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(getBlockState(), pkt.getTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        loadFromNBT(nbt);
        this.level.blockEntityChanged(this.worldPosition, this);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void setRemoved() {
        if (isClient() && display != null)
            display.release();
    }

    @Override
    public void onChunkUnloaded() {
        if (isClient() && display != null)
            display.release();
    }

    public boolean isClient() {
        return this.level != null && this.level.isClientSide;
    }

    @Override
    public CompoundNBT save(CompoundNBT pTag) {
        super.save(pTag);

        pTag.putString("url", url == null ? "" : url);
        pTag.putUUID("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
        pTag.putBoolean("playing", playing);
        pTag.putInt("tick", tick);
        pTag.putFloat("volume", volume);
        return pTag;
    }

    @Override
    public void load(BlockState state, CompoundNBT pTag) {
        super.load(state, pTag);

        loadFromNBT(pTag);
    }

    private void loadFromNBT(CompoundNBT nbt) {
        url = nbt.getString("url");
        playerUsing = nbt.getUUID("beingUsed");
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        volume = nbt.getFloat("volume");
    }

    public void notifyPlayer() {
        PacketHandler.sendToClient(new FrameVideoMessage(worldPosition, playing, tick), level, worldPosition);
    }

    public float getSizeX() {
        return 1.4F;
    }

    public float getSizeY() {
        return 0.81F;
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

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        getBlockState().setValue(TVBlock.LIT, playing);
        this.playing = playing;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    @Override
    public void tick() {
        TVBlockEntity be = this;
        if (level.isClientSide) {
            IDisplay display = be.requestDisplay();
            if (display != null)
                display.tick(be.url, be.volume, be.minDistance, be.maxDistance, be.playing, be.loop, be.tick);
        }
        if (be.playing)
            be.tick++;
        level.setBlock(getBlockPos(), getBlockState().setValue(TVBlock.LIT, be.playing), 3);
    }
}
