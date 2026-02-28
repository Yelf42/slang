package yelf42.slang.blocks;


import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class EraserBlock extends Block {
    public static final MapCodec<EraserBlock> CODEC = simpleCodec(EraserBlock::new);
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0F, 0.0F, 0.0F, 16.0F, 8.0F, 16.0F),
            Block.box(3.0F, 8.0F, 5.0F, 13.0F, 16.0F, 11.0F)
    );

    public EraserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    public MapCodec<EraserBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        boolean shouldLight = blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos());
        if (shouldLight) {
            blockPlaceContext.getLevel().getChunkAt(blockPlaceContext.getClickedPos()).getBlockEntities().forEach((pos, blockEntity) -> {
                if (blockEntity instanceof TabletBlockEntity tabletBlockEntity) {
                    tabletBlockEntity.clearAnswer();
                }
            });
        }
        return this.defaultBlockState().setValue(LIT, shouldLight);
    }

    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (!level.isClientSide()) {
            boolean lit = blockState.getValue(LIT);
            boolean shouldLight = level.hasNeighborSignal(blockPos);
            if (!lit && shouldLight) {
                level.setBlock(blockPos, blockState.setValue(LIT, true), 2);
                level.getChunkAt(blockPos).getBlockEntities().forEach((pos, blockEntity) -> {
                    if (blockEntity instanceof TabletBlockEntity tabletBlockEntity) {
                        tabletBlockEntity.clearAnswer();
                    }
                });
            } else if (lit && !shouldLight) {
                level.setBlock(blockPos, blockState.setValue(LIT, false), 2);
            }
        }
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

}
