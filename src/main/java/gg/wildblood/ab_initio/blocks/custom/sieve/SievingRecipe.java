package gg.wildblood.ab_initio.blocks.custom.sieve;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;

public class SievingRecipe extends ProcessingRecipe<Inventory> {
	public SievingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
		super(ModRecipeTypes.SIEVING, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		if (inventory.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inventory.getStack(0));
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}
}
