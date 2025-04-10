package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.UUID;

public abstract class VideoPlayerBlockEntity extends TileEntity implements ITickableTileEntity {

    // VIDEO / MUSIC  PROPERTIES
    private String url = "";
    private boolean playing = false;
    private boolean stopped = false;

    private int volume = 100;
    private int tick = 0;

    private UUID playerUsing;

    @OnlyIn(Dist.CLIENT)
    public Display display;

    @OnlyIn(Dist.CLIENT)
    public ImageCache imageCache;

    private final Display.DisplayType displayMode;

    public VideoPlayerBlockEntity(TileEntityType<?> tileEntity, Display.DisplayType displayMode) {
        super(tileEntity);
        this.displayMode = displayMode;
    }

    public boolean isURLEmpty() {
        return url.isEmpty();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.tick = 0;
        this.stopped = false;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public boolean isPlaying() {
        return playing && !stopped;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void stop() {
        stopped = true;
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        setChanged();
    }

    public void tryOpen(World level, BlockPos blockPos, PlayerEntity player) {
        // If none is using the block, open the GUI
        if (playerUsing == null) {
            setBeingUsed(player.getUUID());
            onUsedBy(blockPos, player);
            return;
        }

        // If the player that use the block is connected, don't open the GUI
        for (PlayerEntity p : level.players())
            if (p.getUUID() == playerUsing)
                return;

        // Open the GUI
        setBeingUsed(player.getUUID());
        onUsedBy(blockPos, player);
    }

    protected abstract void onUsedBy(BlockPos blockPos, PlayerEntity player);

    public Display requestDisplay() {
        if (isURLEmpty()) return null;

        if (imageCache == null || (!isURLEmpty() && !imageCache.uri.equals(URI.create(url)))) {
            imageCache = ImageAPI.getCache(URI.create(url), Minecraft.getInstance());
            releaseDisplay();
        }

        switch (imageCache.getStatus()) {
            case LOADING:
            case FAILED:
            case READY:
                if (this.display != null) return this.display;
                return this.display = new Display(this, URI.create(url), displayMode);

            case WAITING:
                this.releaseDisplay();
                this.imageCache.load();
                return this.display;

            case FORGOTTEN:
                this.imageCache = null;
                return null;

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getBlockPos(), Registry.BLOCK_ENTITY_TYPE.getId(getType()), getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(getBlockState(), pkt.getTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        loadFromNBTInternal(nbt);
        if (this.level == null) return;
        this.level.blockEntityChanged(this.worldPosition, this);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (isClient()) releaseDisplay();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (isClient()) releaseDisplay();
    }

    public boolean isClient() {
        return this.level != null && this.level.isClientSide;
    }

    @Override
    public CompoundNBT save(CompoundNBT pTag) {
        super.save(pTag);

        pTag.putString("url", url == null ? "" : url);
        pTag.putBoolean("playing", playing || stopped);
        pTag.putInt("tick", tick);
        pTag.putInt("volume", volume);
        pTag.putUUID("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
        return pTag;
    }

    @Override
    public void load(BlockState state, CompoundNBT pTag) {
        super.load(state, pTag);
        loadFromNBTInternal(pTag);
    }

    public void loadFromNBTInternal(CompoundNBT nbt) {
        url = nbt.getString("url");
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        volume = nbt.getInt("volume");
        playerUsing = nbt.getUUID("beingUsed");
    }

    @Override
    public void tick() {
        VideoPlayerBlockEntity be = this;
        if (this.level == null) return;
        if (level.isClientSide) {
            Display display = be.requestDisplay();
            if (display != null) {
                display.tick(be.tick);
            }
        }
        if (be.playing)
            be.tick++;
    }

    public void releaseDisplay() {
        if (display != null) {
            display.release();
            display = null;
        }
    }
}
