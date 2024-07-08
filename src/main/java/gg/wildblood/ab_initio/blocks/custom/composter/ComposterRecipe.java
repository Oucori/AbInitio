package gg.wildblood.ab_initio.blocks.custom.composter;

import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;

public class ComposterRecipe extends ProcessingRecipe<Inventory> {
	private int inputCount = 0;

	public ComposterRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
		super(ModRecipeTypes.COMPOSTING, params);
	}

	public ComposterRecipe(IRecipeTypeInfo typeInfo, ProcessingRecipeBuilder.ProcessingRecipeParams params) {
		super(typeInfo, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 8;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
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

	public int getInputCount() {
		return this.inputCount;
	}

	@Override
	public void readAdditional(JsonObject json) {
			this.inputCount = json.get("count").getAsInt();
	}
}
