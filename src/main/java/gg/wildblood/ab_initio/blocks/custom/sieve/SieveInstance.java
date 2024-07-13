package gg.wildblood.ab_initio.blocks.custom.sieve;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;

public class SieveInstance extends SingleRotatingInstance<SieveBlockEntity> {
	public SieveInstance(MaterialManager materialManager, SieveBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(shaft());
	}
}
