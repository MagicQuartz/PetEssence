package io.github.magicquartz.pet_essence.registry;

import io.github.magicquartz.pet_essence.Main;
import io.github.magicquartz.pet_essence.item.SpiritItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item SPIRIT = new SpiritItem(new Item.Settings());
    public static final Item TOTEM = new Item(new Item.Settings().maxCount(1).rarity(Rarity.EPIC));

    public static void register() {
        //Spirit
        Registry.register(Registries.ITEM, Main.identifier("spirit"), SPIRIT);
        //Totem
        Registry.register(Registries.ITEM, Main.identifier("persistence_totem"), TOTEM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(SPIRIT);
            content.add(TOTEM);
        });
    }
}
