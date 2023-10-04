package com.github.NGoedix.watchvideo.block.entity;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.messages.FrameVideoMessage;
import com.github.NGoedix.watchvideo.network.messages.OpenVideoManagerScreen;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.math.AlignedBox;
import com.github.NGoedix.watchvideo.util.math.Axis;
import com.github.NGoedix.watchvideo.util.math.Facing;
import com.github.NGoedix.watchvideo.util.math.Vec3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class TVBlockEntity extends TileEntity implements ITickable {

    private String url = "";
    private boolean playing = false;
    private int tick = 0;

    private float volume = 1;

    public float minDistance = 5;
    public float maxDistance = 20;

    private boolean loop = true;


    private UUID playerUsing;

    @SideOnly(Side.CLIENT)
    public IDisplay display;

    @SideOnly(Side.CLIENT)
    public TextureCache cache;

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

        return display = cache.createDisplay(new Vec3d(getPos()), url, volume, minDistance, maxDistance, loop, playing);
    }

    public void tryOpen(World level, BlockPos blockPos, EntityPlayerMP player) {
        // If none is using the block, open the GUI
        if (playerUsing == null) {
            setBeingUsed(player.getUniqueID());
            openVideoManagerGUI(blockPos, player);
            return;
        }

        // If the player that use the block is connected, don't open the GUI
        for (EntityPlayerMP p : level.getPlayers(EntityPlayerMP.class, input -> true))
            if (p.getUniqueID() == playerUsing)
                return;

        // Open the GUI
        openVideoManagerGUI(blockPos, player);
    }

    public void openVideoManagerGUI(BlockPos blockPos, EntityPlayerMP player) {
        setBeingUsed(player.getUniqueID());
        PacketHandler.INSTANCE.sendTo(new OpenVideoManagerScreen(blockPos, url, tick, (int) (volume * 100), loop), player);
    }

    public void setBeingUsed(UUID player) {
        this.playerUsing = player;
        updateBlock();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdate(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        handleUpdate(tag);
    }

    public void updateBlock() {
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            world.markChunkDirty(getPos(), this);
        }
    }

    public void handleUpdate(NBTTagCompound nbtTagCompound) {
        loadFromNBT(nbtTagCompound);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    private void loadFromNBT(NBTTagCompound compound) {
        url = compound.getString("url");
        playerUsing = compound.getUniqueId("beingUsed");
        playing = compound.getBoolean("playing");
        tick = compound.getInteger("tick");
        volume = compound.getFloat("volume");
    }

    @SideOnly(Side.CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }

    @SideOnly(Side.CLIENT)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        updateBlock();
    }

    public void setVolume(int volume) {
        this.volume = volume / 100F;
        updateBlock();
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public float getVolume() {
        return volume;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isLoop() {
        return loop;
    }

    public void notifyPlayer() {
        PacketHandler.INSTANCE.sendToAllAround(new FrameVideoMessage(getPos(), playing, tick), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 50));
    }

    @Override
    public void update() {
        TVBlockEntity be = this;
        if (world.isRemote) {
            IDisplay display = be.requestDisplay();
            if (display != null)
                display.tick(be.url, be.volume, be.minDistance, be.maxDistance, be.playing, be.loop, be.tick);
        }
        if (be.playing)
            be.tick++;
        world.getBlockState(getPos()).getBlock().setLightLevel(playing ? 12 : 0);
        updateBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString("url", url == null ? "" : url);
        compound.setUniqueId("beingUsed", playerUsing == null ? new UUID(0, 0) : playerUsing);
        compound.setBoolean("playing", playing);
        compound.setInteger("tick", tick);
        compound.setFloat("volume", volume);

        return compound;
    }

    public IBlockState getBlockState() {
        return world.getBlockState(pos);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        loadFromNBT(compound);
    }

    public float getSizeX() {
        return 1.4F;
    }

    public float getSizeY() {
        return 0.81F;
    }

    public AlignedBox getBox() {
        EnumFacing direction = getBlockState().getValue(TVBlock.FACING);
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
}
