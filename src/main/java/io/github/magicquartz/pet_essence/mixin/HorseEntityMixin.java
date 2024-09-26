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

    @Unique
    private void deathHandle(DamageSource source) {
        // On cat (or parrot, to be added) death
        HorseEntity entity = (HorseEntity) (Object) this;
        deathChecks(entity, source, 100);
    }

    @Unique
    private void deathChecks(HorseEntity entity, DamageSource source, int modelData)
    {
        // Check if the wolf is tamed and has a custom name
        if (entity.isTame() && entity.hasCustomName()) {

            // Get the wolf's custom name
            Text customName = entity.getCustomName();

            // Create an ItemStack of the Spirit item
            ItemStack spiritStack = new ItemStack(ModItems.SPIRIT);

            // Create NBT data for the spirit item
            NbtCompound nbt = new NbtCompound();

            // Copy relevant NBT data from the wolf, excluding Pos, Motion, and Rotation
            entity.writeNbt(nbt); // Write all data into nbt variable
            // If pet died because of player
            if (source.getAttacker() instanceof PlayerEntity player)
            {
                //If player who killed pet is not owner
                if(entity.getOwnerUuid() != null)
                {
                    if(!entity.getOwnerUuid().equals(player.getUuid()))
                    {
                        horseToSpirit(nbt, spiritStack, customName, source, modelData);
                    }
                } else
                    horseToSpirit(nbt, spiritStack, customName, source, modelData);
            } else
                horseToSpirit(nbt, spiritStack, customName, source, modelData);
        }
    }

    @Unique
    private void horseToSpirit(NbtCompound nbt, ItemStack spiritStack, Text customName, DamageSource source, int modelData)
    {
        nbt.remove("Pos");
        nbt.remove("Motion");
        nbt.remove("Rotation");
        nbt.remove("Fire");
        nbt.putInt("Health", 15);

        nbt.putInt("CustomModelData", modelData); // number by old spawn eggs numbers in older minecraft versions

        // Set the NBT data to the spirit item
        spiritStack.setNbt(nbt);

        // Set the item name to "<Wolf's name>'s Spirit"
        String spiritName = "Spirit of " + customName.getString();
        spiritStack.setCustomName(Text.literal(spiritName).styled(style -> style.withItalic(false)));

        NbtCompound displayTag = spiritStack.getOrCreateSubNbt("display");

        // Create a list for the lore
        NbtList loreListTag = new NbtList();

        //Create a lore entry based on the cause of death
        String deathCause = source.getType().msgId().toLowerCase(); // Get the cause of death message ID
        Text lore = Text.literal("Caused by " + deathCause).styled(style -> style.withItalic(false).withColor(Formatting.BLUE));

        MinecraftServer server = getWorld().getServer();
        String username;
        UUID uuid = ((HorseEntity) (Object) this).getOwnerUuid();
        if(server != null && uuid != null)
            username = server.getUserCache().getByUuid(uuid).get().getName();
        else
            username = "Unknown";
        Text ownerLore = Text.literal("Owner: " + username).styled(style -> style.withItalic(false).withColor(Formatting.DARK_GRAY));

        // Add lines of lore (as JSON-formatted strings)
        loreListTag.add(NbtString.of(Text.Serializer.toJson(lore)));
        loreListTag.add(NbtString.of(Text.Serializer.toJson(ownerLore)));

        // Set the lore in the 'display' tag
        displayTag.put("Lore", loreListTag);

        // Ensure the 'display' tag is attached to the ItemStack
        spiritStack.getOrCreateNbt().put("display", displayTag);

        // Drop the item into the world
        this.dropStack(spiritStack);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        HorseEntity entity = (HorseEntity) (Object) this;
        System.out.println("Taking " + amount + " damage");
        if (getAllied() == 1 && entity.getOwnerUuid() != null) {
            if ((source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.PLAYER_EXPLOSION
            )) && entity.getOwnerUuid().equals(((PlayerEntity) source.getAttacker()).getUuid()))
                return false;
        }
        if(amount >= getHealth())
        {
            deathHandle(source);
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
