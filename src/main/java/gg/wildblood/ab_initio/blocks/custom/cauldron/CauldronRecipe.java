package gg.wildblood.ab_initio.blocks.custom.cauldron;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.DummyCraftingContainer;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CauldronRecipe extends ProcessingRecipe<Inventory> {
	public CauldronRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
		super(ModRecipeTypes.CAULDRON, params);
	}

	public static boolean apply(ClayCauldronEntity cauldron, Recipe<?> recipe) {
		return apply(cauldron, recipe, true);
	}

	private static boolean apply(ClayCauldronEntity cauldron, Recipe<?> recipe, boolean test) {
		boolean isCauldronRecipe = recipe instanceof CauldronRecipe;
		Storage<ItemVariant> availableItems = cauldron.getItemStorage(null);
		Storage<FluidVariant> availableFluids = cauldron.getFluidStorage(null);

		if (availableItems == null || availableFluids == null)
			return false;

		BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(cauldron.getWorld()
			.getBlockState(cauldron.getPos()
				.down(1)));
		if (isCauldronRecipe && !((CauldronRecipe) recipe).getRequiredHeat()
			.testBlazeBurner(heat))
			return false;

		List<FluidStack> recipeOutputFluids = new ArrayList<>();

		List<Ingredient> ingredients = new LinkedList<>(recipe.getIngredients());
		List<FluidIngredient> fluidIngredients =
			isCauldronRecipe ? ((CauldronRecipe) recipe).getFluidIngredients() : Collections.emptyList();

		// fabric: track consumed items to get remainders later
		DefaultedList<ItemStack> consumedItems = DefaultedList.of();

		try (Transaction t = TransferUtil.getTransaction()) {
			Ingredients: for (Ingredient ingredient : ingredients) {
				for (StorageView<ItemVariant> view : availableItems.nonEmptyViews()) {
					ItemVariant var = view.getResource();
					ItemStack stack = var.toStack();
					if (!ingredient.test(stack)) continue;
					// Catalyst items are never consumed
					ItemStack remainder = stack.getRecipeRemainder();
					if (!remainder.isEmpty() && ItemStack.itemsMatch(remainder, stack))
						continue Ingredients;
					long extracted = view.extract(var, 1, t);
					if (extracted == 0) continue;
					consumedItems.add(stack);
					continue Ingredients;
				}
				// something wasn't found
				return false;
			}

			boolean fluidsAffected = false;
			FluidIngredients: for (FluidIngredient fluidIngredient : fluidIngredients) {
				long amountRequired = fluidIngredient.getRequiredAmount();
				for (StorageView<FluidVariant> view : availableFluids.nonEmptyViews()) {
					FluidStack fluidStack = new FluidStack(view);
					if (!fluidIngredient.test(fluidStack)) continue;
					long drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
					if (view.extract(fluidStack.getType(), drainedAmount, t) == drainedAmount) {
						fluidsAffected = true;
						amountRequired -= drainedAmount;
						if (amountRequired != 0) continue;
						continue FluidIngredients;
					}
				}
				// something wasn't found
				return false;
			}

			if (fluidsAffected) {
				TransactionCallback.onSuccess(t, () -> {
					cauldron.getBehaviour(SmartFluidTankBehaviour.INPUT)
						.forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
					cauldron.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
						.forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
				});
			}

			if (recipe instanceof CauldronRecipe cauldronRecipe) {
				recipeOutputFluids.addAll(cauldronRecipe.getFluidResults());
			}

			if (!test)
				t.commit();
			return true;
		}
	}


	@Override
	protected int getMaxInputCount() {
		return 2;
	}

	@Override
	protected int getMaxOutputCount() {
		return 0;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 1;
	}

	@Override
	protected boolean canRequireHeat() {
		return true;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		return false;
	}
}
