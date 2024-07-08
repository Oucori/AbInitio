package gg.wildblood.ab_initio.blocks.custom.composter;

import com.simibubi.create.foundation.utility.Iterate;
import gg.wildblood.ab_initio.blocks.ModBlocks;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import com.simibubi.create.foundation.block.IBE;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ComposterBlock extends Block implements BlockEntityProvider, IBE<ComposterBlockEntity> {

	public ComposterBlock(Settings settings) {
		super(settings);
	}

	@Override
	public Class<ComposterBlockEntity> getBlockEntityClass() {
		return ComposterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ComposterBlockEntity> getBlockEntityType() {
		return ModBlocks.COMPOSTER_BLOCK_ENTITY.get();
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ComposterBlockEntity(getBlockEntityType(), pos, state);
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

		ComposterBlockEntity composter = null;
		for (BlockPos pos : Iterate.hereAndBelow(entityIn.getBlockPos()))
			if (composter == null)
				composter = getBlockEntity(worldIn, pos);

		if (composter == null)
			return;

		Storage<ItemVariant> handler = composter.getItemStorage(null);
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

}
