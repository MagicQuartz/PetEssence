package io.github.magicquartz.pet_essence.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public class TakeoverTotemItem extends Item {
    public TakeoverTotemItem(Settings settings) {
        super(settings.fireproof().maxCount(1).rarity(Rarity.RARE));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        World world = player.getWorld();
        if (target instanceof TameableEntity tameable) {

            // Check if it's a wolf or a cat
            //if (tameable instanceof WolfEntity || tameable instanceof CatEntity) {
                if (!world.isClient()) {
                    if(!tameable.isOwner(player))
                    {
                        // Set the custom "Allied" NBT tag to 1
                        tameable.setOwner(player);
                        tameable.setInSittingPose(tameable.isSitting());

                        String petName = target.getCustomName() != null ? target.getCustomName().toString() : "The pet";
                        player.sendMessage(Text.literal(petName + " has forgotten their previous owner..."), true);

                        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                                SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE,
                                SoundCategory.NEUTRAL, 1.0F, 1.0F);
                        // Reduce the item stack by 1 after use
                        stack.decrement(1);
                        return ActionResult.CONSUME;
                    } else
                    {
                        player.sendMessage(Text.literal("You can't make your own pet forget you!"), true);
                        return ActionResult.PASS;
                    }
                }
                return ActionResult.PASS;
            //}
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        tooltip.add(Text.literal("Uses dark magic to make a pet forget").setStyle(Style.EMPTY.withColor(Formatting.BLUE).withItalic(false)));
        tooltip.add(Text.literal("their previous owner.").setStyle(Style.EMPTY.withColor(Formatting.BLUE).withItalic(false)));
    }
}
