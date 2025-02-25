package com.github.NGoedix.videoplayer.block.entity.custom;

import com.github.NGoedix.videoplayer.util.cache.TextureCache;
import com.github.NGoedix.videoplayer.util.config.TVConfig;
import com.github.NGoedix.videoplayer.util.displayers.IDisplay;
import com.github.NGoedix.videoplayer.util.math.geo.Vec3d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class VideoPlayerBlockEntity extends BlockEntity {

    // VIDEO / MUSIC  PROPERTIES
    private String url = "";
    private boolean playing = false;
    private boolean stopped = false;

    private int volume = 100;
    private int tick = 0;

    private final boolean loop = true;

    @Environment(EnvType.CLIENT)
    public IDisplay display;

    @Environment(EnvType.CLIENT)
    public TextureCache cache;

    private final boolean isOnlyMusic;

    public VideoPlayerBlockEntity(BlockEntityType<?> tileEntity, BlockPos pWorldPosition, BlockState pBlockState, boolean isOnlyMusic) {
        super(tileEntity, pWorldPosition, pBlockState);
        this.isOnlyMusic = isOnlyMusic;
    }

    public boolean isURLEmpty() {
        return url.isEmpty();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        if (this.level == null) return;
        this.level.blockEntityChanged(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public void setVolume(int volume) {
        this.volume = volume;
        if (this.level == null) return;
        this.level.blockEntityChanged(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
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
        if (this.level == null) return;
        this.level.blockEntityChanged(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void stop() {
        stopped = true;
    }

    public IDisplay requestDisplay() {
        String url = getUrl();
        if (isURLEmpty()) return null;
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

        return display = cache.createDisplay(new Vec3d(worldPosition), url, volume, TVConfig.MIN_DISTANCE, TVConfig.MAX_DISTANCE, loop, playing, isOnlyMusic);
    }

    public void tick() {}

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithFullMetadata(registries);
    }

    @Override
    public void setRemoved() {
        if (isClient() && display != null)
            display.release();
    }

    public boolean isClient() {
        return this.level != null && this.level.isClientSide;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);

        pTag.putString("url", url == null ? "" : url);
        pTag.putBoolean("playing", playing);
        pTag.putInt("tick", tick);
        pTag.putInt("volume", volume);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);

        loadFromNBTInternal(pTag);
        loadFromNBT(pTag);
    }

    protected abstract void loadFromNBT(CompoundTag nbt);

    public void loadFromNBTInternal(CompoundTag nbt) {
        loadFromNBT(nbt);

        url = nbt.getString("url");
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        volume = nbt.getInt("volume");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        VideoPlayerBlockEntity be = (VideoPlayerBlockEntity) level.getBlockEntity(pos);
        if (level.isClientSide) {
            IDisplay display = be.requestDisplay();
            if (display != null) {
                if (be.stopped)
                    display.stop();
                be.stopped = false;
                display.tick(be.url, be.volume, TVConfig.MIN_DISTANCE, TVConfig.MAX_DISTANCE, be.playing, be.loop, be.isOnlyMusic ? 0 : be.tick);
            }
        }
        if (be == null) return;
        if (be.playing)
            be.tick++;
        be.tick();
    }
}
