package net.dumbcode.projectnublar.entity.dinosaur;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.DinoDietData;
import net.dumbcode.projectnublar.block.DinosaurFeederBlock;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.ai.FenceAwareNavigation;
import net.dumbcode.projectnublar.entity.ai.behaviour.actions.BreakFenceBehaviour;
import net.dumbcode.projectnublar.entity.ai.behaviour.actions.DinosaurLookAtTarget;
import net.dumbcode.projectnublar.entity.ai.behaviour.actions.GettingUpFromRestBehaviour;
import net.dumbcode.projectnublar.entity.ai.behaviour.needs.SoloHuntRoamBehaviour;
import net.dumbcode.projectnublar.entity.ai.behaviour.needs.SoloHuntingBehaviour;
import net.dumbcode.projectnublar.entity.ai.sensors.NearestFeederSensor;
import net.dumbcode.projectnublar.entity.ai.tasks.*;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.ai.sensors.NearestWaterSourceSensor;
import net.dumbcode.projectnublar.init.*;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.ActivityBuilder;
import net.tslat.smartbrainlib.api.internal.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.base.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.base.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowParent;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyBlocksSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestItemSensor;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import org.joml.Vector3fc;

import java.util.*;

import static net.dumbcode.projectnublar.util.DinoAnimationUtils.IS_ROARING_STATE;

public abstract class Dinosaur extends TamableAnimal implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur> {

