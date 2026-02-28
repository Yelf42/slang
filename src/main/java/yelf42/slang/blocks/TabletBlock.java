package yelf42.slang.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import yelf42.slang.registry.ModBlocks;
import yelf42.slang.registry.ModItems;
import yelf42.slang.registry.ModPackets;

import java.util.Map;
import java.util.UUID;

public class TabletBlock extends BaseEntityBlock {
    public static final MapCodec<TabletBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    propertiesCodec(),
                    Codec.INT.fieldOf("answerLine").forGetter(b -> b.answerLine),
                    Codec.BOOL.fieldOf("big").forGetter(b -> b.big)
            ).apply(instance, TabletBlock::new)
    );    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<TabletState> STATE = EnumProperty.create("state", TabletState.class);
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.box(0.0F, 0.0F, 14.0F, 16.0F, 16.0F, 16.0F));
    private static final Map<Direction, VoxelShape> BIG_SHAPES = Shapes.rotateHorizontal(
            Shapes.or(
                    Block.box(-16.0F, 0.0F, 14.0F, 0.0F, 16.0F, 16.0F),
                    Block.box(0.0F, 0.0F, 14.0F, 16.0F, 16.0F, 16.0F)
    ));

    private final int answerLine;
    private final boolean big;

    public TabletBlock(BlockBehaviour.Properties properties, int answerLine, boolean big) {
        super(properties);
        this.answerLine = answerLine;
        this.big = big;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.big ? BIG_SHAPES.get(blockState.getValue(FACING)) : SHAPES.get(blockState.getValue(FACING));
    }

    public boolean isBig() {
        return big;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return useItemOnTablet(level, blockPos, player, interactionHand);
    }

    public InteractionResult useItemOnTablet(Level level, BlockPos blockPos, Player player, InteractionHand interactionHand) {
        if (level.isClientSide() || interactionHand.equals(InteractionHand.OFF_HAND)) return InteractionResult.PASS;
        boolean canEdit = player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.SLANG_TOOL);

        BlockEntity var7 = level.getBlockEntity(blockPos);
        if (var7 instanceof TabletBlockEntity tabletBlockEntity) {
            if (!otherPlayerIsEditingSign(player, tabletBlockEntity) && player instanceof ServerPlayer serverPlayer) {
                if (canEdit) {
                    if (tabletBlockEntity.isOwner(player.getUUID())) {
                        openTextEdit(serverPlayer, tabletBlockEntity, blockPos, true);
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("You are not the owner"));
                        return InteractionResult.PASS;
                    }
                } else {
                    openTextEdit(serverPlayer, tabletBlockEntity, blockPos, false);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TabletBlockEntity(blockPos, blockState, this.answerLine, this.big ? 15 : 7);
    }

    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlocks.TABLET_ENTITY, TabletBlockEntity::tick);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (this.isBig() && blockPos2.equals(getVoidPos(blockPos, blockState))) {
            if (blockState2.getBlock() instanceof TabletVoidBlock) {
                return blockState;
            } else {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return blockState;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (this.isBig()) {
            level.setBlock(getVoidPos(blockPos, blockState), ModBlocks.TABLET_VOID.defaultBlockState().setValue(TabletVoidBlock.FACING, blockState.getValue(FACING)), 3);
        }
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
    }

    private BlockPos getVoidPos(BlockPos pos, BlockState blockState) {
        Direction direction = blockState.getValueOrElse(TabletBlock.FACING, Direction.SOUTH);
        return switch(direction) {
            case SOUTH -> pos.east();
            case NORTH -> pos.west();
            case EAST -> pos.north();
            case WEST -> pos.south();
            default -> pos;
        };
    }

    private BlockPos getVoidPos(BlockPos pos, Direction direction) {
        return switch(direction) {
            case SOUTH -> pos.east();
            case NORTH -> pos.west();
            case EAST -> pos.north();
            case WEST -> pos.south();
            default -> pos;
        };
    }

    public void openTextEdit(ServerPlayer player, TabletBlockEntity tabletBlockEntity, BlockPos blockPos, boolean editor) {
        tabletBlockEntity.setAllowedPlayerEditor(player.getUUID());
        ModPackets.OpenTabletPayload payload = new ModPackets.OpenTabletPayload(blockPos, editor);
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
        player.connection.send(packet);
    }

    private boolean otherPlayerIsEditingSign(Player player, TabletBlockEntity tabletBlockEntity) {
        UUID uUID = tabletBlockEntity.getPlayerWhoMayEdit();
        return uUID != null && !uUID.equals(player.getUUID());
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction facing = blockPlaceContext.getHorizontalDirection().getOpposite();
        if (this.isBig()) {
            if (!blockPlaceContext.getLevel().getBlockState(this.getVoidPos(blockPlaceContext.getClickedPos(), facing)).canBeReplaced()) return null;
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }

    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    public float getYRotationDegrees(BlockState blockState) {
        return blockState.getValue(FACING).toYRot();
    }

    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return level.getBlockState(blockPos).getValueOrElse(TabletBlock.STATE, TabletState.EMPTY) == TabletState.RIGHT ? 15 : 0;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE);
    }
}
