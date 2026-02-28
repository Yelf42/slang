package yelf42.slang.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import yelf42.slang.Slang;
import yelf42.slang.blocks.TabletBlockEntity;
import yelf42.slang.blocks.TabletText;
import yelf42.slang.registry.ModPackets;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class TabletEditScreen extends Screen {
    protected final TabletBlockEntity tablet;
    private TabletText text;

    private final String[] messages;
    private int frame;
    private int line;
    private @Nullable TextFieldHelper signField;

    private static final Identifier FRAME_TEXTURE = Identifier.fromNamespaceAndPath(Slang.MOD_ID, "textures/gui/tablet_edit_bg.png");

    public TabletEditScreen(TabletBlockEntity tabletBlockEntity) {
        this(tabletBlockEntity, Component.translatable("slang.tablet.edit").withStyle(Slang.STYLE));
    }

    protected TabletEditScreen(TabletBlockEntity tabletBlockEntity, Component component) {
        super(component);
        this.tablet = tabletBlockEntity;
        this.text = tabletBlockEntity.getText();
        this.messages = IntStream.range(0, 3).mapToObj((i) -> this.text.getMessage(i, false)).map(Component::getString).toArray((i) -> new String[i]);
    }

    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
        this.signField = new TextFieldHelper(() -> this.messages[this.line], this::setMessage, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (string) -> string.length() <= this.tablet.getMaxCharacters());
    }

    public void tick() {
        ++this.frame;
        if (!this.isValid()) {
            this.onDone();
        }

    }

    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBg(guiGraphics);
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, -1);
        this.renderSignText(guiGraphics);
    }

    private void renderBg(GuiGraphics guiGraphics) {
        int texWidth = 48;
        int texHeight = 32;
        float scale = (float) this.height / texHeight * 0.8f;
        int scaledW = (int)(texWidth * scale);
        int scaledH = (int)(texHeight * scale);
        int x = (this.width - scaledW) / 2;
        int y = (this.height - scaledH) / 2;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FRAME_TEXTURE, 0, 0, 0.0F, 0.0F, texWidth, texHeight, texWidth, texHeight);
        guiGraphics.pose().popMatrix();
    }

    private void renderSignText(GuiGraphics guiGraphics) {
        guiGraphics.pose().translate((float)this.width / 2.0F, 100.F);
        Vector3f vector3f = new Vector3f(2F, 2F, 2F);
        guiGraphics.pose().scale(vector3f.x(), vector3f.y());
        int i = ARGB.opaque(16777215);
        boolean flash = this.frame / 6 % 2 == 0;
        int j = this.signField.getCursorPos();
        int l = this.tablet.getTextLineHeight();
        int spacing = 20;
        int m = this.line * spacing - l;

        for(int n = 0; n < this.messages.length; ++n) {
            String string = this.messages[n];
            if (string != null) {
                FormattedCharSequence text = this.getLine(Component.literal(string).withStyle(Slang.STYLE));
                if (this.font.isBidirectional()) {
                    string = this.font.bidirectionalShaping(string);
                }

                int o = -this.font.width(text) / 2;

                guiGraphics.drawString(this.font, text, o, n * spacing - l, i, false);

                if (n == this.tablet.getAnswerLine()) {
                    String original = this.text.getMessage(n, false).getString();
                    String underscored = original.chars()
                            .mapToObj(c -> c == ' ' ? " " : "_")
                            .collect(Collectors.joining());
                    FormattedCharSequence answerLine = this.getLine(Component.literal(underscored).withStyle(Slang.STYLE));
                    guiGraphics.drawString(this.font, answerLine, o, n * spacing - l + 2, i, false);
                }

                if (n == this.line && j >= 0 && flash) {
                    int p = this.font.width(this.getLine(Component.literal(string.substring(0, Math.max(Math.min(j, string.length()), 0))).withStyle(Slang.STYLE)));
                    int q = p - this.font.width(text) / 2;
                    if (j >= string.length()) {
                        guiGraphics.drawString(this.font, this.getLine(Component.literal("_").withStyle(Slang.STYLE)), q, m, i, false);
                    }
                }
            }
        }

        for(int n = 0; n < this.messages.length; ++n) {
            String string = this.messages[n];

            if (string != null && n == this.line && j >= 0) {
                FormattedCharSequence text = this.getLine(Component.literal(string).withStyle(Slang.STYLE));

                int o = this.font.width(this.getLine(Component.literal(string.substring(0, Math.max(Math.min(j, string.length()), 0))).withStyle(Slang.STYLE)));
                int p = o - this.font.width(text) / 2;
                if (flash && j < string.length()) {
                    guiGraphics.fill(p - 1, m - 4, p,  m + spacing / 2, ARGB.opaque(i));
                }
            }
        }
    }

    private FormattedCharSequence getLine(Component component) {
        List<FormattedCharSequence> list = this.font.split(component, 120);
        return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.isUp()) {
            this.line = Math.floorMod(this.line - 1, 3);
            this.signField.setCursorToEnd();
            return true;
        } else if (!keyEvent.isDown() && !keyEvent.isConfirmation()) {
            return this.signField.keyPressed(keyEvent) ? true : super.keyPressed(keyEvent);
        } else {
            this.line = Math.floorMod(this.line + 1, 3);
            this.signField.setCursorToEnd();
            return true;
        }
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        this.signField.charTyped(characterEvent);
        return true;
    }

    private boolean isValid() {
        return this.minecraft.player != null && !this.tablet.isRemoved() && !this.tablet.playerIsTooFarAwayToEdit(this.minecraft.player.getUUID());
    }

    public void removed() {
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null) {
            ModPackets.CloseTabletPayload payload = new ModPackets.CloseTabletPayload(this.tablet.getBlockPos(), true, this.messages[0], this.messages[1], this.messages[2], "");
            ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket(payload);
            clientPacketListener.send(packet);
        }
    }

    private void setMessage(String string) {
        this.messages[this.line] = string;
        this.text = this.text.setMessage(this.line, Component.literal(string).withStyle(Slang.STYLE));
        this.tablet.setText(this.text, true);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean isInGameUi() {
        return true;
    }

    public void onClose() {
        this.onDone();
    }

    private void onDone() {
        this.minecraft.setScreen((Screen)null);
    }


}
