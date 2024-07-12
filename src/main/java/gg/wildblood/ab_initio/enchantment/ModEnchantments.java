package gg.wildblood.ab_initio.enchantment;

import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.enchantment.custom.HammeringEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;

public class ModEnchantments {
	public static final HammeringEnchantment HAMMERING_ENCHANTMENT = new HammeringEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});

	private static void registerEnchantment(String enchantmentName, Enchantment enchantment) {
		Registry.register(Registries.ENCHANTMENT, new Identifier(AbInitio.MOD_ID, enchantmentName), enchantment);
	}

	public static void register(ModContainer mod) {
		registerEnchantment("hammering", HAMMERING_ENCHANTMENT);
	}
}
