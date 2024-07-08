package gg.wildblood.ab_initio.blocks;

import com.simibubi.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.custom.composter.ComposterBlock;
import gg.wildblood.ab_initio.blocks.custom.composter.ComposterBlockEntity;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveBlock;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import net.minecraft.client.render.RenderLayer;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static gg.wildblood.ab_initio.AbInitio.AB_REGISTRATE;

public class ModBlocks {

	//--Sieve
	public static final BlockEntry<SieveBlock> SIEVE_BLOCK =
		AB_REGISTRATE.block("sieve", SieveBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.transform(axeOrPickaxe())
			.addLayer(() -> RenderLayer::getCutoutMipped)
			.blockstate(BlockStateGen.directionalBlockProvider(true))
			.simpleItem()
			.register();
	public static final BlockEntityEntry<SieveBlockEntity> SIEVE_BLOCK_ENTITY = AB_REGISTRATE
		.blockEntity("sieve", SieveBlockEntity::new)
		.validBlocks(SIEVE_BLOCK)
		.register();

	//--Composter
	public static final BlockEntry<ComposterBlock> COMPOSTER_BLOCK = AB_REGISTRATE.block("composter", ComposterBlock::new)
		.initialProperties(() -> Blocks.COMPOSTER)
		.transform(axeOrPickaxe())
		.addLayer(() -> RenderLayer::getCutoutMipped)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.simpleItem()
		.register();

	public static final BlockEntityEntry<ComposterBlockEntity> COMPOSTER_BLOCK_ENTITY = AB_REGISTRATE
		.blockEntity("composter", ComposterBlockEntity::new)
		.validBlocks(COMPOSTER_BLOCK)
		.register();

//--------------------------------------------------------------------------------------------------------
	private static void registerBlock(String blockName, Block block) {
		Registry.register(Registries.BLOCK, new Identifier(AbInitio.MOD_ID, blockName), block);
		registerBlockItem(blockName, block);
	}

	private static void registerBlockItem(String name, Block block) {
		Registry.register(Registries.ITEM, new Identifier(AbInitio.MOD_ID, name), new BlockItem(block, new QuiltItemSettings()));
	}

	public static void register() {
	}
}
