package gg.wildblood.ab_initio.blocks.custom.sieve;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;
import gg.wildblood.ab_initio.blocks.ModBlocks;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;


@SuppressWarnings("UnstableApiUsage")
public class SieveBlock extends HorizontalKineticBlock implements IBE<SieveBlockEntity> {
	public SieveBlock(Settings settings) {
		super(settings.nonOpaque());
	}

	// #Todo: Add Better Block Model
	// #Todo: Animation for Sieving... ?

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		Direction prefferedSide = getPreferredHorizontalFacing(context);
		if (prefferedSide != null)
			return getDefaultState().with(HORIZONTAL_FACING, prefferedSide);
		return super.getPlacementState(context);
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(HORIZONTAL_FACING)
			.getAxis();
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

		SieveBlockEntity Sieve = null;
		for (BlockPos pos : Iterate.hereAndBelow(entityIn.getBlockPos()))
			if (Sieve == null)
				Sieve = getBlockEntity(worldIn, pos);

		if (Sieve == null)
			return;

		Storage<ItemVariant> handler = Sieve.getItemStorage(null);
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
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
							  BlockHitResult hit) {
		if (!player.getStackInHand(handIn)
			.isEmpty())
			return ActionResult.PASS;
		if (worldIn.isClient)
			return ActionResult.SUCCESS;

		withBlockEntityDo(worldIn, pos, sieve -> {
			boolean emptyOutput = true;
			ItemStackHandler inv = sieve.outputInv;
			for (int slot = 0; slot < inv.getSlotCount(); slot++) {
				ItemStack stackInSlot = inv.getStackInSlot(slot);
				if (!stackInSlot.isEmpty())
					emptyOutput = false;
				player.getInventory()
					.offerOrDrop(stackInSlot);
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}

			if (emptyOutput) {
				inv = sieve.inputInv;
				for (int slot = 0; slot < inv.getSlotCount(); slot++) {
					player.getInventory()
						.offerOrDrop(inv.getStackInSlot(slot));
					inv.setStackInSlot(slot, ItemStack.EMPTY);
				}
			}

			sieve.markDirty();
			sieve.sendData();
		});

		return ActionResult.SUCCESS;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(0f, 0f, 0f, 1f, .8f, 1f);
	}

	@Override
	public Class<SieveBlockEntity> getBlockEntityClass() {
		return SieveBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends SieveBlockEntity> getBlockEntityType() {
		return ModBlocks.SIEVE_BLOCK_ENTITY.get();
	}

}
