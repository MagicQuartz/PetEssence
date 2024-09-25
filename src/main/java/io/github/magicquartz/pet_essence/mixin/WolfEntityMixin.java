package io.github.magicquartz.pet_essence.mixin;

import com.mojang.authlib.GameProfile;
import io.github.magicquartz.pet_essence.registry.ModItems;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity {
    protected WolfEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        WolfEntity wolfEntity = (WolfEntity) (Object) this;

        // Check if the wolf is tamed and has a custom name
        if (wolfEntity.isTamed() && wolfEntity.hasCustomName()) {
            // Get the wolf's custom name
            Text customName = wolfEntity.getCustomName();

            // Create an ItemStack of the Spirit item
            ItemStack spiritStack = new ItemStack(ModItems.SPIRIT);

            // Create NBT data for the spirit item
            NbtCompound nbt = new NbtCompound();

            // Copy relevant NBT data from the wolf, excluding Pos, Motion, and Rotation
            wolfEntity.writeNbt(nbt); // Write all data

            if (source.getAttacker() instanceof PlayerEntity player)
            {
                if(!this.isOwner(player))
                {
                    petToSpirit(nbt, spiritStack, customName, source);
                }
            } else
                petToSpirit(nbt, spiritStack, customName, source);
        }
    }

    @Unique
    private void petToSpirit(NbtCompound nbt, ItemStack spiritStack, Text customName, DamageSource source)
    {
        nbt.remove("Pos");
        nbt.remove("Motion");
        nbt.remove("Rotation");
        nbt.remove("Fire");
        nbt.remove("Sitting");

        nbt.putInt("CustomModelData", 95); // 95 is the previous id of the wolf in spawn eggs and spawners

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

        UUID uuid = nbt.getUuid("Owner");
        String username = (this.getOwner() != null) ? this.getOwner().getEntityName() : "None";;
        //String username = getWorld().getServer().getUserCache().getByUuid(uuid).get().getName();
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

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void injectInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        World world = player.getWorld();
        if (stack.getItem() == ModItems.APPLE && player.isSneaking()) {
            NbtCompound nbt = new NbtCompound();
            this.writeNbt(nbt); // Write into NBT

            // Check if the player is the owner of the wolf
            if (this.isOwner(player)) {
                if (nbt.getInt("Allied") == 0) {
                    nbt.putInt("Allied", 1);
                    this.readNbt(nbt);
                    //this.setSitting(!this.isSitting()); // Change sitting status
                    world.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                            SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    stack.decrement(1); // Consume the item
                    cir.setReturnValue(ActionResult.CONSUME);
                } else if (nbt.getInt("Allied") == 1) {
                    player.sendMessage(Text.literal("Your pet is already protected!"), true);
                    cir.setReturnValue(ActionResult.PASS);
                } else {
                    player.sendMessage(Text.literal("There is an error with the pet's NBT! Try setting 'Allied' to 0!"), true);
                    cir.setReturnValue(ActionResult.PASS);
                }
            } else {
                player.sendMessage(Text.literal("You can only add this to your own pet!"), true);
                cir.setReturnValue(ActionResult.PASS);
            }
        } else {
            super.interactMob(player, hand);
        }
    }
}
