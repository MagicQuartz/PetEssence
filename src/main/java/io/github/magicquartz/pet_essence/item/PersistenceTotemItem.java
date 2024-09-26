package io.github.magicquartz.pet_essence.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class PersistenceTotemItem extends Item {
    public PersistenceTotemItem(Settings settings) {
        super(settings.fireproof().maxCount(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        tooltip.add(Text.literal("Can be used to bring a lost, bound soul back to life.").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED).withItalic(false)));
        tooltip.add(Text.literal("Does not save you from death.").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED).withItalic(false)));
    }
}
