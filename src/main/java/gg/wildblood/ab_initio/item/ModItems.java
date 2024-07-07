package gg.wildblood.ab_initio.item;

import gg.wildblood.ab_initio.item.custom.ClayBucket;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
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
	public static final Item CLAY_BUCKET_RAW = new Item(new QuiltItemSettings().maxCount(16));
	public static final Item CLAY_BUCKET = new ClayBucket(Fluids.EMPTY, new QuiltItemSettings().maxCount(16));
	public static final Item CLAY_WATER_BUCKET = new ClayBucket(Fluids.WATER, new QuiltItemSettings().maxCount(1));
	public static final Item CLAY_LAVA_BUCKET = new ClayBucket(Fluids.LAVA,new QuiltItemSettings().maxCount(1));
  	public static final ShearsItem WOODEN_SHEARS = new ShearsItem(new QuiltItemSettings().maxCount(1).maxDamage(30));

	public static void register(ModContainer mod) {
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "clay_bucket_raw"), CLAY_BUCKET_RAW);
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "clay_bucket"), CLAY_BUCKET);
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "clay_water_bucket"), CLAY_WATER_BUCKET);
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "clay_lava_bucket"), CLAY_LAVA_BUCKET);
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "wooden_shears"), WOODEN_SHEARS);
	}
}
