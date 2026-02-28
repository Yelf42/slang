package yelf42.slang.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import yelf42.slang.Slang;

import java.util.function.Function;

public class ModItems {

    public static void initialize() {
        Slang.LOGGER.info("Initializing items for " + Slang.MOD_ID);
    }

    public static final Item SLANG_TOOL = register("slang_tool", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));

    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Slang.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.setId(itemKey));

        ItemGroupEvents.modifyEntriesEvent(Slang.SLANG_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.accept(item);
        });

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }
}
