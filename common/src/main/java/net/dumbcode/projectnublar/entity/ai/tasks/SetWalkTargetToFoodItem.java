package net.dumbcode.projectnublar.entity.ai.tasks;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class SetWalkTargetToFoodItem<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final Set<MemoryCondition<?, ?>> MEMORY_REQUIREMENTS = Set.of(
            new MemoryCondition.Present<>(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
            new MemoryCondition.Present<>(MemoryModuleTypeInit.IS_HUNGRY.get()));


    protected BiPredicate<E, ItemEntity> predicate = (entity, item) -> true;
    protected BiFunction<E, BlockPos, Float> speedMod = (owner, pos) -> 1f;
    protected BiFunction<E, BlockPos, Integer> closeEnoughDist = (entity, pos) -> 2;

    protected ItemEntity target = null;
    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    public SetWalkTargetToFoodItem<E> predicate(final BiPredicate<E, ItemEntity> predicate) {
        this.predicate = predicate;

        return this;
    }

    public SetWalkTargetToFoodItem<E> speedMod(BiFunction<E, BlockPos, Float> speedModifier) {
        this.speedMod = speedModifier;

        return this;
    }

    public SetWalkTargetToFoodItem<E> closeEnoughWhen(final BiFunction<E, BlockPos, Integer> function) {
        this.closeEnoughDist = function;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        return this.target != null;
    }

    @Override
    protected void start(E entity) {
        BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.target, this.speedMod.apply(entity, this.target.blockPosition()), this.closeEnoughDist.apply(entity, this.target.blockPosition())));
        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.target.blockPosition()));
    }

}
