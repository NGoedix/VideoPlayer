package com.github.NGoedix.videoplayer.block.entity.custom;

import com.github.NGoedix.videoplayer.block.custom.TVBlock;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.util.cache.TextureCache;
import com.github.NGoedix.videoplayer.util.displayers.IDisplay;
import com.github.NGoedix.videoplayer.util.math.AlignedBox;
import com.github.NGoedix.videoplayer.util.math.Axis;
import com.github.NGoedix.videoplayer.util.math.Facing;
import com.github.NGoedix.videoplayer.util.math.Vec3d;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.fabricmc.api.EnvType.CLIENT;

public class TVBlockEntity extends BlockEntity {

    private String url = "";
    private boolean playing = true;
    private int tick = 0;

    public int volume = 100;

    public float minDistance = 5;
    public float maxDistance = 20;

    public boolean loop = true;

    private UUID playerUsing;

    @Environment(CLIENT)
    public IDisplay display;

    @Environment(CLIENT)
    public TextureCache cache;


    public TVBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.TV_BLOCK_ENTITY, pWorldPosition, pBlockState);
    }

    @Environment(CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }

    @Environment(CLIENT)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
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
        return display = cache.createDisplay(new Vec3d(getPos()), url, volume / 100f, minDistance, maxDistance, loop);
    }

    public void tryOpen(World level, BlockPos blockPos, PlayerEntity player) {
        // If none is using the block, open the GUI
        if (playerUsing == null) {
            setBeingUsed(player.getUuid());
            openVideoManagerGUI(blockPos, player);
            return;
        }

        // If the player that use the block is connected, don't open the GUI
        for (PlayerEntity p : level.getPlayers())
            if (p.getUuid() == playerUsing)
                return;

        // Open the GUI
        openVideoManagerGUI(blockPos, player);
    }

    public void openVideoManagerGUI(BlockPos blockPos, PlayerEntity player) {
        setBeingUsed(player.getUuid());
        PacketHandler.sendMsgOpenVideoManager((ServerPlayerEntity) player, blockPos, url, tick, volume, loop);
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        markDirty();
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }



    public void handleUpdateTag(NbtCompound nbt) {
        loadFromNBT(nbt);
        this.world.markDirty(this.getPos());
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }

    public NbtCompound getUpdateTag() {
        return this.createNbtWithIdentifyingData();
    }

    public static void tick(World level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof TVBlockEntity be) {
            if (level.isClient) {
                IDisplay display = be.requestDisplay();
                if (display != null)
                    display.tick(be.url, be.volume, be.minDistance, be.maxDistance, be.playing, be.loop, be.tick);
            }
            if (be.playing)
                be.tick++;
        }
    }

    @Override
    public void markRemoved() {
        if (isClient() && display != null)
            display.release();
    }

    public boolean isClient() {
        return this.world != null && this.world.isClient;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putString("url", url == null ? "" : url);
        nbt.putUuid("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
        nbt.putBoolean("playing", playing);
        nbt.putInt("tick", tick);
        nbt.putInt("volume", volume);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        loadFromNBT(nbt);
    }

    private void loadFromNBT(NbtCompound nbt) {
        url = nbt.getString("url");
        playerUsing = nbt.getUuid("beingUsed");
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        volume = nbt.getInt("volume");
    }

    public void notifyPlayer() {
        PacketHandler.sendMsgFrameVideo(getWorld().getWorldChunk(getPos()), getPos(), playing, tick);
    }

    public float getSizeX() {
        return 1.4F;
    }

    public float getSizeY() {
        return 0.85F;
    }

    public AlignedBox getBox() {
        Direction direction = getCachedState().get(TVBlock.FACING);
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
