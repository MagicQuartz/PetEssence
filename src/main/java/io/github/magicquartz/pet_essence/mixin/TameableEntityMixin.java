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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin extends AnimalEntity {
    @Shadow public abstract boolean isOwner(LivingEntity entity);

    @Shadow public abstract @Nullable UUID getOwnerUuid();

    protected TameableEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private static final TrackedData<Integer> ALLIED = DataTracker.registerData(TameableEntityMixin.class, TrackedDataHandlerRegistry.INTEGER);

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        // On cat (or parrot, to be added) death

        if(((TameableEntity) (Object) this) instanceof CatEntity entity)
        {
            deathChecks(entity, source, 95);

        } else if(((TameableEntity) (Object) this) instanceof ParrotEntity entity)
        {
            deathChecks(entity, source, 105);
        }
    }

    @Unique
    private void deathChecks(TameableEntity entity, DamageSource source, int modelData)
    {
        // Check if the wolf is tamed and has a custom name
        if (entity.isTamed() && entity.hasCustomName()) {
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
                if(!this.isOwner(player))
                {
                    petToSpirit(nbt, spiritStack, customName, source, modelData);
                }
            } else
                petToSpirit(nbt, spiritStack, customName, source, modelData);
        }
    }

    @Unique
    private void petToSpirit(NbtCompound nbt, ItemStack spiritStack, Text customName, DamageSource source, int modelData)
    {
        nbt.remove("Pos");
        nbt.remove("Motion");
        nbt.remove("Rotation");
        nbt.remove("Fire");
        nbt.remove("Sitting");
        if(modelData == 98) // Cat
            nbt.putInt("Health", 10);
        else if(modelData == 105) // Parrot
            nbt.putInt("Health", 6);

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

        UUID uuid = getOwnerUuid();
        String username = getWorld().getServer().getUserCache().getByUuid(uuid).get().getName();
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
        if (getAllied() == 1) {
            if ((source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.PLAYER_EXPLOSION
            )) && this.isOwner((PlayerEntity) source.getAttacker()))
                return false;
        }
        return super.damage(source, amount);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initAlliedDataTracker(CallbackInfo ci) {
        ((TameableEntity) (Object) this).getDataTracker().startTracking(ALLIED, 0); // Default to 0 (not allied)
    }

    // Getter for the "Allied" status
    @Unique
    public int getAllied() {
        return ((TameableEntity) (Object) this).getDataTracker().get(ALLIED);
    }

    // Setter for the "Allied" status
    @Unique
    public void setAllied(int value) {
        ((TameableEntity) (Object) this).getDataTracker().set(ALLIED, value);
    }

    // Inject into the writeCustomDataToNbt method to save "Allied" data
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeAlliedToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("Allied", getAllied());
    }

    // Inject into the readCustomDataFromNbt method to load "Allied" data
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readAlliedFromNbt(NbtCompound nbt, CallbackInfo ci) {
        setAllied(nbt.getInt("Allied"));
    }
}
