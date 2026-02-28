package yelf42.slang.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import org.jspecify.annotations.Nullable;
import yelf42.slang.Slang;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TabletText {
    private static final Codec<Component[]> LINES_CODEC;
    public static final Codec<TabletText> DIRECT_CODEC;
    public static final int LINES = 4;
    private final Component[] messages;
    private final Component[] filteredMessages;
    private final DyeColor color;
    private FormattedCharSequence @Nullable [] renderMessages;
    private boolean renderMessagedFiltered;

    public TabletText() {
        this(emptyMessages(), emptyMessages(), DyeColor.WHITE);
    }

    public TabletText(Component[] components, Component[] components2, DyeColor dyeColor) {
        this.messages = components;
        this.filteredMessages = components2;
        this.color = dyeColor;
    }

    private static Component[] emptyMessages() {
        Component empty = Component.empty().withStyle(Slang.STYLE);
        return new Component[]{empty.copy(), empty.copy(), empty.copy(), empty.copy()};
    }

    private static TabletText load(Component[] components, Optional<Component[]> optional, DyeColor dyeColor) {
        return new TabletText(components, (Component[])optional.orElse((Component[]) Arrays.copyOf(components, components.length)), dyeColor);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public TabletText setColor(DyeColor dyeColor) {
        return dyeColor == this.getColor() ? this : new TabletText(this.messages, this.filteredMessages, dyeColor);
    }

    public Component getMessage(int i, boolean bl) {
        return this.getMessages(bl)[i];
    }

    public TabletText setMessage(int i, Component component) {
        return this.setMessage(i, component, component);
    }

    public TabletText setMessage(int i, Component component, Component component2) {
        Component[] components = (Component[])Arrays.copyOf(this.messages, this.messages.length);
        Component[] components2 = (Component[])Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);
        components[i] = component;
        components2[i] = component2;
        return new TabletText(components, components2, this.color);
    }

    public boolean hasMessage(Player player) {
        return Arrays.stream(this.getMessages(player.isTextFilteringEnabled())).anyMatch((component) -> !component.getString().isEmpty());
    }

    public Component[] getMessages(boolean bl) {
        return bl ? this.filteredMessages : this.messages;
    }

    public FormattedCharSequence[] getRenderMessages(boolean bl, Function<Component, FormattedCharSequence> function) {
        if (this.renderMessages == null || this.renderMessagedFiltered != bl) {
            this.renderMessagedFiltered = bl;
            this.renderMessages = new FormattedCharSequence[4];

            for(int i = 0; i < 4; ++i) {
                this.renderMessages[i] = (FormattedCharSequence)function.apply(this.getMessage(i, bl));
            }
        }

        return this.renderMessages;
    }

    private Optional<Component[]> filteredMessages() {
        for(int i = 0; i < 4; ++i) {
            if (!this.filteredMessages[i].equals(this.messages[i])) {
                return Optional.of(this.filteredMessages);
            }
        }

        return Optional.empty();
    }

    public boolean hasAnyClickCommands(Player player) {
        for(Component component : this.getMessages(player.isTextFilteringEnabled())) {
            Style style = component.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null && clickEvent.action() == ClickEvent.Action.RUN_COMMAND) {
                return true;
            }
        }

        return false;
    }

    static {
        LINES_CODEC = ComponentSerialization.CODEC.listOf().comapFlatMap((list) -> Util.fixedSize(list, 4).map((listx) -> new Component[]{(Component)listx.get(0), (Component)listx.get(1), (Component)listx.get(2), (Component)listx.get(3)}), (components) -> List.of(components[0], components[1], components[2], components[3]));
        DIRECT_CODEC = RecordCodecBuilder.create((instance) -> instance.group(LINES_CODEC.fieldOf("messages").forGetter((signText) -> signText.messages), LINES_CODEC.lenientOptionalFieldOf("filtered_messages").forGetter(TabletText::filteredMessages), DyeColor.CODEC.fieldOf("color").orElse(DyeColor.BLACK).forGetter((signText) -> signText.color)).apply(instance, TabletText::load));
    }
}
