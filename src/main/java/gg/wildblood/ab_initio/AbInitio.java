package gg.wildblood.ab_initio;

import com.simibubi.create.foundation.data.CreateRegistrate;
import gg.wildblood.ab_initio.blocks.ModBlocks;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import gg.wildblood.ab_initio.groups.ModGroups;
import gg.wildblood.ab_initio.item.ModItems;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbInitio implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Ab initio");
	public static final String MOD_ID = "ab_initio";

	public static final CreateRegistrate AB_REGISTRATE = CreateRegistrate.create(MOD_ID);

    @Override
    public void onInitialize(ModContainer mod) {
        LOGGER.info("Hello Quilt world from {}! Stay fresh!", mod.metadata().name());

		ModItems.registerItem(mod);
		ModBlocks.register();

		AB_REGISTRATE.register();

		ModRecipeTypes.register();
		ModGroups.register(mod);
    }

	public static Identifier asResource(String path){
		return new Identifier(AbInitio.MOD_ID, path);
	}
}
