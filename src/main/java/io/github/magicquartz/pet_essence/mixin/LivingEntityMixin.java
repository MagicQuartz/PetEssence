package io.github.magicquartz.pet_essence.mixin;

import io.github.magicquartz.pet_essence.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
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

    @Inject(method = "onDeath", at = @At("TAIL"), cancellable = true)
    public void deathAction(DamageSource damageSource, CallbackInfo ci)
    {
        if(((LivingEntity) (Object) this) instanceof HorseEntity entity)
        {
            deathChecks(entity, damageSource, 100);
        }
    }


}
