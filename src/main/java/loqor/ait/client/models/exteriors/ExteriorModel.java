package loqor.ait.client.models.exteriors;

import loqor.ait.core.blockentities.ExteriorBlockEntity;
import loqor.ait.core.entities.FallingTardisEntity;
import loqor.ait.core.entities.TardisRealEntity;
import loqor.ait.tardis.data.DoorData;
import loqor.ait.tardis.data.loyalty.Loyalty;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static loqor.ait.tardis.animation.ExteriorAnimation.*;

@SuppressWarnings("rawtypes")
public abstract class ExteriorModel extends SinglePartEntityModel {
	public static int MAX_TICK_COUNT = 2 * 20;

	public ExteriorModel() {
		this(RenderLayer::getEntityCutoutNoCull);
	}

	public ExteriorModel(Function<Identifier, RenderLayer> function) {
		super(function);
	}

	public void renderWithAnimations(ExteriorBlockEntity exterior, ModelPart root, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		if (exterior.tardis().isEmpty())
			return;

		if (exterior.tardis().get().getHandlers().getCloak().isEnabled()) {
			if (!(exterior.tardis().get().getHandlers().getLoyalties().get(MinecraftClient.getInstance().player).level() < Loyalty.Type.COMPANION.level)) {
				alpha = 0f;
				root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
				return;
			}
			if (isNearTardis(MinecraftClient.getInstance().player, exterior.tardis().get(), MAX_CLOAK_DISTANCE)) {
				alpha = 1f - (float) (distanceFromTardis(MinecraftClient.getInstance().player, exterior.tardis().get()) / MAX_CLOAK_DISTANCE);
				if (exterior.getAlpha() != 0.105f)
					alpha = alpha * exterior.getAlpha();
			} else {
				alpha = 0f;
			}
		}

		root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	public void renderFalling(FallingTardisEntity falling, ModelPart root, MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}

	public void renderRealWorld(TardisRealEntity realEntity, ModelPart root, MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		if (realEntity.getTardis() == null) return;

		root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}

	@Override
	public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}


	public abstract Animation getAnimationForDoorState(DoorData.DoorStateEnum state);
}
