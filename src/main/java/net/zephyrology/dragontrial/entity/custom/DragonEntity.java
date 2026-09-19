package net.zephyrology.dragontrial.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.zephyrology.dragontrial.entity.ModEntities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;

public class DragonEntity extends TameableEntity implements GeoEntity {

    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private net.minecraft.entity.ai.goal.TemptGoal temptGoal;
    private WanderAroundFarGoal wanderAround;
    private static final net.minecraft.entity.data.TrackedData<Boolean> SLEEPING_ON_BED =
            net.minecraft.entity.data.DataTracker.registerData(DragonEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.BOOLEAN);




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
        this.goalSelector.add(2, new TameableEntity.TameableEscapeDangerGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.add(3, new SitGoal(this));
        this.goalSelector.add(4, new DragonEntity.SleepWithOwnerGoal(this));
        this.goalSelector.add(5, new FollowOwnerGoal(this, 0.85, 3.0F, 1.0F));
        this.goalSelector.add(6, this.temptGoal);
        this.goalSelector.add(7, new GoToBedAndSleepGoal(this, 1.1, 8));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F));
        this.goalSelector.add(9, new LookAroundGoal(this));

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



                if (this.isZoomiesItem(itemStack) && !this.isSitting() && this.isOwner(player) && this.wanderAround!=null){
                    this.eat(player, hand, itemStack);

                    int durationInTicks = 10 * 20;
                    int amplifier = 3;

                    this.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SPEED,
                            durationInTicks,
                            amplifier));

                    return ActionResult.success(this.getWorld().isClient());
                }


                if (this.isWanderItem(itemStack) && !this.isSitting() && this.isOwner(player)){
                    this.eat(player, hand, itemStack);

                    if (this.wanderAround == null){
                       this.wanderAround = new WanderAroundFarGoal(this, 1.0);
                       this.goalSelector.add(2, this.wanderAround);
                        player.sendMessage(Text.literal("Wander Mode ON"), true);
                    } else {
                        this.goalSelector.remove(this.wanderAround);
                        this.wanderAround = null;
                        player.sendMessage(Text.literal("Wander Mode OFF"), true);
                    }




                    return ActionResult.success(this.getWorld().isClient());
                }



                if (hand == Hand.MAIN_HAND) {
                    if (!this.getWorld().isClient()) {
                        boolean sit = !this.isSitting();
                        this.setInSittingPose(sit);
                        this.setSitting(sit);
                        this.navigation.stop();
                        this.setTarget(null);
                    }
                    return ActionResult.success(this.getWorld().isClient());
                }


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
            this.setInSittingPose(true);
            this.setSitting(true);
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
        } else {
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
        }


    }



    private PlayState predicate(software.bernie.geckolib.animation.AnimationState<GeoAnimatable> geoAnimatableAnimationState) {

        if(this.isSleepingOnBed()){
            geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("animation.sleep"));
            return PlayState.CONTINUE;
        }


        if(this.isInSittingPose()){
            geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.sit", Animation.LoopType.PLAY_ONCE).thenLoop("animation.seated"));
            return PlayState.CONTINUE;
        }


        if(geoAnimatableAnimationState.isMoving()){
            geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("animation.walk"));
            return PlayState.CONTINUE;
        }

        geoAnimatableAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("animation.idle"));
        return PlayState.CONTINUE;
    }

    public boolean isSleepingOnBed() {
        return this.dataTracker.get(SLEEPING_ON_BED);
    }

    public void setSleepingOnBed(boolean sleeping) {
        this.dataTracker.set(SLEEPING_ON_BED, sleeping);
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SLEEPING_ON_BED, false);
    }


    static class SleepWithOwnerGoal extends Goal {
        private final DragonEntity dragon;
        @Nullable
        private PlayerEntity owner;
        @Nullable
        private BlockPos bedPos;
        private int ticksOnBed;

        public SleepWithOwnerGoal(DragonEntity dragon) {
            this.dragon = dragon;
        }

        @Override
        public boolean canStart() {
            if (!this.dragon.isTamed()) {
                return false;
            }

            if (this.dragon.isSitting()) {
                return false;
            }

            LivingEntity livingEntity = this.dragon.getOwner();
            if (livingEntity instanceof PlayerEntity) {
                this.owner = (PlayerEntity)livingEntity;
                if (!livingEntity.isSleeping()) {
                    return false;
                }

                if (this.dragon.squaredDistanceTo(this.owner) > 100.0) {
                    return false;
                }

                BlockPos blockPos = this.owner.getSleepingPosition().orElse(null);
                if (blockPos != null && this.canSleepOn(this.dragon.getWorld(), blockPos)) {
                    this.bedPos = blockPos;
                    return true;
                }
            }

            return false;
        }


        @Override
        public boolean shouldContinue() {
            return this.dragon.isTamed() && !this.dragon.isSitting() && this.owner != null && this.owner.isSleeping() && this.bedPos != null && this.canSleepOn(this.dragon.getWorld(), this.bedPos);
        }

        @Override
        public void start() {
            if (this.bedPos != null) {
                this.dragon.setInSittingPose(false);
                this.dragon.getNavigation().startMovingTo(this.bedPos.getX(), this.bedPos.getY(), this.bedPos.getZ(), 1.1F);
            }
        }

        @Override
        public void stop() {
            this.dragon.setSleepingOnBed(false);
            this.owner = null;
            this.bedPos = null;
            this.ticksOnBed = 0;
        }


        @Override
        public void tick() {
            if (this.owner == null || this.bedPos == null) return;

            if (this.dragon.getBlockPos().isWithinDistance(this.bedPos, 2.0)) {
                this.ticksOnBed++;
                if (this.ticksOnBed >= 10) {
                    this.dragon.setSleepingOnBed(true);
                    this.dragon.getNavigation().stop();
                } else {
                    this.dragon.getNavigation().startMovingTo(this.owner, 1.1);
                }
            } else {
                this.dragon.setSleepingOnBed(false);
                this.dragon.getNavigation().startMovingTo(this.owner, 1.1);
            }


        }

        private boolean canSleepOn(World world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            return blockState.isIn(BlockTags.BEDS) && blockState.get(net.minecraft.state.property.Properties.BED_PART) == net.minecraft.block.enums.BedPart.HEAD;
        }

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


    public class GoToBedAndSleepGoal extends MoveToTargetPosGoal {
        private final DragonEntity dragon;

        public GoToBedAndSleepGoal(DragonEntity dragon, double speed, int range) {
            super(dragon, speed, range, 6);
            this.dragon = dragon;
            this.lowestY = -2;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return this.dragon.isTamed() && !this.dragon.isSitting() && !this.dragon.isSleepingOnBed() && super.canStart();
        }

        @Override
        public void start() {
            super.start();
            this.dragon.setInSittingPose(false);
        }

        @Override
        protected int getInterval(PathAwareEntity mob) {
            return 40;
        }

        @Override
        public void stop() {
            super.stop();
            this.dragon.setSleepingOnBed(false);
        }

        @Override
        public void tick() {
            super.tick();
            this.dragon.setInSittingPose(false);
            if (!this.hasReached()) {
                this.dragon.setSleepingOnBed(false);
            } else if (!this.dragon.isSleepingOnBed()) {
                this.dragon.setSleepingOnBed(true);
            }
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            return world.isAir(pos.up()) && world.getBlockState(pos).isIn(BlockTags.BEDS);
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

    public boolean isZoomiesItem(ItemStack stack){
        return stack.isOf(Items.SUGAR);
    }

    public boolean isWanderItem(ItemStack stack){
        return stack.isOf(Items.GOLD_NUGGET);
    }


}
