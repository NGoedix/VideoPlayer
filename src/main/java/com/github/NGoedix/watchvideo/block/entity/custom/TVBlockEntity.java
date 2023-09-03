package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.message.OpenVideoManagerScreen;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.FrameVideoMessage;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.math.AlignedBox;
import com.github.NGoedix.watchvideo.util.math.Axis;
import com.github.NGoedix.watchvideo.util.math.Facing;
import com.github.NGoedix.watchvideo.util.math.Vec3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TVBlockEntity extends BlockEntity {

    private String url = "";
    private boolean playing = true;
    private int tick = 0;

    public float volume = 1;

    public float minDistance = 5;
    public float maxDistance = 20;

    public boolean loop = true;

    private UUID playerUsing;

    @OnlyIn(Dist.CLIENT)
    public IDisplay display;

    @OnlyIn(Dist.CLIENT)
    public TextureCache cache;


    public TVBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.TV_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
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

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public IDisplay requestDisplay() {
        String url = getUrl();
        if (cache == null || !cache.url.equals(url)) {
            cache = new TextureCache(url);
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
        PacketHandler.sendTo(new OpenVideoManagerScreen(blockPos, url, tick, (int) (volume * 100), loop), player);
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        setChanged();
    }

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
        loadFromNBT(nbt);
        this.level.blockEntityChanged(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof TVBlockEntity be) {
            if (level.isClientSide) {
                IDisplay display = be.requestDisplay();
                if (display != null)
                    display.tick(be.url, be.volume, be.minDistance, be.maxDistance, be.playing, be.loop, be.tick);
            }
            if (be.playing)
                be.tick++;
        }
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
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        pTag.putString("url", url == null ? "" : url);
        pTag.putUUID("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
        pTag.putBoolean("playing", playing);
        pTag.putInt("tick", tick);
        pTag.putFloat("volume", volume);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        loadFromNBT(pTag);
    }

    private void loadFromNBT(CompoundTag nbt) {
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
        this.playing = playing;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }
}
