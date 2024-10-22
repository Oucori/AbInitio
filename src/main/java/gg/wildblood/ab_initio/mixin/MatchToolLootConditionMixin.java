package gg.wildblood.ab_initio.mixin;

import gg.wildblood.ab_initio.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.item.ItemPredicate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MatchToolLootCondition.class)
public abstract class MatchToolLootConditionMixin implements LootCondition {

	@Final
	@Shadow
	ItemPredicate predicate;

	@Inject(method = "test", at = @At("RETURN"), cancellable = true)
	public void test(LootContext lootContext, CallbackInfoReturnable<Boolean> callbackInfo) {
		ItemStack itemStack = lootContext.get(LootContextParameters.TOOL);
		if(itemStack == null) return;

		if(itemStack.getItem() == ModItems.WOODEN_SHEARS) {
			callbackInfo.setReturnValue(this.predicate.test(Items.SHEARS.getDefaultStack()));
		}
	}
}
