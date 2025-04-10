package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.UUID;

public abstract class VideoPlayerBlockEntity extends BlockEntity {

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

    public VideoPlayerBlockEntity(BlockEntityType<?> tileEntity, BlockPos pWorldPosition, BlockState pBlockState, Display.DisplayType displayMode) {
        super(tileEntity, pWorldPosition, pBlockState);
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

    public void tryOpen(Level level, BlockPos blockPos, Player player) {
        // If none is using the block, open the GUI
        if (playerUsing == null) {
            setBeingUsed(player.getUUID());
            onUsedBy(blockPos, player);
            return;
        }

        // If the player that use the block is connected, don't open the GUI
        for (Player p : level.players())
            if (p.getUUID() == playerUsing)
                return;

        // Open the GUI
        setBeingUsed(player.getUUID());
        onUsedBy(blockPos, player);
    }

    protected abstract void onUsedBy(BlockPos blockPos, Player player);

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

    public void tick() {}

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        loadFromNBTInternal(nbt);
        this.level.blockEntityChanged(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
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
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        pTag.putString("url", url == null ? "" : url);
        pTag.putBoolean("playing", playing);
        pTag.putInt("tick", tick);
        pTag.putInt("volume", volume);
        pTag.putUUID("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        loadFromNBTInternal(pTag);
    }

    public void loadFromNBTInternal(CompoundTag nbt) {
        url = nbt.getString("url");
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        volume = nbt.getInt("volume");
        playerUsing = nbt.getUUID("beingUsed");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        VideoPlayerBlockEntity be = (VideoPlayerBlockEntity) level.getBlockEntity(pos);
        if (level.isClientSide && be != null) {
            Display display = be.requestDisplay();
            if (display != null) {
                display.tick(be.tick);
            }
        }
        if (be != null && be.playing)
            be.tick++;

        be.tick();
    }

    public void releaseDisplay() {
        if (display != null) {
            display.release();
            display = null;
        }
    }
}
