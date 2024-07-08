package gg.wildblood.ab_initio.compat.emi;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import dev.emi.emi.api.widget.WidgetHolder;
import gg.wildblood.ab_initio.blocks.ModBlocks;

public class AbInitioEmiAnimations extends CreateEmiAnimations {

	public static void addSieve(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			int scale = 22;

			blockElement(ModBlocks.SIEVE_BLOCK.getDefaultState())
				.rotateBlock(22.5, 22.5, 0)
				.scale(scale)
				.render(matrices);
		});
	}

	public static void addComposter(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			int scale = 22;

			blockElement(ModBlocks.COMPOSTER_BLOCK.getDefaultState())
				.rotateBlock(22.5, 22.5, 0)
				.scale(scale)
				.render(matrices);
		});
	}

}
