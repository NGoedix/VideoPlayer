package com.github.NGoedix.watchvideo.block.entity.custom;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenTVScreenPacket;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import com.github.NGoedix.watchvideo.util.math.geo.AlignedBox;
import com.github.NGoedix.watchvideo.util.math.geo.Axis;
import com.github.NGoedix.watchvideo.util.math.geo.Facing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;


public class TVBlockEntity extends VideoPlayerBlockEntity {

    public TVBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.TV_BLOCK_ENTITY.get(), pWorldPosition, pBlockState, Display.DisplayType.VIDEO);
    }

    @Override
    protected void onUsedBy(BlockPos blockPos, Player player) {
        PacketHandler.sendTo(new OpenTVScreenPacket(blockPos, getUrl(), getVolume(), getTick(), isPlaying()), player);
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

    @Override
    public void tick() {
        super.tick();
        level.setBlock(getBlockPos(), getBlockState().setValue(TVBlock.LIT, isPlaying()), 3);
    }
}
