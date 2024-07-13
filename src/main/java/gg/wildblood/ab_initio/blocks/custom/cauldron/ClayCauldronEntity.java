package gg.wildblood.ab_initio.blocks.custom.cauldron;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveBlockEntity;
import gg.wildblood.ab_initio.blocks.custom.sieve.SievingRecipe;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ClayCauldronEntity extends SmartBlockEntity implements IHaveGoggleInformation, SidedStorageBlockEntity {
	// Input Inventory -> Storage for Items
	public ItemStackHandlerContainer inputInv;
	// Output Inventory -> Output for Fluids
	protected SmartFluidTankBehaviour outputTank;

	private boolean contentsChanged;

	public int timer;

	private final ClayCauldronEntity.CauldronInventoryHandler capability;

	// Idea -> Cobble + Soulfire = Molten Soulrockfluid -> Molten Soulrockfluid + Lava World Interaction -> Netherrack ? -> Hammering -> Nethergravel
	public ClayCauldronEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		contentsChanged = true;
		inputInv = new ItemStackHandlerContainer(1);
		outputTank = SmartFluidTankBehaviour.single(this, 2000);
		capability = new ClayCauldronEntity.CauldronInventoryHandler();
	}

	@Override
	public void tick() {
		super.tick();

		//if (getHeatLevel() == 0) Get Heat
			//return;
		// Check if we have space in output Tank

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


		sendData();
	}

	private void process() {
	}

	private void spawnParticles() {
		// TODO: Implement
	}

	private int getProcessingSpeed() {
		return 2; // TODO: Implement Heat Level
	}


	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		outputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 2, FluidConstants.BUCKET, true)
			.whenFluidUpdates(() -> contentsChanged = true)
			.forbidInsertion();

		behaviours.add(outputTank);
	}

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(world, pos, inputInv);
	}

	@Override
	protected void read(NbtCompound compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		inputInv.deserializeNBT(compound.getCompound("InputInventory"));
	}

	@Override
	protected void write(NbtCompound compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("InputInventory", inputInv.serializeNBT());
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
		return capability;
	}

	private boolean canProcess(ItemStack stack) {
		return true;
		/**
		ItemStackHandlerContainer tester = new ItemStackHandlerContainer(1);
		tester.setStackInSlot(0, stack);

		if (lastRecipe != null && lastRecipe.matches(tester, world))
			return true;
		return ModRecipeTypes.SIEVING.find(tester, world)
			.isPresent();*/
	}

	private class CauldronInventoryHandler extends CombinedStorage<ItemVariant, ItemStackHandler> {

		public CauldronInventoryHandler() {
			super(List.of(inputInv));
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (canProcess(resource.toStack()))
				return inputInv.insert(resource, maxAmount, transaction);
			return 0;
		}
	}

}
