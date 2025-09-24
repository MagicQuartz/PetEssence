package io.github.magicquartz.pet_essence.mixin;

import io.github.magicquartz.pet_essence.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(HorseEntity.class)
public abstract class HorseEntityMixin extends LivingEntity {
    @Unique
    private static final TrackedData<Integer> ALLIED = DataTracker.registerData(HorseEntityMixin.class, TrackedDataHandlerRegistry.INTEGER);

    public HorseEntityMixin(EntityType<? extends HorseEntity> entityType, World world) {
        super(entityType, world);
    }

    //The death checks for the horse have been moved to LivingEntityMixin

    @Override
    public boolean damage(DamageSource source, float amount) {
        HorseEntity entity = (HorseEntity) (Object) this;
        System.out.println("Taking " + amount + " damage");
        if (getAllied() == 1 && entity.getOwnerUuid() != null) {
            if ((source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.PLAYER_EXPLOSION
            )) && entity.getOwnerUuid().equals(((PlayerEntity) source.getAttacker()).getUuid()))
                return false;
        }
        return super.damage(source, amount);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        World world = player.getWorld();
        HorseEntity horse = (HorseEntity) (Object) this;
        if ((stack.isOf(ModItems.APPLE) && player.isSneaking())) {
            if (!world.isClient()) {
                NbtCompound nbt = new NbtCompound();
                horse.writeNbt(nbt); // Write into nbt
                if(horse.getOwnerUuid() != null)
                {
                    if(horse.getOwnerUuid().equals(player.getUuid()))
                    {
                        if(nbt.getInt("Allied") == 0)
                        {
                            // Set the custom "Allied" NBT tag to 1
                            nbt.putInt("Allied", 1);
                            horse.readNbt(nbt);

                            world.playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                                    SoundCategory.NEUTRAL, 1.0F, 1.0F);
                            // Reduce the item stack by 1 after use
                            stack.decrement(1);
                            cir.setReturnValue(ActionResult.CONSUME);
                            cir.cancel();
                        } else if(nbt.getInt("Allied") == 1) {
                            player.sendMessage(Text.literal("Your horse is already protected!"), true);
                            cir.setReturnValue(ActionResult.PASS);
                            cir.cancel();
                        } else
                        {
                            player.sendMessage(Text.literal("There is an error with the horse's NBT! Try setting 'Allied' to 0!"), true);
                            cir.setReturnValue(ActionResult.PASS);
                            cir.cancel();
                        }
                    } else
                    {
                        player.sendMessage(Text.literal("You can only add this to your own horse!"), true);
                        cir.setReturnValue(ActionResult.PASS);
                        cir.cancel();
                    }
                }
            }
        } else if(stack.isOf(ModItems.TAKEOVER_TOTEM))
        {
            if (!world.isClient()) {
                if(horse.getOwnerUuid() != null)
                {
                    if(!horse.getOwnerUuid().equals(player.getUuid()))
                    {
                        // Set the custom "Allied" NBT tag to 1
                        horse.setOwnerUuid(player.getUuid());

                        String horseName = horse.getCustomName() != null ? horse.getCustomName().getString() : "The horse";
                        player.sendMessage(Text.literal(horseName + " has forgotten their previous owner..."), true);

                        world.playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                                SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE,
                                SoundCategory.NEUTRAL, 1.0F, 1.0F);
                        // Reduce the item stack by 1 after use
                        stack.decrement(1);
                        cir.setReturnValue(ActionResult.CONSUME);
                        cir.cancel();
                    } else
                    {
                        player.sendMessage(Text.literal("You can't make your own horse forget you!"), true);
                        cir.setReturnValue(ActionResult.PASS);
                        cir.cancel();
                    }
                } else
                {
                    player.sendMessage(Text.literal("This horse is not tamed!"), true);
                    cir.setReturnValue(ActionResult.PASS);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initAlliedDataTracker(CallbackInfo ci) {
        ((HorseEntity) (Object) this).getDataTracker().startTracking(ALLIED, 0); // Default to 0 (not allied)
    }

    @Unique
    public int getAllied() {
        return ((HorseEntity) (Object) this).getDataTracker().get(ALLIED);
    }

    @Unique
    public void setAllied(int value) {
        ((HorseEntity) (Object) this).getDataTracker().set(ALLIED, value);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeAlliedToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("Allied", getAllied());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readAlliedFromNbt(NbtCompound nbt, CallbackInfo ci) {
        setAllied(nbt.getInt("Allied"));
    }
}
