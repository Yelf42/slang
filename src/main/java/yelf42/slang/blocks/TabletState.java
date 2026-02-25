package yelf42.slang.blocks;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum TabletState implements StringRepresentable {
    EMPTY("empty"),
    WRONG("wrong"),
    RIGHT("right"),
    INCOMPLETE("incomplete");

    private final String name;

    private TabletState(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}