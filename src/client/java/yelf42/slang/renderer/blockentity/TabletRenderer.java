package yelf42.slang.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import yelf42.slang.Slang;
import yelf42.slang.blocks.TabletBlock;
import yelf42.slang.blocks.TabletBlockEntity;
import yelf42.slang.blocks.TabletText;

import java.util.List;
import java.util.stream.Collectors;

public class TabletRenderer implements BlockEntityRenderer<TabletBlockEntity, TabletRenderState> {
    private final Font font;

    public TabletRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public TabletRenderState createRenderState() {
        return new TabletRenderState();
    }

    @Override
    public void extractRenderState(TabletBlockEntity blockEntity, TabletRenderState tabletRenderState, float f, Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, tabletRenderState, f, vec3, crumblingOverlay);
        tabletRenderState.maxTextLineWidth = blockEntity.getMaxTextLineWidth();
        tabletRenderState.textLineHeight = blockEntity.getTextLineHeight();
        tabletRenderState.text = blockEntity.getText();
        tabletRenderState.answerLine = blockEntity.getAnswerLine();
        tabletRenderState.big = blockEntity.isBig();
    }

    @Override
    public void submit(TabletRenderState blockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        BlockState blockState = blockEntityRenderState.blockState;
        TabletBlock tablet = (TabletBlock)blockState.getBlock();

        poseStack.pushPose();
        this.translateSign(poseStack, -tablet.getYRotationDegrees(blockState));
        this.submitTabletText(blockEntityRenderState, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    private void submitTabletText(TabletRenderState signRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        TabletText signText = signRenderState.text;
        if (signText != null) {
            poseStack.pushPose();
            this.translateSignText(poseStack, new Vec3((signRenderState.big ? 0.5F : 0.0F), 0.15F, 0.08F));
            int i = -988212;
            int j = 4 * signRenderState.textLineHeight / 2;
            FormattedCharSequence[] formattedCharSequences = signText.getRenderMessages(false, this::getLine);

            String original = signText.getMessage(signRenderState.answerLine, false).getString();
            String underscored = original.chars()
                    .mapToObj(c -> c == ' ' ? " " : "_")
                    .collect(Collectors.joining());
            FormattedCharSequence answerLine = this.getLine(Component.literal(underscored).withStyle(Slang.STYLE));

            int l = signRenderState.lightCoords;

            for(int m = 0; m < 3; ++m) {
                if (m == signRenderState.answerLine) {
                    FormattedCharSequence formattedCharSequence = formattedCharSequences[3];
                    float f = (float)(-this.font.width(answerLine) / 2);
                    submitNodeCollector.submitText(poseStack, f, (float)(m * signRenderState.textLineHeight - j) + 1.F, answerLine, false, Font.DisplayMode.POLYGON_OFFSET, l, i, 0, 0);
                    submitNodeCollector.submitText(poseStack, f, (float)(m * signRenderState.textLineHeight - j) - 1.F, formattedCharSequence, false, Font.DisplayMode.POLYGON_OFFSET, l, i, 0, 0);
                } else {
                    FormattedCharSequence formattedCharSequence = formattedCharSequences[m];
                    float f = (float)(-this.font.width(formattedCharSequence) / 2);
                    submitNodeCollector.submitText(poseStack, f, (float)(m * signRenderState.textLineHeight - j), formattedCharSequence, false, Font.DisplayMode.POLYGON_OFFSET, l, i, 0, 0);
                }
            }

            poseStack.popPose();
        }
    }

    private FormattedCharSequence getLine(Component component) {
        List<FormattedCharSequence> list = this.font.split(component, 120);
        return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
    }

    private void translateSignText(PoseStack poseStack, Vec3 vec3) {
        float f = 0.015625F;
        poseStack.translate(vec3);
        poseStack.scale(f, -f, f);
    }

    protected void translateSign(PoseStack poseStack, float f) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(f));
        poseStack.translate(0.0F, -0.3125F, -0.4375F);
    }
}
