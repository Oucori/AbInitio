package gg.wildblood.ab_initio.item;

import gg.wildblood.ab_initio.item.custom.ClayBucket;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import gg.wildblood.ab_initio.AbInitio;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class ModItems {
	public static final Item CLAY_BUCKET_RAW = new Item(new QuiltItemSettings().maxCount(16));
	public static final Item CLAY_BUCKET = new ClayBucket(Fluids.EMPTY, new QuiltItemSettings().maxCount(16));
	public static final Item CLAY_WATER_BUCKET = new ClayBucket(Fluids.WATER, new QuiltItemSettings().maxCount(1));
	public static final Item CLAY_LAVA_BUCKET = new ClayBucket(Fluids.LAVA, new QuiltItemSettings().maxCount(1));
	public static final ShearsItem WOODEN_SHEARS = new ShearsItem(new QuiltItemSettings().maxCount(1).maxDamage(30));
	public static final Item PLANT_FIBER = new Item(new QuiltItemSettings());
	public static final Item PEBBLE = new Item(new QuiltItemSettings());
	public static final Item ANDESITE_PEBBLE = new Item(new QuiltItemSettings());

	// Todo: Maybe something else fany with the hammer?
	public static final PickaxeItem STONE_HAMMER = new PickaxeItem(ToolMaterials.STONE, 3, -2.8F, new QuiltItemSettings());

	private static void registerItem(String itemName, Item item) {
		Registry.register(Registries.ITEM, new Identifier(AbInitio.MOD_ID, itemName), item);
	}

	public static void registerItem(ModContainer mod) {
		registerItem("clay_bucket_raw", CLAY_BUCKET_RAW);
		registerItem("clay_bucket", CLAY_BUCKET);
		registerItem("clay_water_bucket", CLAY_WATER_BUCKET);
		registerItem("clay_lava_bucket", CLAY_LAVA_BUCKET);
		registerItem("wooden_shears", WOODEN_SHEARS);
		registerItem("plant_fiber", PLANT_FIBER);
		registerItem("pebble", PEBBLE);
		registerItem("andesite_pebble", ANDESITE_PEBBLE);
		registerItem("hammer", STONE_HAMMER);
	}
}
