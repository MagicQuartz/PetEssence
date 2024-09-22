package io.github.magicquartz.pet_essence.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.magicquartz.pet_essence.registry.ModItems;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    // This is to prevent explosion damage to the spirit
    @ModifyExpressionValue(
            method = "damage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0)
    )
    private boolean modifyIsNetherStar(boolean original, DamageSource source) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        ItemStack stack = itemEntity.getStack();

        // Original condition checks if the item is a Nether Star
        if (original) {
            return true;
        }

        // Additional condition to prevent damage to ModItems.SPIRIT from explosions
        if (!stack.isEmpty() && stack.isOf(ModItems.SPIRIT) && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            return true;
        }

        return false;
    }
}
