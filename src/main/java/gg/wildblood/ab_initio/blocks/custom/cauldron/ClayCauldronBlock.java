package gg.wildblood.ab_initio.blocks.custom.cauldron;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.ModBlocks;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveBlockEntity;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ClayCauldronBlock extends Block implements IBE<ClayCauldronEntity>, IWrenchable {
	public ClayCauldronBlock(Settings settings) {
		super(settings.nonOpaque());
	}

	@Override
	public void onEntityLand(BlockView worldIn, Entity entityIn) {
		super.onEntityLand(worldIn, entityIn);

		if (entityIn.getWorld().isClient)
			return;
		if (!(entityIn instanceof ItemEntity itemEntity))
			return;
		if (!entityIn.isAlive())
			return;

		ClayCauldronEntity cauldron = null;
		for (BlockPos pos : Iterate.hereAndBelow(entityIn.getBlockPos()))
			if (cauldron == null)
				cauldron = getBlockEntity(worldIn, pos);

		if (cauldron == null)
			return;

		Storage<ItemVariant> handler = cauldron.getItemStorage(null);
		if (handler == null)
			return;

		try (Transaction t = TransferUtil.getTransaction()) {
			ItemStack inEntity = itemEntity.getStack();
			long inserted = handler.insert(ItemVariant.of(inEntity), inEntity.getCount(), t);
			if (inserted == inEntity.getCount())
				itemEntity.discard();
			else itemEntity.setStack(ItemHandlerHelper.copyStackWithSize(inEntity, (int) (inEntity.getCount() - inserted)));
			t.commit();
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack heldItem = player.getStackInHand(hand);

		return onBlockEntityUse(world, pos, be -> {
			if (!heldItem.isEmpty()) {
				Direction direction = hit.getSide();
				if (FluidHelper.tryEmptyItemIntoBE(world, player, hand, heldItem, be, direction))
					return ActionResult.SUCCESS;
				if (FluidHelper.tryFillItemFromBE(world, player, hand, heldItem, be, direction))
					return ActionResult.SUCCESS;

				if (GenericItemEmptying.canItemBeEmptied(world, heldItem)
					|| GenericItemFilling.canItemBeFilled(world, heldItem))
					return ActionResult.SUCCESS;
				return ActionResult.PASS;
			}

			Storage<ItemVariant> inv = be.getItemStorage(null);
			if (inv == null) return ActionResult.PASS;
			List<ItemStack> extracted = TransferUtil.extractAllAsStacks(inv);
			if (!extracted.isEmpty()) {
				extracted.forEach(s -> player.getInventory().offerOrDrop(s));
				world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f,
					1f + Create.RANDOM.nextFloat());
			}
			//be.onEmptied();
			return ActionResult.SUCCESS;
		});
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(0,0,0,.75, .95, .75).offset(.125, 0, .125);
	}

	@Override
	public Class<ClayCauldronEntity> getBlockEntityClass() {
		return ClayCauldronEntity.class;
	}

	@Override
	public BlockEntityType<? extends ClayCauldronEntity> getBlockEntityType() {
		return ModBlocks.CLAY_CAULDRON_ENTITY.get();
	}
}
