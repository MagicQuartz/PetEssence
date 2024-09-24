package io.github.magicquartz.pet_essence.registry;

import io.github.magicquartz.pet_essence.Main;
import io.github.magicquartz.pet_essence.item.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item SPIRIT = new Item(new Item.Settings().fireproof().maxCount(1).rarity(Rarity.RARE));
    public static final Item PERSISTENT_SPIRIT = new PersistentSpiritItem(new Item.Settings());
    public static final Item ALLIED_SPIRIT = new AlliedSpiritItem(new Item.Settings());
    public static final Item PERSISTENCE_TOTEM = new PersistenceTotemItem(new Item.Settings());
    public static final Item TAKEOVER_TOTEM = new TakeoverTotemItem(new Item.Settings());
    public static final Item APPLE = new AppleItem(new Item.Settings());

    public static void register() {
        //Spirit
        Registry.register(Registries.ITEM, Main.identifier("spirit"), SPIRIT);
        Registry.register(Registries.ITEM, Main.identifier("spirit_persist"), PERSISTENT_SPIRIT);
        Registry.register(Registries.ITEM, Main.identifier("allied_spirit"), ALLIED_SPIRIT);
        //Totem
        Registry.register(Registries.ITEM, Main.identifier("persistence_totem"), PERSISTENCE_TOTEM);
        Registry.register(Registries.ITEM, Main.identifier("takeover_totem"), TAKEOVER_TOTEM);
        //Apple
        Registry.register(Registries.ITEM, Main.identifier("apple_of_alliance"), APPLE);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(SPIRIT);
            content.add(PERSISTENT_SPIRIT);
            content.add(ALLIED_SPIRIT);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(APPLE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(PERSISTENCE_TOTEM);
            content.add(TAKEOVER_TOTEM);
        });
    }
}
