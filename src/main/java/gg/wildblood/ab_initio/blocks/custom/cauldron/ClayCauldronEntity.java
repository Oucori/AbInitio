package gg.wildblood.ab_initio.blocks.custom.cauldron;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.simibubi.create.infrastructure.config.AllConfigs;
import gg.wildblood.ab_initio.AbInitio;
import gg.wildblood.ab_initio.blocks.ModRecipeTypes;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import io.github.fabricators_of_create.porting_lib.util.FluidTextUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidUnit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ClayCauldronEntity extends SmartBlockEntity implements IHaveGoggleInformation, SidedStorageBlockEntity {
	private final ClayCauldronEntity.CauldronInventoryHandler itemCapability;
	protected Storage<FluidVariant> fluidCapability;
	protected SmartFluidTankBehaviour outputTank;
	public SmartInventory inputInv;
	private CauldronRecipe lastRecipe;
	private boolean contentsChanged;
	private boolean needsUpdate;
	public int timer;

	// Idea -> Cobble + Soulfire = Molten Soulrockfluid -> Molten Soulrockfluid + Lava World Interaction -> Netherrack ? -> Hammering -> Nethergravel
	public ClayCauldronEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		contentsChanged = true;
		inputInv = new SmartInventory(2, this).withMaxStackSize(8);
		itemCapability = new ClayCauldronEntity.CauldronInventoryHandler();
	}

	public static BlazeBurnerBlock.HeatLevel getHeatLevelOf(BlockState state) {
		if (state.contains(BlazeBurnerBlock.HEAT_LEVEL))
			return state.get(BlazeBurnerBlock.HEAT_LEVEL);
		return AllTags.AllBlockTags.PASSIVE_BOILER_HEATERS.matches(state) && BlockHelper.isNotUnheated(state) ? BlazeBurnerBlock.HeatLevel.SMOULDERING : BlazeBurnerBlock.HeatLevel.NONE;
	}

	public void notifyChangeOfContents() {
		contentsChanged = true;
	}

	@Override
	public void tick() {
		super.tick();

		if (needsUpdate) {
			needsUpdate = false;
			super.notifyUpdate();
		}


		try (Transaction t = TransferUtil.getTransaction()) {
			fluidCapability.insert(FluidVariant.of(Fluids.LAVA), 1, t);
			t.commit();
		} catch (Throwable e) {
			AbInitio.LOGGER.error("Error inserting fluid", e);
		}

		//AbInitio.LOGGER.info("{} {} mb", outputTank.getPrimaryTank().getTank().getFluid().getDisplayName(), outputTank.getPrimaryTank().getTank().getFluidAmount());

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

	public boolean isEmpty() {
		return inputInv.isEmpty() && outputTank.isEmpty();
	}

	private void process() {
	}

	@Override
	public void notifyUpdate() {
		this.needsUpdate = true;
	}

	private void spawnParticles() {
		// TODO: Implement
	}

	@Override
	public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		Lang.translate("gui.goggles.basin_contents")
			.forGoggles(tooltip);

		boolean isEmpty = true;


		for (int i = 0; i < inputInv.getSlotCount(); i++) {
			ItemStack stackInSlot = inputInv.getStackInSlot(i);
			if (stackInSlot.isEmpty())
				continue;
			Lang.text("")
				.add(Components.translatable(stackInSlot.getTranslationKey())
					.formatted(Formatting.GRAY))
				.add(Lang.text(" x" + stackInSlot.getCount())
					.style(Formatting.GREEN))
				.forGoggles(tooltip, 1);
			isEmpty = false;
		}


		FluidUnit unit = AllConfigs.client().fluidUnitType.get();
		LangBuilder unitSuffix = Lang.translate(unit.getTranslationKey());
		boolean simplify = AllConfigs.client().simplifyFluidUnit.get();

		for (SmartFluidTankBehaviour.TankSegment tank : outputTank.getTanks()) {
			FluidStack fluidStack = tank.getTank().getFluid();
			if (fluidStack.isEmpty())
				continue;
			Lang.text("")
				.add(Lang.fluidName(fluidStack)
					.add(Lang.text(" "))
					.style(Formatting.GRAY)
					.add(Lang.text(FluidTextUtil.getUnicodeMillibuckets(fluidStack.getAmount(), unit, simplify))
						.add(unitSuffix)
						.style(Formatting.BLUE)))
				.forGoggles(tooltip, 1);
			isEmpty = false;
		}

		if (isEmpty)
			tooltip.remove(0);

		return true;
	}

	private int getProcessingSpeed() {
		return 2; // TODO: Implement Heat Level
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));

		outputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 1, FluidConstants.BUCKET, true)
			.whenFluidUpdates(() -> contentsChanged = true)
			.forbidInsertion();

		behaviours.add(outputTank);

		fluidCapability = outputTank.getCapability();
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

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(world, pos, inputInv);
	}

	@Nullable
	@Override
	public Storage<FluidVariant> getFluidStorage(@Nullable Direction face) {
		return fluidCapability;
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
		return itemCapability;
	}

	private boolean canProcess(ItemStack stack) {
		if(world == null) return false;

		SmartInventory tester = new SmartInventory(1, this);
		tester.setStackInSlot(0, stack);

		if (lastRecipe != null && lastRecipe.matches(tester, world))
			return true;
		return ModRecipeTypes.CAULDRON.find(tester, world)
			.isPresent();
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
