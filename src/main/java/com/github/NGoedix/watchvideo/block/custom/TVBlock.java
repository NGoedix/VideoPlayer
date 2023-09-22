package com.github.NGoedix.watchvideo.block.custom;

import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.util.math.AlignedBox;
import com.github.NGoedix.watchvideo.util.math.Facing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public class TVBlock extends Block {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    private static final VoxelShape SHAPE_EAST_WEST = Block.box(7, 0, -4, 8, 15, 20);
    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(-4, 0, 7, 20, 15, 8);

    public TVBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        TileEntity blockEntity = world.getBlockEntity(pPos);
        if (blockEntity instanceof TVBlockEntity) {
            blockEntity.setChanged();
            world.setBlockEntity(pPos, blockEntity);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getStateDefinition().any().setValue(FACING, context.getHorizontalDirection() == Direction.WEST ? Direction.EAST : (context.getHorizontalDirection() == Direction.EAST ? Direction.WEST : context.getHorizontalDirection()));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return super.mirror(state, mirrorIn);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
        TileEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (!pLevel.isClientSide) {
            if (blockEntity instanceof TVBlockEntity) {
                TVBlockEntity tvBlockEntity = (TVBlockEntity) blockEntity;
                tvBlockEntity.tryOpen(pLevel, pPos, pPlayer);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModBlockEntities.TV_BLOCK_ENTITY.get().create();
    }


    public static AlignedBox box(Direction direction) {
        Facing facing = Facing.get(direction);
        AlignedBox box = new AlignedBox();
        if (facing.positive)
            box.setMax(facing.axis, 0.031F);
        else
            box.setMin(facing.axis, 1 - 0.031F);
        return box;
    }


}
