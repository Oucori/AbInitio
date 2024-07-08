package gg.wildblood.ab_initio.blocks.custom.composter;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.ViewOnlyWrappedStorageView;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerSlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ComposterBlockEntity extends SmartBlockEntity implements SidedStorageBlockEntity {
	public ItemStackHandlerContainer inputInv;
	public ItemStackHandler outputInv;
	private final ComposterBlockEntity.ComposterInventoryHandler capability;
	private int timer;
	private ComposterRecipe lastRecipe;

	public ComposterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inputInv = new ItemStackHandlerContainer(1);
		outputInv = new ItemStackHandler(1);
		capability = new ComposterBlockEntity.ComposterInventoryHandler();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
	}

	@Override
	public void tick() {
		super.tick();

		for (int i = 0; i < outputInv.getSlotCount(); i++)
			if (outputInv.getStackInSlot(i)
				.getCount() == outputInv.getSlotLimit(i))
				return;

		if (timer > 0) {
			timer -= 5;

			if (world.isClient) {
				spawnParticles();
				return;
			}
			if (timer <= 0)
				process();
			return;
		}

		if (inputInv.getStackInSlot(0).isEmpty())
			return;


		if (lastRecipe != null && inputInv.getStackInSlot(0).getCount() < lastRecipe.getInputCount())
			return;

		if (lastRecipe == null || !lastRecipe.matches(inputInv, world)) {
			Optional<ComposterRecipe> recipe = ModRecipeTypes.COMPOSTING.find(inputInv, world);
			if (!recipe.isPresent()) {
				timer = 100;
				sendData();
			} else {
				lastRecipe = recipe.get();
				timer = lastRecipe.getProcessingDuration();
				sendData();
			}
			return;
		}

		timer = lastRecipe.getProcessingDuration();
		sendData();
	}

	public void spawnParticles() {
		ItemStack stackInSlot = inputInv.getStackInSlot(0);
		if (stackInSlot.isEmpty())
			return;

		float angle = world.random.nextFloat() * 360;
		Vec3d offset = new Vec3d(0, 0.5f, 0.5f);
		offset = VecHelper.rotate(offset, angle, Direction.Axis.Y);
		Vec3d target = VecHelper.rotate(offset, 25, Direction.Axis.Y);

		Vec3d center = offset.add(VecHelper.getCenterOf(pos));
		target = VecHelper.offsetRandomly(target.subtract(offset), world.random, 1 / 128f);
		world.addParticle(ParticleTypes.HAPPY_VILLAGER, center.x, center.y, center.z, target.x, target.y, target.z);
	}

	private void process() {
		if (lastRecipe == null || !lastRecipe.matches(inputInv, world)) {
			Optional<ComposterRecipe> recipe = ModRecipeTypes.COMPOSTING.find(inputInv, world);
			if (!recipe.isPresent())
				return;
			lastRecipe = recipe.get();
		}

		try (Transaction t = TransferUtil.getTransaction()) {
			ItemStackHandlerSlot slot = inputInv.getSlot(0);
			slot.extract(slot.getResource(), lastRecipe.getInputCount(), t);
			lastRecipe.rollResults().forEach(stack -> outputInv.insert(ItemVariant.of(stack), stack.getCount(), t));
			t.commit();
		}

		sendData();
		markDirty();
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
		return capability;
	}

	private boolean canProcess(ItemStack stack) {
		ItemStackHandlerContainer tester = new ItemStackHandlerContainer(1);
		tester.setStackInSlot(0, stack);

		if (lastRecipe != null && lastRecipe.matches(tester, world))
			return true;
		return ModRecipeTypes.COMPOSTING.find(tester, world)
			.isPresent();
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(world, pos, inputInv);
		ItemHelper.dropContents(world, pos, outputInv);
	}

	@Override
	public void write(NbtCompound compound, boolean clientPacket) {
		compound.putInt("Timer", timer);
		compound.put("InputInventory", inputInv.serializeNBT());
		compound.put("OutputInventory", outputInv.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(NbtCompound compound, boolean clientPacket) {
		timer = compound.getInt("Timer");
		inputInv.deserializeNBT(compound.getCompound("InputInventory"));
		outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
		super.read(compound, clientPacket);
	}

	private class ComposterInventoryHandler extends CombinedStorage<ItemVariant, ItemStackHandler> {

		public ComposterInventoryHandler() {
			super(List.of(inputInv, outputInv));
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (canProcess(resource.toStack()))
				return inputInv.insert(resource, maxAmount, transaction);
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return outputInv.extract(resource, maxAmount, transaction);
		}

		@Override
		public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
			return new ComposterBlockEntity.ComposterInventoryHandler.ComposterInventoryHandlerIterator();
		}

		private class ComposterInventoryHandlerIterator implements Iterator<StorageView<ItemVariant>> {
			private boolean output = true;
			private Iterator<StorageView<ItemVariant>> wrapped;

			public ComposterInventoryHandlerIterator() {
				wrapped = outputInv.iterator();
			}

			@Override
			public boolean hasNext() {
				return wrapped.hasNext();
			}

			@Override
			public StorageView<ItemVariant> next() {
				StorageView<ItemVariant> view = wrapped.next();
				if (!output) view = new ViewOnlyWrappedStorageView<>(view);
				if (output && !hasNext()) {
					wrapped = inputInv.iterator();
					output = false;
				}
				return view;
			}
		}
	}

}
