package yelf42.slang;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import yelf42.slang.blocks.TabletBlockEntity;
import yelf42.slang.registry.ModBlocks;
import yelf42.slang.registry.ModPackets;
import yelf42.slang.renderer.blockentity.TabletRenderer;
import yelf42.slang.screen.TabletAnswerScreen;
import yelf42.slang.screen.TabletEditScreen;

public class SlangClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.TABLET_1_SMALL, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TABLET_2_SMALL, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TABLET_3_SMALL, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TABLET_1_BIG, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TABLET_2_BIG, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TABLET_3_BIG, ChunkSectionLayer.CUTOUT);

        BlockRenderLayerMap.putBlock(ModBlocks.ERASER, ChunkSectionLayer.CUTOUT);

        BlockEntityRenderers.register(ModBlocks.TABLET_ENTITY, TabletRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OpenTabletPayload.ID, (payload, context) -> {
            ClientLevel world = context.client().level;
            if (world == null) return;

            BlockPos pos = payload.pos();
            BlockEntity blockEntity = context.player().level().getBlockEntity(pos);
            if (blockEntity instanceof TabletBlockEntity tabletBlockEntity) {
                boolean editor = payload.editor();
                if (editor) {
                    Minecraft.getInstance().setScreen(new TabletEditScreen(tabletBlockEntity));
                } else {
                    Minecraft.getInstance().setScreen(new TabletAnswerScreen(tabletBlockEntity));
                }
            }
        });
	}
}