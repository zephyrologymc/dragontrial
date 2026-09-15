package net.zephyrology.dragontrial.entity.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.zephyrology.dragontrial.DragonTrial1;
import net.zephyrology.dragontrial.entity.custom.DragonEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DragonRenderer extends GeoEntityRenderer<DragonEntity> {

    public DragonRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DragonModel());
    }



    @Override
    public Identifier getTextureLocation(DragonEntity animatable) {
        return Identifier.of(DragonTrial1.MOD_ID, "textures/entity/jibble-example-texture.png");
    }



    @Override
    public void render(DragonEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

}
