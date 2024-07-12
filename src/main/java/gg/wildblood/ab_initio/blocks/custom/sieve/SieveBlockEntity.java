package gg.wildblood.ab_initio.blocks.custom.sieve;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.ViewOnlyWrappedStorageView;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerSlot;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
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
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


@SuppressWarnings("UnstableApiUsage")
public class SieveBlockEntity extends KineticBlockEntity implements SidedStorageBlockEntity {
	public ItemStackHandlerContainer inputInv;
	public ItemStackHandler outputInv;
	private final SieveBlockEntity.SieveInventoryHandler capability;
	public int timer;
	private SievingRecipe lastRecipe;

	public ItemStack visualizedInputItem;
	public List<ItemStack> visualizedOutputItems;

	public static final int ANIMATION_TIME = 10;

	public SieveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inputInv = new ItemStackHandlerContainer(1);
		outputInv = new ItemStackHandler(9);
		capability = new SieveBlockEntity.SieveInventoryHandler();
		visualizedInputItem = ItemStack.EMPTY;
		visualizedOutputItems = Collections.synchronizedList(new ArrayList<>());;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		super.addBehaviours(behaviours);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
	}

	@Override
	public void tick() {
		super.tick();

		if (getSpeed() == 0)
			return;
		for (int i = 0; i < outputInv.getSlotCount(); i++)
			if (outputInv.getStackInSlot(i)
				.getCount() == outputInv.getSlotLimit(i))
				return;

		if (timer > 0) {
			timer -= getProcessingSpeed();

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

		Optional<SievingRecipe> recipe = ModRecipeTypes.SIEVING.find(inputInv, world);
		recipe.ifPresent(sievingRecipe -> visualizedInputItem = sievingRecipe.getIngredients().get(0).getMatchingStacks()[0]);

		if (lastRecipe == null || !lastRecipe.matches(inputInv, world)) {
			if (recipe.isEmpty()) {
				timer = 100;
				sendData();
			} else {
				lastRecipe = recipe.get();
				timer = lastRecipe.getProcessingDuration();
				sendData();
				markDirty();
			}
			return;
		}

		timer = lastRecipe.getProcessingDuration();
		sendData();
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

	private void process() {
		if (lastRecipe == null || !lastRecipe.matches(inputInv, world)) {
			Optional<SievingRecipe> recipe = ModRecipeTypes.SIEVING.find(inputInv, world);
			if (!recipe.isPresent())
				return;
			lastRecipe = recipe.get();
		}

		try (Transaction t = TransferUtil.getTransaction()) {
			ItemStackHandlerSlot slot = inputInv.getSlot(0);
			slot.extract(slot.getResource(), 1, t);
			lastRecipe.rollResults().forEach(stack -> outputInv.insert(ItemVariant.of(stack), stack.getCount(), t));
			t.commit();
		}

		visualizedInputItem = ItemStack.EMPTY;

		sendData();
		markDirty();
	}

	public void spawnParticles() {
		ItemStack stackInSlot = inputInv.getStackInSlot(0);
		if (stackInSlot.isEmpty())
			return;

		ItemStackParticleEffect data = new ItemStackParticleEffect(ParticleTypes.ITEM, stackInSlot);
		float angle = world.random.nextFloat() * 360;
		Vec3d offset = new Vec3d(0, .4, .2);
		offset = VecHelper.rotate(offset, angle, Direction.Axis.Y);
		Vec3d target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Direction.Axis.Y);

		Vec3d center = offset.add(VecHelper.getCenterOf(pos));
		target = VecHelper.offsetRandomly(target.subtract(offset), world.random, 1 / 128f);
		world.addParticle(data, center.x, center.y, center.z, target.x, target.y, target.z);
	}

	@Override
	public void write(NbtCompound compound, boolean clientPacket) {
		compound.putInt("Timer", timer);
		compound.put("InputInventory", inputInv.serializeNBT());
		compound.put("OutputInventory", outputInv.serializeNBT());
		compound.put("VisualizedInputItem", visualizedInputItem.serializeNBT());
		compound.put("VisualizedOutputItems", NBTHelper.writeCompoundList(visualizedOutputItems, NBTSerializer::serializeNBTCompound));
		visualizedOutputItems.clear();
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(NbtCompound compound, boolean clientPacket) {
		timer = compound.getInt("Timer");
		inputInv.deserializeNBT(compound.getCompound("InputInventory"));
		outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
		visualizedInputItem = ItemStack.fromNbt(compound.getCompound("VisualizedInputItem"));
		NBTHelper.iterateCompoundList(compound.getList("VisualizedOutputItems", NbtElement.COMPOUND_TYPE),
			c -> visualizedOutputItems.add(ItemStack.fromNbt(c)));
		super.read(compound, clientPacket);
	}

	public int getProcessingSpeed() {
		return MathHelper.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
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
		return ModRecipeTypes.SIEVING.find(tester, world)
			.isPresent();
	}

	private class SieveInventoryHandler extends CombinedStorage<ItemVariant, ItemStackHandler> {

		public SieveInventoryHandler() {
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
			return new SieveInventoryHandlerIterator();
		}

		private class SieveInventoryHandlerIterator implements Iterator<StorageView<ItemVariant>> {
			private boolean output = true;
			private Iterator<StorageView<ItemVariant>> wrapped;

			public SieveInventoryHandlerIterator() {
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
