package gg.wildblood.ab_initio.groups;

import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.ModBlocks;
import gg.wildblood.ab_initio.enchantment.ModEnchantments;
import gg.wildblood.ab_initio.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;

public class ModGroups {
	public static final RegistryKey<ItemGroup> AB_INITIO_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(AbInitio.MOD_ID, "item_group"));
	public static final ItemGroup AB_INITIO_ITEM_GROUP = FabricItemGroup.builder()
		.icon(() -> new ItemStack(ModItems.WOODEN_SHEARS))
		.name(Text.translatable("ab_initio.item_group"))
		.build();

	public static void register(ModContainer mod) {
		Registry.register(Registries.ITEM_GROUP, AB_INITIO_ITEM_GROUP_KEY, AB_INITIO_ITEM_GROUP);

		// Register items to the custom item group.
		ItemGroupEvents.modifyEntriesEvent(AB_INITIO_ITEM_GROUP_KEY).register(itemGroup -> {
			// Tools
			itemGroup.addItem(ModItems.WOODEN_SHEARS);
			itemGroup.addItem(ModItems.CLAY_BUCKET_RAW);
			itemGroup.addItem(ModItems.CLAY_BUCKET);
			itemGroup.addItem(ModItems.CLAY_WATER_BUCKET);
			itemGroup.addItem(ModItems.CLAY_LAVA_BUCKET);
			itemGroup.addItem(ModItems.STONE_HAMMER);

			// Materials
			itemGroup.addItem(ModItems.ANDESITE_PEBBLE);
			itemGroup.addItem(ModItems.PEBBLE);
			itemGroup.addItem(ModItems.PLANT_FIBER);

			// Blocks
			itemGroup.addItem(ModBlocks.SIEVE_BLOCK.asItem());
		});
	}
}