    // 26.2: synced data accessors must be declared by the entity class itself — NeoForge rejects
    // definitions from foreign classes, so these live here and DinoNeedsUtils aliases them
    public static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> THIRST = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SOCIAL = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> AGGRESSION = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> FERTILITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DOMESTICITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> INTELLIGENCE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VISION = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> IMMUNITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> TAMING_SCORE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);

    // animation-state flags (aliased by DinoAnimationUtils)
    public static final EntityDataAccessor<Boolean> IS_EATING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_DRINKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_NESTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ROARING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SPEAKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SITTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RESTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RISING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ATTACKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_FLINCHING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_DEAD_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SWIMMING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RUNNING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> LOOKING_LEFT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> LOOKING_RIGHT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> TURNING_LEFT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> TURNING_RIGHT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);

    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.COMPOUND_TAG);

    public static EntityDataAccessor<Optional<UUID>> DINO_FAMILY_UUID = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.OPTIONAL_UUID);
    public static EntityDataAccessor<Optional<UUID>> DINO_MATE = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.OPTIONAL_UUID);
    public static EntityDataAccessor<Boolean> BABY_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> JUVENILE_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SUB_ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SHOULD_TICK_STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Vector3fc> DINOSAUR_HEAD_POS = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.VECTOR3);

    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected @Nullable DinoBehaviourData cachedBehaviourData;
    private DinoDietData dietData;
    public  DinosaurPart head;
    public @Nullable Vec3 headBonePos;
    private int staminaDrainTick;
    private int lastEatTime;
    private int daysSincelastAte;
    private int lastDrinkTime;
    private int daysSincelastDrink;
    private boolean eatenToday;
    private int socialDrainTick;
    private int breedingCoolDown = 500;
    private int flinchAnimLength;
    public DinosaurPart[] subEntities;
    private int cachedDayTime;
    public boolean isNewDay;
    List<Long> hungerSchedule = new ArrayList<>();
    List<Long> thirstSchedule = new ArrayList<>();

    public Dinosaur(EntityType<? extends Dinosaur> $$0, Level $$1, int flinchAnimLength) {
        super($$0, $$1);
        this.flinchAnimLength = flinchAnimLength;
    }
    //ANIMATION
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkController());
        controllers.add(new AnimationController<>("dino_controller", 0, this::animationPredicate));
        controllers.add(new AnimationController<>("dino_secondary_Controller", 0, this::animationPredicateAmbient));
    }
    private PlayState animationPredicateAmbient(AnimationTest<Dinosaur> state) {

    return PlayState.STOP;

    }

    //STOP THE GAME DESPAWNING AFTER DEATH
    @Override
    protected void tickDeath() {
    }

    public List<DinoLayer> getLayers() {
        return CommonClientClass.getDinoLayers(this.getType());
    };

    public boolean isRunning(){
        return this.entityData.get(DinoAnimationUtils.IS_RUNNING_STATE);
    }
    public boolean isFlinching(){
        return this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE);
    }

    public void resetParts(float scale) {}
    public void removeParts() {}
    public void updateParts(){}
    public void updatePart(@Nullable final DinosaurPart part, @NotNull final Dinosaur parent) {}

    private PlayState animationPredicate(AnimationTest<Dinosaur> state) {
        if(this.isDeadOrDying()){
            return state.setAndContinue(DinoAnimationUtils.DEAD_ANIM);
        }
        if(this.isFlinching()){
            return state.setAndContinue(DinoAnimationUtils.FLINCH_ANIM);
        }

        /// TO-DO: Fix running animation

        if(!this.isResting() && !this.shouldTickStamina() && (this.isRunning() || this.isSwimming() || state.isMoving())){
            this.entityData.set(SHOULD_TICK_STAMINA, true);
        } else if (this.shouldTickStamina() && !state.isMoving() && !this.isRunning() && !this.isSwimming()) {
            this.entityData.set(SHOULD_TICK_STAMINA, false);
        }
        if(this.isRoaring()){
            return state.setAndContinue(DinoAnimationUtils.ROARING_ANIM);
        }

        if(this.isAttacking()){
            return state.setAndContinue(DinoAnimationUtils.ATTACK_ANIM);
        }
        if(this.isSitting()){
            return state.setAndContinue(DinoAnimationUtils.REST_ANIM);
        }
        if(this.isRising()){
            return state.setAndContinue(DinoAnimationUtils.GETTING_UP_ANIM);
        }
        if(this.isResting()) {
            return state.setAndContinue(DinoAnimationUtils.REST_IDLE_ANIM);
        }
        if(this.isDrinking()) {
            return state.setAndContinue(DinoAnimationUtils.DRINKING_ANIM);
        }
        if (this.isEating()) {
            return  state.setAndContinue(DinoAnimationUtils.EATING_ANIM);
        }
        if (this.isInWater() && state.isMoving()){
            return state.setAndContinue(DinoAnimationUtils.SWIM_ANIM);
        }
        if(!state.isMoving() && this.isIdle()){
            return state.setAndContinue(DinoAnimationUtils.IDLE_ANIM);
        }

        return PlayState.STOP;
    }

    //MAIN DATA GETTERS
    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }

    public DinoBehaviourData getDinoBehaviour(){
        if(this.cachedBehaviourData == null){
            this.cachedBehaviourData = DinoBehaviourData.fromNBT(this.entityData.get(DINO_BEHAVIOUR));
        }
        return this.cachedBehaviourData;
    }

    public DinoDietData getDinoDiet(){
        if(this.dietData == null){
            this.dietData = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        }
        return this.dietData;
    }
    //DATA SYNC
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DINO_DATA, new DinoData());
        builder.define(DINO_BEHAVIOUR, new CompoundTag());
        builder.define(DINO_FAMILY_UUID, Optional.empty());
        builder.define(DINO_MATE, Optional.empty());

        builder.define(DinoNeedsUtils.HUNGER, 100.0F);
        builder.define(DinoNeedsUtils.THIRST, 100.0F);
        builder.define(DinoNeedsUtils.STAMINA, 100.0F);
        builder.define(DinoNeedsUtils.SOCIAL, 100.0F);
        builder.define(DinoNeedsUtils.AGGRESSION, 100.0F);
        builder.define(DinoNeedsUtils.DOMESTICITY, 100.0F);
        builder.define(DinoNeedsUtils.FERTILITY, 100.0F);
        builder.define(DinoNeedsUtils.IMMUNITY, 100.0F);
        builder.define(DinoNeedsUtils.INTELLIGENCE, 100.0F);
        builder.define(DinoNeedsUtils.SIZE, 1F);
        builder.define(DinoNeedsUtils.TAMING_SCORE, 100.0F);
        builder.define(DinoNeedsUtils.VISION, 100.0F);

        builder.define(DINOSAUR_HEAD_POS, new org.joml.Vector3f());

        builder.define(BABY_DATA_ID, false);
        builder.define(JUVENILE_DATA_ID, false);
        builder.define(SUB_ADULT_DATA_ID, false);
        builder.define(ADULT_DATA_ID, true);
        builder.define(SHOULD_TICK_STAMINA, true);

        builder.define(DinoAnimationUtils.IS_EATING_STATE, false);
        builder.define(DinoAnimationUtils.IS_DRINKING_STATE, false);
        builder.define(DinoAnimationUtils.IS_NESTING_STATE, false);
        builder.define(DinoAnimationUtils.IS_RESTING_STATE, false);
        builder.define(DinoAnimationUtils.IS_RISING_STATE, false);
        builder.define(DinoAnimationUtils.IS_SITTING_STATE, false);
        builder.define(DinoAnimationUtils.IS_ATTACKING_STATE, false);
        builder.define(DinoAnimationUtils.IS_FLINCHING_STATE, false);
        builder.define(DinoAnimationUtils.IS_DEAD_STATE, false);
        builder.define(DinoAnimationUtils.IS_SWIMMING_STATE, false);
        builder.define(DinoAnimationUtils.IS_RUNNING_STATE, false);
        builder.define(DinoAnimationUtils.LOOKING_LEFT_STATE, false);
        builder.define(DinoAnimationUtils.LOOKING_RIGHT_STATE, false);
        builder.define(DinoAnimationUtils.TURNING_RIGHT_STATE, false);
        builder.define(DinoAnimationUtils.TURNING_LEFT_STATE, false);
        builder.define(IS_ROARING_STATE, false);
        builder.define(DinoAnimationUtils.IS_SPEAKING_STATE, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("dino_data", CompoundTag.CODEC, this.getDinoData().toNBT());
        output.store("behaviour_profile", CompoundTag.CODEC, this.entityData.get(DINO_BEHAVIOUR));
        output.putFloat("hunger_bar", this.entityData.get(DinoNeedsUtils.HUNGER));
        output.putFloat("thirst_bar", this.entityData.get(DinoNeedsUtils.THIRST));
        output.putFloat("stamina_bar", this.entityData.get(DinoNeedsUtils.STAMINA));
        output.putFloat("social_bar", this.entityData.get(DinoNeedsUtils.SOCIAL));
        output.putFloat("trust_threshold", this.entityData.get(DinoNeedsUtils.TAMING_SCORE));
        output.putFloat("dino_vision", this.entityData.get(DinoNeedsUtils.VISION));
        output.putFloat("dino_aggression", this.entityData.get(DinoNeedsUtils.AGGRESSION));
        output.putFloat("dino_fertility", this.entityData.get(DinoNeedsUtils.FERTILITY));
        output.putFloat("dino_domesticity", this.entityData.get(DinoNeedsUtils.DOMESTICITY));
        output.putFloat("dino_size", this.entityData.get(DinoNeedsUtils.SIZE));
        output.putFloat("dino_intelligence", this.entityData.get(DinoNeedsUtils.INTELLIGENCE));
        output.putFloat("dino_immunity", this.entityData.get(DinoNeedsUtils.IMMUNITY));
        output.putBoolean("baby_age_boolean", this.entityData.get(BABY_DATA_ID));
        output.putBoolean("juvenile_age_boolean", this.entityData.get(JUVENILE_DATA_ID));
        output.putBoolean("sub_adult_age_boolean", this.entityData.get(SUB_ADULT_DATA_ID));
        output.putBoolean("adult_age_boolean", this.entityData.get(ADULT_DATA_ID));


        if(headBonePos != null){
            output.putDouble("headx",headBonePos.x);
            output.putDouble("heady",headBonePos.y);
            output.putDouble("headz",headBonePos.z);
        }

        if(this.entityData.get(DINO_MATE).isPresent()) {
            output.store("mate_uuid", UUIDUtil.CODEC, this.entityData.get(DINO_MATE).get());
        }
        if(this.entityData.get(DINO_FAMILY_UUID).isPresent()) {
            output.store("family_uuid", UUIDUtil.CODEC, this.entityData.get(DINO_FAMILY_UUID).get());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(DINO_DATA, DinoData.fromNBT(input.read("dino_data", CompoundTag.CODEC).orElseGet(CompoundTag::new)));
        this.entityData.set(DINO_BEHAVIOUR, input.read("behaviour_profile", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        this.entityData.set(DinoNeedsUtils.HUNGER, input.getFloatOr("hunger_bar", 100.0F));
        this.entityData.set(DinoNeedsUtils.THIRST, input.getFloatOr("thirst_bar", 100.0F));
        this.entityData.set(DinoNeedsUtils.STAMINA, input.getFloatOr("stamina_bar", 100.0F));
        this.entityData.set(DinoNeedsUtils.SOCIAL, input.getFloatOr("social_bar", 100.0F));
        this.entityData.set(BABY_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        this.entityData.set(JUVENILE_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        this.entityData.set(SUB_ADULT_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        this.entityData.set(ADULT_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        this.entityData.set(DinoNeedsUtils.TAMING_SCORE, input.getFloatOr("trust_threshold", 100.0F));
        this.entityData.set(DinoNeedsUtils.VISION, input.getFloatOr("dino_vision", 100.0F));
        this.entityData.set(DinoNeedsUtils.AGGRESSION, input.getFloatOr("dino_aggression", 100.0F));
        this.entityData.set(DinoNeedsUtils.FERTILITY, input.getFloatOr("dino_fertility", 100.0F));
        this.entityData.set(DinoNeedsUtils.DOMESTICITY, input.getFloatOr("dino_domesticity", 100.0F));
        this.entityData.set(DinoNeedsUtils.SIZE, input.getFloatOr("dino_size", 1.0F));
        this.entityData.set(DinoNeedsUtils.INTELLIGENCE, input.getFloatOr("dino_intelligence", 100.0F));
        this.entityData.set(DinoNeedsUtils.IMMUNITY, input.getFloatOr("dino_immunity", 100.0F));

        double headx = input.getDoubleOr("headx", Double.NaN);
        double heady = input.getDoubleOr("heady", Double.NaN);
        double headz = input.getDoubleOr("headz", Double.NaN);
        if(!Double.isNaN(headx) && !Double.isNaN(heady) && !Double.isNaN(headz)){
           headBonePos = new Vec3(headx, heady, headz);
        }

        Optional<UUID> mateUuid = input.read("mate_uuid", UUIDUtil.CODEC);
        this.entityData.set(DINO_MATE, mateUuid);

        Optional<UUID> familyUuid = input.read("family_uuid", UUIDUtil.CODEC);
        this.entityData.set(DINO_FAMILY_UUID, familyUuid);

    }

    public void setHeadPositon(Vec3 worldPos){
      org.joml.Vector3f pos = worldPos.toVector3f();
      this.entityData.set(DINOSAUR_HEAD_POS, pos);
    }
    public @Nullable Vec3 getHeadBonePos(){
        return new Vec3(this.entityData.get(DINOSAUR_HEAD_POS));
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    //MAIN DATA SETTERS
    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
    }

    public void setDinoBehaviour(CompoundTag behaviourData){
        this.entityData.set(DINO_BEHAVIOUR, behaviourData);
    }

    //TARGETING BOOLEANS FOR BRAIN
    public boolean canTargetWaterSource(BlockState entity){
        if(this.isSleeping()){
            return false;
        }

           return entity.is(Blocks.WATER);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        if(this.isSleeping()){
            return false;
        }
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getItem().getDescriptionId());
    }

    public boolean hurtFromPart(DinosaurPart part, DamageSource source, float amount) {
        return this.hurtServer((ServerLevel) this.level(), source, amount);
    }

    public boolean canTarget(LivingEntity target) {
       return target.getVehicle() != this;
    }

    public boolean isChild(){
        return this.isBaby() || this.isJuvanile();
    }

    public boolean isHuntingBlocked(){
        return BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_RESTING.get()) ||BrainUtil.hasMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM) || BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_EATING.get()) ||
                BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_DRINKING.get());
    }

    public boolean canTargetFeeder(BlockState target){
        return target.getBlock() instanceof DinosaurFeederBlock;
    }

    public boolean canTargetFoodItem(ItemEntity target) {
        if(this.isSleeping()){
            return false;
        }
        if(target.getItem().is(Items.AIR)){
            return false;
        }
        if(this.getDinoDiet() == null){
            return false;
        }
        return this.getDinoDiet().foodMap().containsKey(target.getItem().getItem().getDescriptionId());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getItem().getDescriptionId());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FenceAwareNavigation(this,level);
    }


    @Override
    public int getMaxHeadYRot() {
        return 2;
    }

    @Override
    public int getHeadRotSpeed() {
        return 2;
    }

    public boolean hasWantedLookTarget(){
        double x;
        double z;

        if(this.getLookControl() != null) {
            x = this.getLookControl().getWantedX();
            z = this.getLookControl().getWantedZ();
        } else{
            x = 0.0;
            z = 0.0;
        }
    return x != 0.0 && z != 0.0;
    }


    @Override
    public void setYRot(float yRot) {
        float currentYaw = super.getYRot();
        float delta = Mth.wrapDegrees(yRot - currentYaw);

        float maxTurn = 10.0F;

            if (delta > maxTurn) {
                delta = maxTurn;
                super.setYRot(currentYaw + delta);
            } else if (delta < -maxTurn) {
                delta = -maxTurn;
                super.setYRot(currentYaw + delta);
            } else {
                super.setYRot(yRot);
            }
    }
    //BRAIN

    @Override
    protected Brain<? extends LivingEntity> makeBrain(Brain.Packed packed) {
        return new SmartBrainProvider<>(this).makeBrain(this, packed);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity entity) {
        return this.distanceToSqr(entity) < (double)(this.getBbWidth() * 3.0F * this.getBbWidth() * 3.0F + entity.getBbWidth());
    }

    @Override
    public List<? extends ExtendedSensor<?>> getSensors(Dinosaur dinosaur) {
        NearestWaterSourceSensor<Dinosaur> waterSourceSensor = new NearestWaterSourceSensor<>();
        waterSourceSensor.setPredicate((dino, state) -> dino.canTargetWaterSource(state));
        waterSourceSensor.setRadius(20);
        NearestFeederSensor<Dinosaur> feederSensor = new NearestFeederSensor<>();
        feederSensor.setPredicate((dino, state) -> dino.canTargetFeeder(state));
        feederSensor.setRadius(20);
        NearbyBlocksSensor<Dinosaur> fenceProximinitySensor = new NearbyBlocksSensor<>();
        fenceProximinitySensor.detectionRadius(10.0);
        fenceProximinitySensor.setPredicate((dino, posState) -> posState.getSecond().is(BlockInit.ELECTRIC_FENCE.get()) && DinoNeedsUtils.starving(dino));
        NearestItemSensor<Dinosaur> foodItemSensor = new NearestItemSensor<>();
        foodItemSensor.setPredicate((dino, item) -> dino.canTargetFoodItem(item)) ;
        foodItemSensor.setRadius(20);
        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();
        return List.of(
                waterSourceSensor,
                feederSensor,
                nearbyLivingEntitySensor,
                foodItemSensor,
                fenceProximinitySensor
        );
    }


    @Override
    public ActivityBuilder<Dinosaur> getCoreBehaviourGroup(Dinosaur dinosaur) {
        return ActivityBuilder.<Dinosaur>create(Activity.CORE).behaviourPriorityBase(0).behaviours(
                new DinosaurLookAtTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dino) && (dino.isResting() || dino.isDrinking() || dino.isDeadOrDying())),
            //   new ThreatDisplay<>(34) //- needs to be made more situational so it happens more
              //      .whenStarting(dino -> dino.entityData.set(IS_ROARING_STATE, true))
                //    .whenStopping(dino -> dino.entityData.set(IS_ROARING_STATE,false)),
                new MoveToWalkTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dino) && (dino.isResting() || dino.isDrinking() || dino.isDeadOrDying())) ,
                new SetHunting<>(),
                new SetWalkTargetToWaterSource<>().closeEnoughWhen((entity, pos)-> 3),
                new WalkToNearestFeeder<>().closeEnoughWhen((entity, pos) -> 3) ,
                new SetWalkTargetToFoodItem<>().predicate(Dinosaur::canTargetFoodItem),
                new FollowParent<>().parentPredicate((baby, parent)-> baby instanceof Dinosaur dino && dino.isFamily(parent) && !parent.isBaby())
        );
    }


    @Override
    public ActivityBuilder<Dinosaur> getIdleBehaviourGroup(Dinosaur dinosaur) {
        return ActivityBuilder.<Dinosaur>create(Activity.IDLE).behaviourPriorityBase(50).behaviours(
                new FirstApplicableBehaviour(
                //        new Panic<>(),
                        new Drink<>(100)
                                .whenStarting(dino -> DinoAnimationUtils.setAnimationState(dino,"drink",true))
                                .whenStopping(dino ->  DinoAnimationUtils.setAnimationState(dino,"drink",false)),
                        new EatFromMeatFeeder<>(20)
                                .whenStarting(dino -> DinoAnimationUtils.setAnimationState(dino,"eat",true))
                                .whenStopping(dino ->  DinoAnimationUtils.setAnimationState(dino,"eat",false)),
                        new Eat<>(69)
                                .whenStarting(dino -> DinoAnimationUtils.setAnimationState(dino,"eat",true))
                                .whenStopping(dino ->  DinoAnimationUtils.setAnimationState(dino,"eat",false)),
                        new Rest<>(69)
                                .whenStarting(dino -> DinoAnimationUtils.setAnimationState(dino,"sit",true))
                                .whenStopping(dino -> DinoAnimationUtils.setAnimationState(dino,"rest",false)),
                        new GettingUpFromRestBehaviour<>(69)
                                .whenStarting(dino -> DinoAnimationUtils.setAnimationState(dino,"getup",true))
                                .whenStopping(dino ->DinoAnimationUtils.setAnimationState(dino,"getup",false)),
                       new SoloHuntingBehaviour<>()
                                .attackablePredicate(entity -> canTarget(entity))
                                .startCondition(dino -> dino instanceof CarnivoreDinosaur),
                        new SoloHuntRoamBehaviour<>()
                                .dontAvoidWater()
                                .setRadius(40.0D)
                                .stopIf(dino -> BrainUtil.hasMemory(dino, MemoryModuleType.ATTACK_TARGET)),
                new OneRandomBehaviour<>(
                        new SetRandomWalkTarget<>().dontAvoidWater().setRadius(10.0, 4.0).isValidPositionIf((dino, pos)-> !BrainUtil.hasMemory(dino, MemoryModuleTypeInit.BRAIN_OVERRIDE.get()))
                )));
    }

    @Override
    public ActivityBuilder<Dinosaur> getFightingBehaviourGroup(Dinosaur dinosaur) {
        return ActivityBuilder.<Dinosaur>create(Activity.FIGHT).behaviourPriorityBase(50).behaviours(
                new InvalidateAttackTarget<>()
                        .invalidateIf((entity, target) -> (target instanceof Player pl && (pl.isCreative() || pl.isSpectator())) || target.isDeadOrDying()),
                new SetWalkTargetToAttackTarget<>().speedModifier((owner, target) -> 1.5f)
                        .whenStarting(dino -> DinoAnimationUtils.setAnimationState((Dinosaur) dino,"run",true)),
                new BreakFenceBehaviour<>(20),
                new AnimatableMeleeAttack<>(20)
                        .whenStarting(dino -> DinoAnimationUtils.setAnimationState((Dinosaur) dino,"attack",true))
                        .whenStopping(dino -> DinoAnimationUtils.setAnimationState((Dinosaur) dino,"attack",false))
        );
    }
    private int flinchAnimTicks;
    private int restTicks;
    @Override
    public void tick() {
        super.tick();

        if(this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE)){
            flinchAnimTicks++;
            if(flinchAnimTicks > this.flinchAnimLength){
                this.entityData.set(DinoAnimationUtils.IS_FLINCHING_STATE, false);
                flinchAnimTicks = 0;
            }
        }

        if(!level().isClientSide() && !this.isDeadOrDying()) {
         //   this.socialDrainTick++;
            if(this.isResting()) {
                this.restTicks++;
            }
            if(this.shouldTickStamina()) {
                this.staminaDrainTick++;
            }

            if(this.isBaby() && !this.entityData.get(BABY_DATA_ID)){
                this.entityData.set(BABY_DATA_ID, true);
            } else if (!this.isBaby() && this.entityData.get(BABY_DATA_ID)){
                this.entityData.set(BABY_DATA_ID, false);
            }
            if(this.isJuvanile() && !this.entityData.get(JUVENILE_DATA_ID)){
                this.entityData.set(JUVENILE_DATA_ID, true);
            } else if (!this.isJuvanile() && this.entityData.get(JUVENILE_DATA_ID)){
                this.entityData.set(JUVENILE_DATA_ID, false);
            }
            if(this.isSubAdult() && !this.entityData.get(SUB_ADULT_DATA_ID)){
                this.entityData.set(SUB_ADULT_DATA_ID, true);
            } else if (!this.isSubAdult() && this.entityData.get(SUB_ADULT_DATA_ID)){
                this.entityData.set(SUB_ADULT_DATA_ID, false);
            }
            if(this.age >= 0 && !this.entityData.get(ADULT_DATA_ID)){
                this.entityData.set(ADULT_DATA_ID, true);
            } else if (this.age < 0 && this.entityData.get(ADULT_DATA_ID)){
                this.entityData.set(ADULT_DATA_ID, false);
            }

          if(this.breedingCoolDown == 0){
                if(this.getDinoGender() == 1.0F && this.hasMate()) {
                    this.tryBreedWithMate();
                    this.breedingCoolDown++;
                }
            }
            if(this.breedingCoolDown >= 1){
                this.breedingCoolDown++;
            }
            if(this.breedingCoolDown > 2000){
                this.breedingCoolDown = 0;
            }

            if(this.staminaDrainTick >= 20 && this.shouldTickStamina()){
                DinoNeedsUtils.tickStamina(this);
                if(DinoNeedsUtils.isTired(this) && !BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_TIRED.get())){
                    BrainUtil.setMemory(this, MemoryModuleTypeInit.IS_TIRED.get(), true);
                }
                this.staminaDrainTick = 0;
            }

            if(this.isResting() && restTicks >= 20){
                if(DinoNeedsUtils.getMaxStamina(this) > DinoNeedsUtils.getCurrentStamina(this)) {
                    float stamina = DinoNeedsUtils.getCurrentStamina(this);
                    float newStamina = stamina + 10F;
                    DinoNeedsUtils.setCurrentStamina(this, newStamina);
                }
                restTicks = 0;
            }


            if(this.socialDrainTick >= 300){
                DinoNeedsUtils.tickSocial();
                this.socialDrainTick = 0;
            }

            if(cachedDayTime != (int) this.level().getOverworldClockTime() / 24000L){
                cachedDayTime = (int) (this.level().getOverworldClockTime() / 24000L);
                this.isNewDay = true;
            }
            if(this.isNewDay){
                boolean eatenToday = Boolean.TRUE.equals(BrainUtil.getMemory(this, MemoryModuleTypeInit.EATEN_TODAY.get()));
                boolean drankToday = Boolean.TRUE.equals(BrainUtil.getMemory(this, MemoryModuleTypeInit.DRANK_TODAY.get()));
                if(!eatenToday){
                    if(BrainUtil.hasMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get())) {
                        daysSincelastAte = BrainUtil.getMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get());
                    } else { daysSincelastAte = 0; }
                        daysSincelastAte++;

                    BrainUtil.setMemory(this, MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get(), daysSincelastAte);
                }
                if(!drankToday){
                    if(BrainUtil.hasMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get())) {
                        daysSincelastDrink = BrainUtil.getMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get());
                    } else { daysSincelastDrink = 0;}

                    daysSincelastDrink++;

                    BrainUtil.setMemory(this, MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get(), daysSincelastDrink);
                }

                this.isNewDay = false;
                BrainUtil.setMemory(this,MemoryModuleTypeInit.MEAL_COUNTER.get(), 0);
                BrainUtil.setMemory(this, MemoryModuleTypeInit.DRANK_TODAY.get(), false);
                BrainUtil.setMemory(this, MemoryModuleTypeInit.EATEN_TODAY.get(), false);
                DinoNeedsUtils.tickThirst(this,this.daysSincelastDrink,this.getDinoBehaviour().dehydrationLimit());
                DinoNeedsUtils.tickHunger(this,this.daysSincelastAte,this.getDinoBehaviour().starvationLimit());

                long activeStart;
                long activeEnd;

                if(this.getDinoBehaviour().isNocturnal()){
                    activeStart = 12000;
                    activeEnd = 23999;
                } else {
                    activeStart = 0;
                    activeEnd = 12000;
                }
                Random random = new Random();

                for (int i = 0; i < this.getDinoBehaviour().eatRate() - 1; i++) {
                    long hungerTime = random.nextInt((int)activeStart,(int) activeEnd);
                    hungerSchedule.add(hungerTime);
                }
                for (int i = 0; i < this.getDinoBehaviour().drinkRate() - 1; i++) {
                    long thirstTime = random.nextInt((int)activeStart,(int) activeEnd);
                    thirstSchedule.add(thirstTime);
                }
            }
            if(!thirstSchedule.isEmpty()) {
                int toRemove = -1;
                int i = 0;

                for (long thirstTime : thirstSchedule) {
                    if (this.level().getOverworldClockTime() % 24000 >= thirstTime) {
                        toRemove = i;
                        DinoNeedsUtils.tickThirst(this, this.daysSincelastDrink, this.getDinoBehaviour().dehydrationLimit());
                    }
                    i++;
                }
                if(toRemove != -1) {
                    thirstSchedule.remove(toRemove);
                }
            }
            if(!hungerSchedule.isEmpty()) {
                int toRemove = -1;
                int i = 0;

                for (long hungerTime : hungerSchedule) {
                    if (this.level().getOverworldClockTime() % 24000 >= hungerTime) {
                        toRemove = i;
                        DinoNeedsUtils.tickHunger(this, this.daysSincelastAte, this.getDinoBehaviour().starvationLimit());
                    }
                    i++;
                }
                if(toRemove != -1) {
                    hungerSchedule.remove(toRemove);
                }
            }
            if(!DinoNeedsUtils.isHungry(this) && BrainUtil.hasMemory(this, MemoryModuleTypeInit.HUNTING.get())){
                BrainUtil.clearMemory(this, MemoryModuleTypeInit.HUNTING.get());
            }
        }
    }
    Random random = new Random();

    public void tryBreedWithMate(){
        int attempt = random.nextInt(0,100);
        if(attempt > 50) {
            this.setInLove(null);
            @Nullable Dinosaur mate = BrainUtil.getMemory(this, MemoryModuleTypeInit.MATE.get());

            if(mate != null) {
                mate.setInLove(null);
            } else {
                this.clearDinoMate();
            }
        }
    }

    public boolean isNight(){
        int day = (int) this.level().getOverworldClockTime() % 24000;
        return day > 12000;
    }
    public boolean isDay(){
        return !this.isNight() ;
    }


    //ATTRIBUTES
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35)
                .add(Attributes.MOVEMENT_SPEED, .25)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .add(AttributesInit.DINO_ENERGY_NEED.holder(),100)
                .add(AttributesInit.DINO_THIRST_NEED.holder(), 100)
                .add(AttributesInit.DINO_HUNGER_NEED.holder(),100)
                .add(AttributesInit.DINO_SOCIAL_NEED.holder(),100)
                .add(AttributesInit.TRUST_SCORE.holder(),1000)
                .add(AttributesInit.DINO_VISION.holder(),100)
                .add(AttributesInit.DINO_AGGRESSION.holder(), 0)
                .add(AttributesInit.DINO_INTELLIGENCE.holder(), 50)
                .add(AttributesInit.DINO_FERTILITY.holder(),50)
                .add(AttributesInit.DINO_IMMUNITY.holder(),0);
    }

    public boolean isDrinking() {
        return this.entityData.get(DinoAnimationUtils.IS_DRINKING_STATE);
    }
    public boolean isEating() {return this.entityData.get(DinoAnimationUtils.IS_EATING_STATE);}
    public boolean isSitting() {return this.entityData.get(DinoAnimationUtils.IS_SITTING_STATE);}
    public boolean isRising() {
        return this.entityData.get(DinoAnimationUtils.IS_RISING_STATE);
    }
    public boolean isResting() {
        return this.entityData.get(DinoAnimationUtils.IS_RESTING_STATE);
    }
    public boolean isRoaring() {
        return this.entityData.get(IS_ROARING_STATE);
    }
    public boolean isAttacking() {return this.entityData.get(DinoAnimationUtils.IS_ATTACKING_STATE);}
    public boolean isIdle(){return !this.isDrinking() && !this.isEating() && !this.isResting() && !this.isRoaring() && !this.isAttacking();}

    public String getStringDinoGender() {
        //Gets Gender and if none has been set then returns as female.
        double geneGender = this.getDinoData().getGeneValue(GeneInit.GENDER.get());
        if(geneGender == 2.0D){
            return "male";
        } else return "female";
    }
    public double getDinoGender(){
      return this.getDinoData().getGeneValue(GeneInit.GENDER.get());
    }
    @Nullable
    public Map<Player,Integer> getPlayerReputationMap(){
        if(BrainUtil.hasMemory(this, MemoryModuleTypeInit.PLAYER_REPUTATION.get())){
            return BrainUtil.getMemory(this, MemoryModuleTypeInit.PLAYER_REPUTATION.get());
        } else return null;
    }

    public int getReputationForPlayer(Player player){
        if(this.getPlayerReputationMap() != null){
            return this.getPlayerReputationMap().getOrDefault(player, 0);
        } else return 0;
    }
    public void increaseReputationForPlayer(Player player, int increase){
        if(this.getPlayerReputationMap() == null || !this.getPlayerReputationMap().containsKey(player)){
            return;
        }
        Map<Player,Integer> reputationMap = this.getPlayerReputationMap();
        int currentReputation = reputationMap.get(player);
        reputationMap.remove(player);
        reputationMap.put(player,currentReputation + increase);
    }
    public void decreaseReputationForPlayer(Player player, int decrease){
        if(this.getPlayerReputationMap() == null || !this.getPlayerReputationMap().containsKey(player)){
            return;
        }
        Map<Player,Integer> reputationMap = this.getPlayerReputationMap();
        int currentReputation = reputationMap.get(player);
        reputationMap.remove(player);
        reputationMap.put(player,currentReputation - decrease);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        EntityType<?> babyType = this.getDinoData().getBaseDino();
        return (Dinosaur) babyType.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        @Nullable Dinosaur dinosaur = (Dinosaur) this.getBreedOffspring(level, mate);
        Dinosaur mother;
        if(this.getDinoGender() == 1){
            mother = this;
        } else mother = (Dinosaur) mate;

        if(mate instanceof Dinosaur) {
            if (dinosaur != null) {
                dinosaur.setDinoData(mother.getDinoData());
                DinoNeedsUtils.setDinoBaseNeeds(dinosaur, mother.getDinoBehaviour());
                dinosaur.setDinoBehaviour(mother.getDinoBehaviour().toNBT(mother.getDinoBehaviour()));
                // nextInt(1,2) is always 1 — every bred baby was female; roll 1 or 2
                dinosaur.getDinoData().setGeneValue(GeneInit.GENDER.get(), random.nextInt(1,3));
                dinosaur.setBaby(true);
                dinosaur.setDinoFamilyUuid(this.getFamilyId());
                dinosaur.snapTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                this.finalizeSpawnChildFromBreeding(level, mate, dinosaur);
                level.addFreshEntityWithPassengers(dinosaur);
            }
        }
    }

    public boolean isJuvanile(){
        int age = this.age;
        return age <= -12000 && age > -18000;
    }

    public boolean isSubAdult(){
        int age = this.age;
        return age <= -6000 && age > -12000;
    }

    public int getGrowthStage(){
        if(this.entityData.get(BABY_DATA_ID)){
            return 1;
        }
        else if(this.entityData.get(JUVENILE_DATA_ID)){
            return 2;
        }
        else if(this.entityData.get(SUB_ADULT_DATA_ID)){
            return 3;

        } else return 4;
    }

    public boolean shouldTickStamina(){
        return this.entityData.get(SHOULD_TICK_STAMINA) && !this.isResting();
    }


    public void createDinosaurFamily(Dinosaur mate){
        UUID newFamilyId = UUID.randomUUID();
        this.setDinoFamilyUuid(newFamilyId);
        mate.setDinoFamilyUuid(newFamilyId);
    }
    public void setDinoFamilyUuid(UUID familyUuid){
        this.entityData.set(DINO_FAMILY_UUID,Optional.of(familyUuid));
    }
    public @Nullable UUID getFamilyId(){
        if(this.entityData.get(DINO_FAMILY_UUID).isPresent()) {
            return this.entityData.get(DINO_FAMILY_UUID).get();
        } else return null;
    }

    public @Nullable UUID getGroupId() {
        if(BrainUtil.hasMemory(this, MemoryModuleTypeInit.GROUP_UUID.get())){
            return BrainUtil.getMemory(this, MemoryModuleTypeInit.GROUP_UUID.get());
        } else return null;
    }
    public boolean hasGroup() {
        if(BrainUtil.hasMemory(this,MemoryModuleTypeInit.HAS_GROUP.get())) {
            return Boolean.TRUE.equals(BrainUtil.getMemory(this, MemoryModuleTypeInit.HAS_GROUP.get()));
        } else return false;
    }
    public boolean isGroupLeader() {
        if(BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_GROUP_LEADER.get())){
            return Boolean.TRUE.equals(BrainUtil.getMemory(this, MemoryModuleTypeInit.IS_GROUP_LEADER.get()));
        } else return false;
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return this.isMate(otherAnimal.getUUID());
    }

    @Override
    public boolean canBreed() {
        return !this.isBaby() && !this.isJuvanile() && !this.isSubAdult();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        if (this.level().isClientSide()) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.STICK) && !(this.getLastHurtByMob() != null && this.getLastAttacker().is(player));
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.heal(itemStack.get(DataComponents.FOOD).nutrition());
                return InteractionResult.SUCCESS;
            } else {
                InteractionResult interactionresult = super.mobInteract(player, hand);
                if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget((LivingEntity) null);
                    return InteractionResult.SUCCESS;
                } else {
                    return interactionresult;
                }
            }
        } else if (itemStack.is(Items.STICK)) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            if (this.random.nextInt(3) == 0) {
                this.increaseReputationForPlayer(player,10);

                if(!this.isTame() && this.getReputationForPlayer(player) > 100){
                    this.tame(player);
                }
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;


        } else {
            return super.mobInteract(player, hand);
        }
    }

    public boolean canMateWith(Dinosaur pDinosaur, Dinosaur pMate){

        if(pDinosaur.isBaby() || pMate.isBaby()){
            return false;
        }if(pDinosaur.isJuvanile() || pMate.isJuvanile()){
            return false;
        }if(pDinosaur.isSubAdult() || pMate.isSubAdult()) {
            return false;
        }
        if(!pDinosaur.isAlive() || !pMate.isAlive()){
            return false;
        }
        if(pDinosaur.hasMate() || pMate.hasMate()){
            return false;
        }

        return pDinosaur.getDinoGender() != pMate.getDinoGender();
    }

    public boolean isMate(UUID mateId){
        if(this.entityData.get(DINO_MATE).isPresent()) {
            return mateId == this.entityData.get(DINO_MATE).get();
        } else return false;
    }
    public boolean isFamily(LivingEntity pMob){
        if(pMob instanceof Dinosaur mob){
            if(mob.getFamilyId() != null && this.getFamilyId() != null){
            return mob.getFamilyId().equals(this.getFamilyId());
            } else return false;
        } else return false;
    }

    public boolean hasMate(){
        return !this.entityData.get(DINO_MATE).isEmpty();
    }
    public void registerDinoMate(UUID pMateId){
        this.entityData.set(DINO_MATE,Optional.of(pMateId));
    }
    public void clearDinoMate(){
        this.entityData.set(DINO_MATE, Optional.empty());
    }

    //SKIN SETTER
    public int layerColor(int layer, DinoLayer dinoLayer) {
        // GeckoLib 5 takes the color int directly as the vertex ARGB color, so force alpha
        // (the stored layer colors are RGB-only — alpha 0 would render the pass transparent)
        if (dinoLayer != null && dinoLayer.getBasicLayer() == -1) {
            return 0xFFFFFFFF;
        }
        if (layer >= this.getDinoData().getLayerColors().stream().count()) {
            return 0xFF000000 | Mth.floor(this.getDinoData().getLayerColor(dinoLayer.getBasicLayer()));
        }
        return 0xFF000000 | Mth.floor(this.getDinoData().getLayerColor(layer));
    }

    public @Nullable SoundEvent getRoarSound(){
        return null;
    }
    public @Nullable SoundEvent getAttackGrowlSound(){
        return null;
    }
    public @Nullable SoundEvent getAttackSound(){
        return null;
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float damageAmount) {
        if(damageSource.getEntity() instanceof Player player){
            this.decreaseReputationForPlayer(player, 20);
            if(this.isTame() && this.getOwner().is(player) && this.getReputationForPlayer(player) < 40){
                this.setTame(false, false);
                this.setOwnerReference(null);
            }
        }
        DinoAnimationUtils.setAnimationState(this,"flinch", true);
        super.actuallyHurt(level, damageSource, damageAmount);

    }
}
