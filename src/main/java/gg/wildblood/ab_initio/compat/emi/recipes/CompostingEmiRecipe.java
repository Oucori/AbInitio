package gg.wildblood.ab_initio.compat.emi.recipes;

import com.simibubi.create.compat.emi.recipes.CreateEmiRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.emi.emi.api.widget.WidgetHolder;
import gg.wildblood.ab_initio.blocks.custom.composter.ComposterRecipe;
import gg.wildblood.ab_initio.compat.emi.AbInitioEmiAnimations;
import gg.wildblood.ab_initio.compat.emi.AbInitioEmiPlugin;

public class CompostingEmiRecipe extends CreateEmiRecipe<ComposterRecipe> {
	public CompostingEmiRecipe(ComposterRecipe recipe) {
		super(AbInitioEmiPlugin.COMPOSTING, recipe, 177, 61);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 43, 4);
		addTexture(widgets, AllGuiTextures.JEI_ARROW, 85, 32);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 32, 40);

		addSlot(widgets, input.get(0), 14, 8);

		for (int i = 0; i < output.size(); i++) {
			int xOff = (i % 2) * 19;
			int yOff = (i / 2) * -19;
			addSlot(widgets, output.get(i), 133 + xOff, 27 + yOff).recipeContext(this);
		}

		AbInitioEmiAnimations.addComposter(widgets, 46, 45);
	}
}
