package net.zephyrology.dragontrial.entity.custom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.zephyrology.dragontrial.entity.ModEntities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;
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
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);


    }

    @Override
    protected void initGoals() {
        this.temptGoal = new DragonEntity.TemptGoal(this, 1, stack -> stack.isIn(ItemTags.CAT_FOOD), false);
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new TameableEntity.TameableEscapeDangerGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.add(3, new SitGoal(this));
        this.goalSelector.add(5, new FollowOwnerGoal(this, 1.0, 3.0F, 2.0F));
        this.goalSelector.add(6, this.temptGoal);
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 1.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));

    }



    @Override
    public DragonEntity createChild(ServerWorld world, PassiveEntity entity) {
        DragonEntity dragonEntity = ModEntities.DRAGON.create(world);
        if (this.isTamed()) {
            dragonEntity.setOwnerUuid(this.getOwnerUuid());
            dragonEntity.setTamed(true, true);
            }
        return dragonEntity;
    }



    @Override
    public void setTamed(boolean tamed, boolean updateAttributes) {
        super.setTamed(tamed, updateAttributes);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (this.isTamed()) {
            if (this.isOwner(player)) {
                if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                    if (!this.getWorld().isClient()) {
                        this.eat(player, hand, itemStack);
                        FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                        this.heal(foodComponent != null ? foodComponent.nutrition() : 1.0F);
                    }

                    return ActionResult.success(this.getWorld().isClient());
                }

                ActionResult actionResult = super.interactMob(player, hand);
                if (!actionResult.isAccepted()) {
                    this.setSitting(!this.isSitting());
                    return ActionResult.success(this.getWorld().isClient());
                }

                return actionResult;
            }
        } else if (this.isBreedingItem(itemStack)) {
            if (!this.getWorld().isClient()) {
                this.eat(player, hand, itemStack);
                this.tryTame(player);
                this.setPersistent();
            }

            return ActionResult.success(this.getWorld().isClient());
        }

        ActionResult actionResult = super.interactMob(player, hand);
        if (actionResult.isAccepted()) {
            this.setPersistent();
        }

        return actionResult;
    }

    @Nullable
    @Override
    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }



    private void tryTame(PlayerEntity player) {
        if (this.random.nextInt(3) == 0) {
            this.setOwner(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setSitting(true);
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
        } else {
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
        }


    }



    private PlayState predicate(software.bernie.geckolib.animation.AnimationState<GeoAnimatable> geoAnimatableAnimationState) {

        if(geoAnimatableAnimationState.isMoving()){
            geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("animation.walk"));
            return PlayState.CONTINUE;
        }

        if(this.isSitting()){
            geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.sit", Animation.LoopType.PLAY_ONCE).thenLoop("animation.seated"));
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
        return stack.isIn(ItemTags.CAT_FOOD);
    }

}
