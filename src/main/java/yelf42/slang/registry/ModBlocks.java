package yelf42.slang.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import yelf42.slang.Slang;
import yelf42.slang.blocks.TabletBlock;

import java.util.function.Function;

public class ModBlocks {

    public static final Block TABLET = register(
            "tablet",
            TabletBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.9F)
                    .sound(SoundType.STONE),
            true
    );

    public static void initialize() {
        Slang.LOGGER.info("Initializing blocks for " + Slang.MOD_ID);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        // Create a registry key for the block
        ResourceKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        Block block = blockFactory.apply(settings.setId(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            ResourceKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new net.minecraft.world.item.Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

            ItemGroupEvents.modifyEntriesEvent(Slang.SLANG_ITEM_GROUP_KEY).register((itemGroup) -> {
                itemGroup.accept(blockItem);
            });
        }

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, net.minecraft.world.item.Item.Properties itemSettings) {
        // Create a registry key for the block
        ResourceKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        Block block = blockFactory.apply(settings.setId(blockKey));

        ResourceKey<Item> itemKey = keyOfItem(name);

        BlockItem blockItem = new BlockItem(block, itemSettings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        ItemGroupEvents.modifyEntriesEvent(Slang.SLANG_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.accept(blockItem);
        });

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static Block registerPotted(String name, BlockBehaviour.Properties settings, Block flower) {
        // Create a registry key for the block
        ResourceKey<Block> blockKey = keyOfBlock(name);
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, new FlowerPotBlock(flower, settings.setId(blockKey)));
    }

    private static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Slang.MOD_ID, name));
    }

    private static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Slang.MOD_ID, name));
    }

}
