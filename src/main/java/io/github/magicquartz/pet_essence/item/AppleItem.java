package io.github.magicquartz.pet_essence.item;

import io.github.magicquartz.pet_essence.mixin.TameableEntityMixin;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class AppleItem extends Item {
    public AppleItem(Settings settings) {
        super(settings
                .fireproof()
                .rarity(Rarity.EPIC)
                .food(new FoodComponent.Builder()
                        .alwaysEdible()
                        .hunger(4)
                        .saturationModifier(9.6f)
                        .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120, 3), 100f)
                        .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300), 100f)
                        .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20, 1), 100f)
                        .statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 300), 100f)
                        .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 1), 100f)
                        .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, 300, 1), 100f)
                        .statusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 300, 1), 100f)
                        .build()));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        World world = player.getWorld();
        // Check if the target is a tameable entity (specifically a wolf or a cat)
        if (player.isSneaking() && target instanceof TameableEntity tameable) {
            // Check if it's a wolf or a cat
            if (tameable instanceof WolfEntity || tameable instanceof CatEntity) {
                if (!world.isClient()) {
                    NbtCompound nbt = new NbtCompound();
                    tameable.writeNbt(nbt); // Write into nbt
                    if(tameable.isOwner(player))
                    {
                        if(nbt.getInt("Allied") == 0)
                        {
                            // Set the custom "Allied" NBT tag to 1
                            nbt.putInt("Allied", 1);
                            tameable.readNbt(nbt);

                            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                                    SoundCategory.NEUTRAL, 1.0F, 1.0F);
                            // Reduce the item stack by 1 after use
                            stack.decrement(1);
                            return ActionResult.CONSUME;
                        } else if(nbt.getInt("Allied") == 1) {
                            player.sendMessage(Text.literal("Your pet is already protected!"), true);
                            return ActionResult.PASS;
                        } else
                        {
                            player.sendMessage(Text.literal("There is an error with the pet's NBT! Try setting 'Allied' to 0!"), true);
                            return ActionResult.PASS;
                        }
                    } else
                    {
                        player.sendMessage(Text.literal("You can only add this to your own pet!"), true);
                        return ActionResult.PASS;
                    }
                }
                return ActionResult.PASS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
