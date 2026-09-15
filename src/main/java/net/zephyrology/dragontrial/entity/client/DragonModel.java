package net.zephyrology.dragontrial.entity.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.zephyrology.dragontrial.DragonTrial1;
import net.zephyrology.dragontrial.entity.custom.DragonEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class DragonModel extends GeoModel<DragonEntity> {
    @Override
    public Identifier getModelResource(DragonEntity animatable) {
        return Identifier.of(DragonTrial1.MOD_ID, "geo/dragonbaby.geo.json");
    }

    @Override
    public Identifier getTextureResource(DragonEntity animatable) {
        return Identifier.of(DragonTrial1.MOD_ID, "textures/entity/jibble-example-texture.png");
    }

    @Override
    public Identifier getAnimationResource(DragonEntity animatable) {
        return Identifier.of(DragonTrial1.MOD_ID, "animations/dragonbaby.animation.json");
    }

    @Override
    public void setCustomAnimations(DragonEntity animatable, long instanceId, AnimationState<DragonEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
        }
    }

}
