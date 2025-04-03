package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenTVScreenPacket;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import com.github.NGoedix.watchvideo.util.math.geo.AlignedBox;
import com.github.NGoedix.watchvideo.util.math.geo.Axis;
import com.github.NGoedix.watchvideo.util.math.geo.Facing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class TVBlockEntity extends VideoPlayerBlockEntity {

    public TVBlockEntity() {
        super(ModBlockEntities.TV_BLOCK_ENTITY.get(), Display.DisplayType.VIDEO);
    }

    @Override
    protected void onUsedBy(BlockPos blockPos, PlayerEntity player) {
        PacketHandler.sendTo(new OpenTVScreenPacket(blockPos, getUrl(), getVolume(), getTick(), isPlaying()), player);
    }

    @Override
    public void tick() {
        super.tick();
        level.setBlock(getBlockPos(), getBlockState().setValue(TVBlock.LIT, isPlaying()), 3);
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
}
