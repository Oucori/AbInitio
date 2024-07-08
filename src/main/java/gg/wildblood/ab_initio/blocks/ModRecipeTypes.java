package gg.wildblood.ab_initio.blocks;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Lang;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.custom.composter.ComposterRecipe;
import gg.wildblood.ab_initio.blocks.custom.sieve.SievingRecipe;
import io.github.fabricators_of_create.porting_lib.util.ShapedRecipeUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public enum ModRecipeTypes implements IRecipeTypeInfo {

	SIEVING(SievingRecipe::new),
	COMPOSTING(ComposterRecipe::new);

	private final Identifier id;
	private final RecipeSerializer<?> serializerObject;
	@Nullable
	private final RecipeType<?> typeObject;
	private final Supplier<RecipeType<?>> type;

	ModRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
		String name = Lang.asId(name());
		id = AbInitio.asResource(name);
		serializerObject = Registry.register(Registries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		if (registerType) {
			typeObject = typeSupplier.get();
			Registry.register(Registries.RECIPE_TYPE, id, typeObject);
			type = typeSupplier;
		} else {
			typeObject = null;
			type = typeSupplier;
		}
	}

	ModRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
		String name = Lang.asId(name());
		id = AbInitio.asResource(name);
		serializerObject = Registry.register(Registries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		typeObject = simpleType(id);
		Registry.register(Registries.RECIPE_TYPE, id, typeObject);
		type = () -> typeObject;
	}

	ModRecipeTypes(ProcessingRecipeBuilder.ProcessingRecipeFactory<?> processingFactory) {
		this(() -> new ProcessingRecipeSerializer<>(processingFactory));
	}

	public static <T extends Recipe<?>> RecipeType<T> simpleType(Identifier id) {
		String stringId = id.toString();
		return new RecipeType<T>() {
			@Override
			public String toString() {
				return stringId;
			}
		};
	}

	public static void register() {
		ShapedRecipeUtil.setCraftingSize(9, 9);
		// fabric: just load the class
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializerObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeType<?>> T getType() {
		return (T) type.get();
	}

	public <C extends Inventory, T extends Recipe<C>> Optional<T> find(C inv, World world) {
		return world.getRecipeManager()
			.getFirstMatch(getType(), inv, world);
	}

	public static boolean shouldIgnoreInAutomation(Recipe<?> recipe) {
		RecipeSerializer<?> serializer = recipe.getSerializer();
		if (serializer != null && AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.matches(serializer))
			return true;
		return recipe.getId()
			.getPath()
			.endsWith("_manual_only");
	}
}
