package io.github.magicquartz.pet_essence.mixin;

import com.mojang.authlib.GameProfile;
import io.github.magicquartz.pet_essence.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
                UUID playerUUID = player.getUuid();
                if(!playerUUID.toString().equals(nbt.getUuid("Owner").toString()))
                {
                    petToSpirit(nbt, spiritStack, customName, source);
                }
            } else
                petToSpirit(nbt, spiritStack, customName, source);
        }
    }

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
}
