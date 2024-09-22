package io.github.magicquartz.pet_essence;

import io.github.magicquartz.pet_essence.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
    public static final String MOD_ID = "pet_essence";

    @Override
    public void onInitialize() {
        ModItems.register();
    }

    public static Identifier identifier(String id) {
        return new Identifier(MOD_ID, id);
    }
}
