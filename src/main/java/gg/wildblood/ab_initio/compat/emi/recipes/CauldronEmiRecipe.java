package gg.wildblood.ab_initio.compat.emi.recipes;

import com.google.common.collect.Lists;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.compat.emi.recipes.CreateEmiRecipe;
import com.simibubi.create.compat.emi.recipes.basin.MixingEmiRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.custom.cauldron.CauldronRecipe;
import gg.wildblood.ab_initio.compat.emi.AbInitioEmiAnimations;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

import java.util.List;

public class CauldronEmiRecipe extends CreateEmiRecipe<CauldronRecipe> {
	private final List<EmiIngredient> catalysts = Lists.newArrayList();
	private final boolean needsHeating;

	public CauldronEmiRecipe(EmiRecipeCategory category, CauldronRecipe recipe) {
		super(category, recipe, 177, 108);
		boolean needsHeating = recipe.getRequiredHeat() != HeatCondition.NONE;

		List<EmiStack> outputStack = Lists.newArrayList();

		recipe.getFluidResults().forEach(fluidStack -> {
			outputStack.add(EmiStack.of(fluidStack.getFluid(), fluidStack.getAmount()));
		});

		output = outputStack;


		if (!needsHeating) {
			height = 90;
		}

		this.needsHeating = needsHeating;

		HeatCondition requiredHeat = recipe.getRequiredHeat();

		if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
			catalysts.add(EmiStack.of(AllBlocks.BLAZE_BURNER.get()));
		}
		if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED)) {
			catalysts.add(EmiStack.of(AllItems.BLAZE_CAKE.get()));
		}
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		int inputSize = input.size();
		int outputSize = output.size();
		int vRows = (1 + outputSize) / 2;
		HeatCondition requiredHeat = recipe.getRequiredHeat();

		if (vRows <= 2) {
			addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 136, 32 - 19 * (vRows - 1));
		}

		boolean noHeat = requiredHeat == HeatCondition.NONE;
		AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
		addTexture(widgets, shadow, 81, 58 + (noHeat ? 10 : 30));

		if (needsHeating) {
			AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
			addTexture(widgets, heatBar, 4, 80);
			widgets.addText(Lang.translateDirect(requiredHeat.getTranslationKey()).asOrderedText(), 9, 86, requiredHeat.getColor(), true);
		}

		int xOff = inputSize < 3 ? (3 - inputSize) * 19 / 2 : 0;
		int yOff = 0;

		for (int i = 0; i < inputSize; i++) {
			EmiIngredient stack = input.get(i);
			addSlot(widgets, stack, xOff + 16 + (i % 3) * 19, yOff + 50 + (i / 3) * 19);
		}


		for (int i = 0; i < output.size(); i++) {
			int x = 140 - (outputSize % 2 != 0 && i == outputSize - 1 ? 0 : i % 2 == 0 ? 10 : -9);
			int y = 50 - 20 * (i / 2) + yOff;

			EmiStack stack = output.get(i);
			addSlot(widgets, stack, x, y).recipeContext(this);
		}

		if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
			widgets.addSlot(EmiStack.of(AllBlocks.BLAZE_BURNER.get()), 133, 81).drawBack(false).catalyst(true);
		}
		if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED)) {
			widgets.addSlot(EmiStack.of(AllItems.BLAZE_CAKE.get()), 152, 81).drawBack(false);
		}

		if (requiredHeat != HeatCondition.NONE) {
			CreateEmiAnimations.addBlazeBurner(widgets, widgets.getWidth() / 2 + 3, 55, requiredHeat.visualizeAsBlazeBurner());
		}

		AbInitioEmiAnimations.addCauldron(widgets, widgets.getWidth() / 2 + 5, 70);
	}

	/**
	@Override
	public void addWidgets(WidgetHolder widgets) {
		int outputSize = output.size();
		int halfWidth = widgets.getWidth() / 2;

		addSlot(widgets, input.get(0), 3, 20);

		addTexture(widgets, AllGuiTextures.JEI_ARROW, 30, 24);
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, halfWidth + 30, 24);

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE) {
			CreateEmiAnimations.addBlazeBurner(widgets, halfWidth - 5, 35, requiredHeat.visualizeAsBlazeBurner());
		}

		AbInitioEmiAnimations.addCauldron(widgets, halfWidth - 2, 40);

		AbInitio.LOGGER.info("CauldronEmiRecipe: addWidgets: output.size() = " + output.size());

		for (int i = 0; i < outputSize; i++) {
			EmiStack stack = output.get(i);
			addSlot(widgets, stack, halfWidth + 20 + (i * 2), 40).recipeContext(this);
		}

		//addSlot(widgets, output.get(0), halfWidth + 20, 50).recipeContext(this);
	}
	*/
}
