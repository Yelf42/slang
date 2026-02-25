package yelf42.slang;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import yelf42.slang.registry.ModBlocks;

public class SlangClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.TABLET, ChunkSectionLayer.CUTOUT);
	}
}