package gg.wildblood.ab_initio.compat.emi.recipes;

import com.simibubi.create.compat.emi.recipes.CreateEmiRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.emi.emi.api.widget.WidgetHolder;
import gg.wildblood.ab_initio.blocks.custom.sieve.SievingRecipe;
import gg.wildblood.ab_initio.compat.emi.AbInitioEmiAnimations;
import gg.wildblood.ab_initio.compat.emi.AbInitioEmiPlugin;

public class SievingEmiRecipe extends CreateEmiRecipe<SievingRecipe> {
	public SievingEmiRecipe(SievingRecipe recipe) {
		super(AbInitioEmiPlugin.SIEVING, recipe, 177, 110);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 38, 23);
		addTexture(widgets, AllGuiTextures.JEI_ARROW, 80, 51);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 27, 59);

		addSlot(widgets, input.get(0), 11, 15);

		for (int i = 0; i < output.size(); i++) {
			int xOff = (i % 2) * 19;
			int yOff = (i / 2) * -19;
			addSlot(widgets, output.get(i), 128 + xOff, 66 + yOff).recipeContext(this);
		}

		AbInitioEmiAnimations.addSieve(widgets, 41, 65);
	}
}
