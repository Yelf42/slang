package yelf42.slang.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import yelf42.slang.Slang;

import java.util.Map;

public class TabletVoidBlock extends Block {
    public static final MapCodec<TabletVoidBlock> CODEC = simpleCodec(TabletVoidBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(
            Shapes.or(
                    Block.box(0.0F, 0.0F, 14.0F, 16.0F, 16.0F, 16.0F),
                    Block.box(16.0F, 0.0F, 14.0F, 32.0F, 16.0F, 16.0F)
            )
    );

    public TabletVoidBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<TabletVoidBlock> codec() {
        return CODEC;
    }

    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.get(blockState.getValue(FACING));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockPos tabletPos = this.getOwnerPos(blockPos, blockState);
        BlockState tabletState = level.getBlockState(tabletPos);
        if (tabletState.getBlock() instanceof TabletBlock tabletBlock) {
            return tabletBlock.useItemOnTablet(level, tabletPos, player, interactionHand);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockPos2.equals(getOwnerPos(blockPos, blockState))) {
            if (blockState2.getBlock() instanceof TabletBlock tabletBlock && tabletBlock.isBig()) {
                return blockState;
            } else {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return blockState;
    }

    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    private BlockPos getOwnerPos(BlockPos pos, BlockState blockState) {
        Direction direction = blockState.getValueOrElse(TabletVoidBlock.FACING, Direction.SOUTH);
        return switch(direction) {
            case SOUTH -> pos.west();
            case NORTH -> pos.east();
            case EAST -> pos.south();
            case WEST -> pos.north();
            default -> pos;
        };
    }

    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return level.getBlockState(getOwnerPos(blockPos, blockState)).getValueOrElse(TabletBlock.STATE, TabletState.EMPTY) == TabletState.RIGHT ? 15 : 0;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
