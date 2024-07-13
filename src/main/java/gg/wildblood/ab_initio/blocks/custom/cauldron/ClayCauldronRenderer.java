package gg.wildblood.ab_initio.blocks.custom.cauldron;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.utility.VecHelper;
import gg.wildblood.ab_initio.blocks.custom.sieve.SieveBlockEntity;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;

import java.util.List;

public class ClayCauldronRenderer extends SmartBlockEntityRenderer<ClayCauldronEntity> {
	public ClayCauldronRenderer(BlockEntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ClayCauldronEntity cauldron, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
							  int light, int overlay) {
		super.renderSafe(cauldron, partialTicks, ms, buffer, light, overlay);

		renderInputInventory(cauldron, ms, buffer, light, overlay);
	}

	@SuppressWarnings("UnstableApiUsage")
	private void renderInputInventory(ClayCauldronEntity cauldron, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
		float baseHeigth = .125f;
		ms.push();

		BlockPos pos = cauldron.getPos();
		ms.translate(.5, .2f, .5);

		RandomGenerator r = RandomGenerator.createLegacy(pos.hashCode());
		Vec3d baseVector = new Vec3d(0, baseHeigth, 0);

		Storage<ItemVariant> inputInv = cauldron.inputInv;

		if (inputInv != null) {
			int itemCount = 0;
			List<ItemStack> stacks = TransferUtil.getAllItems(inputInv);
			itemCount = stacks.size();

			float anglePartition = 360f / itemCount;
			for (ItemStack stack : stacks) {

				ms.push();

				Vec3d itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Direction.Axis.Y);
				ms.translate(itemPosition.x -.15, itemPosition.y, itemPosition.z -.15);
				TransformStack.cast(ms)
					.rotateY(anglePartition * itemCount + 35)
					.rotateX(65);

				for (int i = 0; i <= stack.getCount() / 8; i++) {
					ms.push();

					Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, 1 / 8f);

					ms.translate(vec.x, vec.y, vec.z);
					renderItem(ms, buffer, light, overlay, stack);
					ms.pop();
				}
				ms.pop();

				itemCount--;
			}
		}
		ms.pop();
	}


	protected void renderItem(MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay, ItemStack stack) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.getItemRenderer()
			.renderItem(stack, ModelTransformationMode.GROUND, light, overlay, ms, buffer, mc.world, 0);
	}

	@Override
	public int getRenderDistance() {
		return 16;
	}
}
