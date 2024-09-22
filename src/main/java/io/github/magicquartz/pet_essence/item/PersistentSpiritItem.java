package io.github.magicquartz.pet_essence.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PersistentSpiritItem extends Item {
    public PersistentSpiritItem(Settings settings) {
        super(settings.fireproof().maxCount(1).rarity(Rarity.EPIC));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        } else {
            ItemStack itemStack = context.getStack();
            BlockPos blockPos = context.getBlockPos();

            Direction direction = context.getSide();
            BlockState blockState = world.getBlockState(blockPos);

            PlayerEntity user = context.getPlayer();

            BlockPos spawnPos;
            if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
                spawnPos = blockPos;
            } else {
                spawnPos = blockPos.offset(direction);
            }

            // Retrieve NBT data from the item
            NbtCompound nbt = itemStack.getOrCreateNbt();

            // Check for CustomModelData and ensure it's 95
            if (nbt.contains("CustomModelData")) {
                // Remove lore if it exists in the display tag
                if (nbt.contains("display", 10)) { // 10 is the type ID for compound tags
                    NbtCompound displayTag = nbt.getCompound("display");

                    /*if (displayTag.contains("Lore", 9)) { // 9 is the type ID for list tags
                        displayTag.remove("Lore");
                    }*/

                    // If display tag is now empty, remove it
                    if (displayTag.isEmpty()) {
                        nbt.remove("display");
                    } else {
                        // Otherwise, update the display tag
                        nbt.put("display", displayTag);
                    }
                }
                summonPet(spawnPos, world, nbt, user);
                itemStack.setCount(0); // Consume the item
                user.incrementStat(Stats.USED.getOrCreateStat(this));

                return ActionResult.CONSUME;
            }

            // If CustomModelData is not 95, return success without doing anything
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(world, user, RaycastContext.FluidHandling.ANY);

        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(itemStack);
        } else if (!(world instanceof ServerWorld)) {
            return TypedActionResult.success(itemStack);
        } else {
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!(world.getBlockState(blockPos).getBlock() instanceof FluidBlock)) {
                return TypedActionResult.pass(itemStack);
            } else if (world.canPlayerModifyAt(user, blockPos) && user.canPlaceOn(blockPos, blockHitResult.getSide(), itemStack)) {
                // Retrieve NBT data from the item
                NbtCompound nbt = itemStack.getOrCreateNbt();

                // Check for CustomModelData and ensure it's 95
                if (nbt.contains("CustomModelData")) {
                    // Remove lore if it exists in the display tag
                    if (nbt.contains("display", 10)) { // 10 is the type ID for compound tags
                        NbtCompound displayTag = nbt.getCompound("display");

                        // If display tag is now empty, remove it
                        if (displayTag.isEmpty()) {
                            nbt.remove("display");
                        } else {
                            // Otherwise, update the display tag
                            nbt.put("display", displayTag);
                        }
                    }
                    Vec3d vector = summonPet(blockPos, world, nbt, user);
                    itemStack.setCount(0); // Consume the item
                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    if(vector != null)
                        world.emitGameEvent(user, GameEvent.ENTITY_PLACE, vector);
                    return TypedActionResult.consume(itemStack);
                } else {
                    return TypedActionResult.pass(itemStack);
                }
            } else {
                return TypedActionResult.fail(itemStack);
            }
        }
    }

    private Vec3d summonPet(BlockPos blockPos, World world, NbtCompound nbt, PlayerEntity user) {
        int CustomModelData = nbt.getInt("CustomModelData");
        switch (CustomModelData)
        {
            case 95: // Wolf
                // Create and spawn the wolf
                WolfEntity wolf = EntityType.WOLF.create(world);
                if (wolf != null) {
                    wolf.readNbt(nbt); // Read the NBT data into the wolf
                    wolf.setPosition(blockPos.toCenterPos().getX(), blockPos.getY(), blockPos.toCenterPos().getZ()); // Set the position
                    world.spawnEntity(wolf);
                    // Play the sound
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE,
                            SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return wolf.getPos();
                }
                return null;
            case 98: // Cat
                // Create and spawn the cat
                CatEntity cat = EntityType.CAT.create(world);
                if (cat != null) {
                    cat.readNbt(nbt); // Read the NBT data into the cat
                    cat.setPosition(blockPos.toCenterPos().getX(), blockPos.getY(), blockPos.toCenterPos().getZ()); // Set the position
                    world.spawnEntity(cat);

                    // Play the sound
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE,
                            SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return cat.getPos();
                }
                return null;
            default:
                return null;
        }
    }
}
