package net.zephyrology.dragontrial.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.zephyrology.dragontrial.entity.ModEntities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Predicate;

public class DragonEntity extends TameableEntity implements GeoEntity {

    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private net.minecraft.entity.ai.goal.TemptGoal temptGoal;


    public DragonEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 2.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f);


    }

    @Override
    protected void initGoals() {
        this.temptGoal = new DragonEntity.TemptGoal(this, 1, stack -> stack.isIn(ItemTags.CAT_FOOD), false);
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, this.temptGoal);
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 1.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));

    }

    @Override
    public DragonEntity createChild(ServerWorld world, PassiveEntity entity) {
        return ModEntities.DRAGON.create(world);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(software.bernie.geckolib.animation.AnimationState<GeoAnimatable> geoAnimatableAnimationState) {

        if(geoAnimatableAnimationState.isMoving()){
            geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("animation.walk"));
            return PlayState.CONTINUE;
        }

        geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("animation.idle"));
        return PlayState.CONTINUE;
    }

    static class TemptGoal extends net.minecraft.entity.ai.goal.TemptGoal {
        @Nullable
        private PlayerEntity player;
        private final DragonEntity dragon;

        public TemptGoal(DragonEntity dragon, double speed, Predicate<ItemStack> foodPredicate, boolean canBeScared) {
            super(dragon, speed, foodPredicate, canBeScared);
            this.dragon = dragon;
        }
        }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

}
