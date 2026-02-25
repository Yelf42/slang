package yelf42.slang;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yelf42.slang.registry.ModBlocks;

public class Slang implements ModInitializer {
	public static final String MOD_ID = "slang";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<CreativeModeTab> SLANG_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MOD_ID, "item_group"));
    public static final CreativeModeTab SLANG_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.TABLET.asItem()))
            .title(Component.translatable("itemGroup.slang"))
            .build();

	@Override
	public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SLANG_ITEM_GROUP_KEY, SLANG_ITEM_GROUP);

        ModBlocks.initialize();
	}
}