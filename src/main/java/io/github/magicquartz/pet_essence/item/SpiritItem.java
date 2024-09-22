package io.github.magicquartz.pet_essence.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class Spirit extends Item {
    public Spirit(Settings settings) {
        super(settings.fireproof().maxCount(1).rarity(Rarity.RARE));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand){ //Right Click
        /*BlockHitResult blockHit = raycast(world, playerEntity, RaycastContext.FluidHandling.SOURCE_ONLY);
        if(blockHit.getType().equals(HitResult.Type.BLOCK)) {
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            if(blockState.getBlock().equals(Blocks.WATER)) {
                playerEntity.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                return new TypedActionResult<>(ActionResult.SUCCESS,  new ItemStack(ModArmor.WATER_GLASS_BOWL, 1));
            } else {
                return new TypedActionResult<>(ActionResult.PASS, playerEntity.getStackInHand(hand));
            }
        } else {
            return new TypedActionResult<>(ActionResult.PASS, playerEntity.getStackInHand(hand));
        }*/
        return null;
    }
}
