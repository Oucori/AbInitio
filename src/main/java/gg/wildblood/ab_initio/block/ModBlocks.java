package gg.wildblood.ab_initio.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class ModBlocks {
	public static final Block AB_INITIO_COMPOSTER = registerBlock("ab_initio_composter", new Block(QuiltBlockSettings.copyOf(Blocks.COMPOSTER)));

	public static BlockItem registerBlockItem(String name, Block block){
		return Registry.register(Registries.ITEM, new Identifier("ab_initio", name),
			new BlockItem(block, new QuiltItemSettings()));
	}

	public static Block registerBlock(String name, Block block){
		registerBlockItem(name, block);
		return Registry.register(Registries.BLOCK, new Identifier("ab_initio",name), block);
	}

	public static void register(ModContainer mod) {

	}
}
