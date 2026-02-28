package yelf42.slang.registry;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import yelf42.slang.Slang;

import java.util.ArrayList;
import java.util.Arrays;

public class ModPackets {
    
    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(OpenTabletPayload.ID, OpenTabletPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CloseTabletPayload.ID, CloseTabletPayload.CODEC);

    }

    public record OpenTabletPayload(BlockPos pos, boolean editor) implements CustomPacketPayload {
        public static final Identifier OPEN_TABLET_PAYLOAD_ID = Identifier.fromNamespaceAndPath(Slang.MOD_ID, "open_tablet_payload");
        public static final CustomPacketPayload.Type<OpenTabletPayload> ID = new CustomPacketPayload.Type<>(OPEN_TABLET_PAYLOAD_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenTabletPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, OpenTabletPayload::pos,
                ByteBufCodecs.BOOL, OpenTabletPayload::editor,
                OpenTabletPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record CloseTabletPayload(BlockPos pos, boolean editor, String[] lines) implements CustomPacketPayload {
        public static final Identifier CLOSE_TABLET_PAYLOAD_ID = Identifier.fromNamespaceAndPath(Slang.MOD_ID, "close_tablet_payload");
        public static final CustomPacketPayload.Type<CloseTabletPayload> ID = new CustomPacketPayload.Type<>(CLOSE_TABLET_PAYLOAD_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, CloseTabletPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, CloseTabletPayload::pos,
                ByteBufCodecs.BOOL, CloseTabletPayload::editor,
                ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8).map(list -> list.toArray(String[]::new), array -> new ArrayList<>(Arrays.asList(array))), CloseTabletPayload::lines,
                CloseTabletPayload::new
        );

        public CloseTabletPayload(BlockPos pos, boolean editor, String one, String two, String three, String four) {
            this(pos, editor, new String[]{one, two, three, four});
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
