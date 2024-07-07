package gg.wildblood.ab_initio.item;

import gg.wildblood.ab_initio.AbInitio;
import net.minecraft.item.Item;
import net.minecraft.item.ShearsItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class ModItems {
	public static final ShearsItem WOODEN_SHEARS = new ShearsItem(new QuiltItemSettings().maxCount(1).maxDamage(30));
	public static final Item PLANT_FIBER = new Item(new QuiltItemSettings());

	private static void registerItem(String itemName, Item item) {
		Registry.register(Registries.ITEM, new Identifier(AbInitio.MOD_ID, itemName), item);
	}

	public static void registerItem(ModContainer mod) {
		registerItem("wooden_shears", WOODEN_SHEARS);
		registerItem("plant_fiber", PLANT_FIBER);
	}
}
