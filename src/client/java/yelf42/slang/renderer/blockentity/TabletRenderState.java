package yelf42.slang.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import yelf42.slang.blocks.TabletText;

public class TabletRenderState extends BlockEntityRenderState {
    public TabletText text;
    public int answerLine;
    public int textLineHeight;
    public int maxTextLineWidth;
    public boolean big;
}
