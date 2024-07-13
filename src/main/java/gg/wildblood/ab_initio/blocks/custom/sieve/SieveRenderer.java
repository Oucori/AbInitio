package gg.wildblood.ab_initio.blocks.custom.sieve;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.utility.VecHelper;
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

public class SieveRenderer extends SmartBlockEntityRenderer<SieveBlockEntity> {
	public SieveRenderer(BlockEntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(SieveBlockEntity sieve, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
							  int light, int overlay) {
		super.renderSafe(sieve, partialTicks, ms, buffer, light, overlay);

		renderOutputInventory(sieve, ms, buffer, light, overlay);

		renderInputInventory(sieve, ms, buffer, light, overlay);

	}

	private void renderInputInventory(SieveBlockEntity sieve, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
		float baseHeigthTop = .7f;

		if(sieve.visualizedInputItem.isEmpty())
			return;

		float progress = sieve.timer / 100f + .5f;

		ItemStack stack = sieve.visualizedInputItem;

		Vec3d baseVector = new Vec3d(.5, baseHeigthTop, .5);

		ms.push();

		TransformStack.cast(ms)
			.translate(baseVector)
			.scale(1.5f, progress, 1.5f);

		renderItem(ms, buffer, light, overlay, stack);

		ms.pop();
	}

	@SuppressWarnings("UnstableApiUsage")
	private void renderOutputInventory(SieveBlockEntity sieve, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
		float baseHeigth = .15f;
		ms.push();

		BlockPos pos = sieve.getPos();
		ms.translate(.5, .2f, .5);

		RandomGenerator r = RandomGenerator.createLegacy(pos.hashCode());
		Vec3d baseVector = new Vec3d(.125, baseHeigth, 0);

		Storage<ItemVariant> outputInv = sieve.outputInv;

		if (outputInv != null) {
			int itemCount = 0;
			List<ItemStack> stacks = TransferUtil.getAllItems(outputInv);
			itemCount = stacks.size();

			if (itemCount == 1)
				baseVector = new Vec3d(0, baseHeigth, 0);

			float anglePartition = 360f / itemCount;
			for (ItemStack stack : stacks) {

				ms.push();

				Vec3d itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Direction.Axis.Y);
				ms.translate(itemPosition.x, itemPosition.y, itemPosition.z);
				TransformStack.cast(ms)
					.rotateY(anglePartition * itemCount + 35)
					.rotateX(65);

				for (int i = 0; i <= stack.getCount() / 8; i++) {
					ms.push();

					Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, 1 / 16f);

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
