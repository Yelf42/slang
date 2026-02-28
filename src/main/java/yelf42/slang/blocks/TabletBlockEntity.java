package yelf42.slang.blocks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import yelf42.slang.Slang;
import yelf42.slang.registry.ModBlocks;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class TabletBlockEntity extends BlockEntity {

    private @Nullable UUID playerWhoMayEdit;
    private @Nullable UUID owner = null;

    // Lines 0-2 are set by puzzle master
    // Line 3 stores player attempt
    // answerLine stores which line should be blanked
    private TabletText text;
    private int answerLine;
    private int maxCharacters;

    public TabletBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlocks.TABLET_ENTITY, blockPos, blockState);
        this.text = new TabletText();
    }

    public TabletBlockEntity(BlockPos blockPos, BlockState blockState, int answer, int maxChars) {
        this(blockPos, blockState);
        this.answerLine = answer;
        this.maxCharacters = maxChars;
    }

    public boolean isOwner(UUID player) {
        if (this.owner == null) setOwner(player);
        return this.owner.equals(player);
    }

    public void setOwner(UUID player) {
        this.owner = player;
        this.markUpdated();
    }

    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.store("text", TabletText.DIRECT_CODEC, this.text);
        if (this.owner != null) {
            valueOutput.store("owner", UUIDUtil.CODEC, this.owner);
        }
    }

    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.text = valueInput.read("text", TabletText.DIRECT_CODEC).map(this::loadLines).orElseGet(TabletText::new);
        this.owner = valueInput.read("owner", UUIDUtil.CODEC).orElse(null);
    }

    private TabletText loadLines(TabletText signText) {
        for(int i = 0; i < 4; ++i) {
            Component component = this.loadLine(signText.getMessage(i, false));
            Component component2 = this.loadLine(signText.getMessage(i, true));
            signText = signText.setMessage(i, component, component2);
        }

        return signText;
    }

    private Component loadLine(Component component) {
        Level var3 = this.level;
        if (var3 instanceof ServerLevel serverLevel) {
            try {
                return ComponentUtils.updateForEntity(createCommandSourceStack(null, serverLevel, this.worldPosition), component, null, 0);
            } catch (CommandSyntaxException var4) {
            }
        }
        return component;
    }

    private static CommandSourceStack createCommandSourceStack(@Nullable Player player, ServerLevel serverLevel, BlockPos blockPos) {
        String string = player == null ? "Tablet" : player.getPlainTextName();
        Component component = player == null ? Component.literal("Tablet") : player.getDisplayName();
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(blockPos), Vec2.ZERO, serverLevel, LevelBasedPermissionSet.GAMEMASTER, string, component, serverLevel.getServer(), player);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public void updateTabletText(Player player, List<String> list, boolean editor) {
        if (player.getUUID().equals(this.getPlayerWhoMayEdit()) && this.level != null) {
            this.updateText((signText) -> this.setMessages(list, signText, editor), editor);
            this.setAllowedPlayerEditor(null);
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            this.updateState();
        } else {
            Slang.LOGGER.warn("Player {} just tried to change non-editable sign", player.getPlainTextName());
        }
    }

    public boolean updateText(UnaryOperator<yelf42.slang.blocks.TabletText> unaryOperator, boolean editor) {
        TabletText signText = this.getText();
        return this.setText(unaryOperator.apply(signText), editor);
    }

    private TabletText setMessages(List<String> list, TabletText signText, boolean editor) {
        if (editor) {
            for(int i = 0; i < 3; i++) {
                String text = list.get(i);
                signText = signText.setMessage(i, Component.literal(text).withStyle(Slang.STYLE), Component.literal(text).withStyle(Slang.STYLE));

            }
        } else {
            String text = list.get(3);
            Style style = signText.getMessage(3, false).getStyle();
            signText = signText.setMessage(3, Component.literal(text).withStyle(Slang.STYLE), Component.literal(text).withStyle(Slang.STYLE));
        }

        return signText;
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean setText(TabletText signText, boolean editor) {
        if (editor) {
            if (signText != this.text) {
                this.text = signText;
                this.markUpdated();
                return true;
            }
            return false;
        } else {
            if (!signText.getMessage(3, false).equals(this.text.getMessage(3, false))) {
                this.text = this.text.setMessage(3, signText.getMessage(3, false));
                this.updateState();
                this.markUpdated();
                return true;
            }
            return false;
        }
    }

    public void clearAnswer() {
        this.text = this.text.setMessage(3, Component.empty().withStyle(Slang.STYLE));
        this.updateState();
        this.markUpdated();
    }

    private void updateState() {
        if (this.level == null) return;

        String answer = this.text.getMessage(3, false).getString().stripTrailing();
        String question = this.text.getMessage(this.answerLine, false).getString().stripTrailing();

        if (answer.isBlank()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(TabletBlock.STATE, TabletState.EMPTY), 3);

        } else if (answer.length() < question.length()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(TabletBlock.STATE, TabletState.INCOMPLETE), 3);

        } else if (answer.equalsIgnoreCase(question)) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(TabletBlock.STATE, TabletState.RIGHT), 3);

        } else {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(TabletBlock.STATE, TabletState.WRONG), 3);
        }
    }

    public TabletText getText() {
        return this.text;
    }

    public int getAnswerLine() {
        return this.answerLine;
    }

    public int getMaxCharacters() {
        return this.maxCharacters;
    }

    public int getTextLineHeight() {
        return 15;
    }

    public int getMaxTextLineWidth() {
        return 90;
    }

    public void setAllowedPlayerEditor(@Nullable UUID uUID) {
        this.playerWhoMayEdit = uUID;
    }

    public @Nullable UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, TabletBlockEntity tabletBlockEntity) {
        UUID uUID = tabletBlockEntity.getPlayerWhoMayEdit();
        if (uUID != null) {
            tabletBlockEntity.clearInvalidPlayerWhoMayEdit(tabletBlockEntity, level, uUID);
        }
    }

    private void clearInvalidPlayerWhoMayEdit(TabletBlockEntity tabletBlockEntity, Level level, UUID uUID) {
        if (tabletBlockEntity.playerIsTooFarAwayToEdit(uUID)) {
            tabletBlockEntity.setAllowedPlayerEditor(null);
        }
    }
    
    public boolean playerIsTooFarAwayToEdit(UUID uUID) {
        Player player = this.level.getPlayerByUUID(uUID);
        return player == null || !player.isWithinBlockInteractionRange(this.getBlockPos(), (double)4.0F);
    }

    public boolean isBig() {
        return getMaxCharacters() > 7;
    }
}
