package gg.wildblood.ab_initio.item;

import net.minecraft.item.BucketItem;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class ModItems {
	public static final ShearsItem WOODEN_SHEARS = new ShearsItem(new QuiltItemSettings().maxCount(1).maxDamage(30));

	public static void register(ModContainer mod) {
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "wooden_shears"), WOODEN_SHEARS);
	}
}
