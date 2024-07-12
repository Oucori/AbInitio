package gg.wildblood.ab_initio.enchantment.custom;

import gg.wildblood.ab_initio.item.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;

public class HammeringEnchantment extends Enchantment {
	public HammeringEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
		super(weight, type, slotTypes);
	}

	@Override
	protected boolean canAccept(Enchantment other) {
		return this != other && other != Enchantments.SILK_TOUCH;
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return this.type.isAcceptableItem(stack.getItem()) && stack.getItem() != ModItems.STONE_HAMMER;
	};
}
