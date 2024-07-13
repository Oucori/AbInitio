package gg.wildblood.ab_initio.blocks;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.custom.cauldron.ClayCauldronBlock;
import gg.wildblood.ab_initio.blocks.custom.cauldron.ClayCauldronEntity;
import gg.wildblood.ab_initio.blocks.custom.cauldron.ClayCauldronRenderer;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveBlock;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveBlockEntity;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveInstance;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import net.minecraft.client.render.RenderLayer;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static gg.wildblood.ab_initio.AbInitio.AB_REGISTRATE;

public class ModBlocks {
	public static final BlockEntry<SieveBlock> SIEVE_BLOCK =
		AB_REGISTRATE.block("sieve", SieveBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.properties(p -> p.mapColor(MapColor.BROWN))
			.transform(axeOrPickaxe())
			.transform(BlockStressDefaults.setImpact(4.0))
			.transform(BuilderTransformers.bearing("windmill", "gearbox"))
			.addLayer(() -> RenderLayer::getCutoutMipped)
			.blockstate(BlockStateGen.directionalBlockProvider(true))
			.item()
			.tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.transform(customItemModel())
			.register();

	public static final BlockEntityEntry<SieveBlockEntity> SIEVE_BLOCK_ENTITY = AB_REGISTRATE
		.blockEntity("sieve", SieveBlockEntity::new)
		.instance(() -> SieveInstance::new)
		.validBlocks(SIEVE_BLOCK)
		.renderer(() -> SieveRenderer::new)
		.register();

	public static final BlockEntry<ClayCauldronBlock> CLAY_CAULDRON_BLOCK =
		AB_REGISTRATE.block("clay_cauldron", ClayCauldronBlock::new)
			.initialProperties(() -> Blocks.TERRACOTTA)
			.properties(p -> p.mapColor(MapColor.BROWN_TERRACOTTA))
			.transform(axeOrPickaxe())
			.addLayer(() -> RenderLayer::getCutoutMipped)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntityEntry<ClayCauldronEntity> CLAY_CAULDRON_ENTITY = AB_REGISTRATE
		.blockEntity("clay_cauldron", ClayCauldronEntity::new)
		.validBlocks(CLAY_CAULDRON_BLOCK)
		.renderer(() -> ClayCauldronRenderer::new)
		.register();


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
