package gg.wildblood.ab_initio;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbInitio implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Ab initio");

    @Override
    public void onInitialize(ModContainer mod) {
        LOGGER.info("Hello Quilt world from {}! Stay fresh!", mod.metadata().name());
    }
}