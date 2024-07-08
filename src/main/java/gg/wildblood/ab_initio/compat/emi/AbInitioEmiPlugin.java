package gg.wildblood.ab_initio.compat.emi;

import com.simibubi.create.compat.recipeViewerCommon.HiddenItems;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.ModBlocks;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import gg.wildblood.ab_initio.compat.emi.recipes.SievingEmiRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AbInitioEmiPlugin implements EmiPlugin {
	public static final Map<Identifier, EmiRecipeCategory> ALL = new LinkedHashMap<>();

	public static final EmiRecipeCategory
		SIEVING = register("sieving", EmiStack.of(ModBlocks.SIEVE_BLOCK.get()));

	@Override
	public void register(EmiRegistry registry) {
		AbInitio.LOGGER.warn("Registering EmiPlugin for Ab Initio");
		registry.removeEmiStacks(s -> {
			Object key = s.getKey();
			Item item = s.getItemStack().getItem();
			if (key instanceof TagDependentIngredientItem tagDependent && tagDependent.shouldHide())
				return true;
			if (HiddenItems.getHiddenPredicate().test(item))
				return true;
			return key instanceof VirtualFluid;
		});

		registry.addGenericExclusionArea((screen, consumer) -> {
			if (screen instanceof AbstractSimiContainerScreen<?> simi) {
				simi.getExtraAreas().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			}
		});

		ALL.forEach((id, category) -> registry.addCategory(category));

		registry.addWorkstation(SIEVING, EmiStack.of(ModBlocks.SIEVE_BLOCK.get()));

		RecipeManager manager = registry.getRecipeManager();

		addAll(registry, ModRecipeTypes.SIEVING, SievingEmiRecipe::new);
	}

	@SuppressWarnings("unchecked")
	private <T extends Recipe<?>> void addAll(EmiRegistry registry, ModRecipeTypes type, Function<T, EmiRecipe> constructor) {
		for (T recipe : (List<T>) registry.getRecipeManager().listAllOfType(type.getType())) {
			registry.addRecipe(constructor.apply(recipe));
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Recipe<?>> void addAll(EmiRegistry registry, ModRecipeTypes type, EmiRecipeCategory category,
											  BiFunction<EmiRecipeCategory, T, EmiRecipe> constructor) {
		for (T recipe : (List<T>) registry.getRecipeManager().listAllOfType(type.getType())) {
			registry.addRecipe(constructor.apply(category, recipe));
		}
	}

	public static boolean doInputsMatch(Recipe<?> a, Recipe<?> b) {
		if (!a.getIngredients().isEmpty() && !b.getIngredients().isEmpty()) {
			ItemStack[] matchingStacks = a.getIngredients().get(0).getMatchingStacks();
			if (matchingStacks.length != 0) {
				return b.getIngredients().get(0).test(matchingStacks[0]);
			}
		}
		return false;
	}

	private static EmiRecipeCategory register(String name, EmiRenderable icon) {
		Identifier id = AbInitio.asResource(name);
		EmiRecipeCategory category = new EmiRecipeCategory(id, icon);
		ALL.put(id, category);
		return category;
	}

}
