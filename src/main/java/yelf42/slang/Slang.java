package yelf42.slang;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yelf42.slang.blocks.TabletBlockEntity;
import yelf42.slang.registry.ModBlocks;
import yelf42.slang.registry.ModItems;
import yelf42.slang.registry.ModPackets;

import java.util.List;
import java.util.stream.Stream;

public class Slang implements ModInitializer {
	public static final String MOD_ID = "slang";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Style STYLE = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(Slang.MOD_ID, "monocraft")));


    public static final ResourceKey<CreativeModeTab> SLANG_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MOD_ID, "item_group"));
    public static final CreativeModeTab SLANG_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.TABLET_1_SMALL.asItem()))
            .title(Component.translatable("itemGroup.slang"))
            .build();

	@Override
	public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SLANG_ITEM_GROUP_KEY, SLANG_ITEM_GROUP);

        ModItems.initialize();
        ModBlocks.initialize();
        ModPackets.initialize();

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.CloseTabletPayload.ID, (payload, context) -> {
            ServerLevel serverLevel = context.player().level();
            BlockPos pos = payload.pos();
            List<String> list = Stream.of(payload.lines()).map(ChatFormatting::stripFormatting).toList();
            if (serverLevel.hasChunkAt(pos)) {
                BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                if (blockEntity instanceof TabletBlockEntity tabletBlockEntity) {
                    tabletBlockEntity.updateTabletText(context.player(), list, payload.editor());
                }
            }


        });
	}
}