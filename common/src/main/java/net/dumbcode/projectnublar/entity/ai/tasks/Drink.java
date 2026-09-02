package net.dumbcode.projectnublar.entity.ai.tasks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.List;
import java.util.function.Predicate;


public class Drink<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final Set<MemoryCondition<?, ?>> MEMORY_REQUIREMENTS = Set.of(new MemoryCondition.Present<>(MemoryModuleTypeInit.HAS_FOUND_WATER.get()),new MemoryCondition.Present<>(MemoryModuleTypeInit.IS_THIRSTY.get()));

    protected Predicate<? extends BlockState> targetPredicate = (blockState) -> true;
    protected Predicate<E> canTargetPredicate = (dinosaur) -> true;

    public Drink(int delayTicks) {
        super(delayTicks);
    }


    public Drink<E> targetPredicate(final Predicate<BlockState> predicate) {
        this.targetPredicate = predicate; return this;
    }
    public Drink<E> canTargetPredicate(final Predicate<E> predicate) {
        this.canTargetPredicate = predicate; return this;
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        BlockPos nearestWaterSource = BrainUtil.getMemory(dinosaur, MemoryModuleTypeInit.HAS_FOUND_WATER.get());

        if(dinosaur.distanceToSqr(Vec3.atCenterOf(nearestWaterSource)) > 5){
            return false;
        }

        return !BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_DRINKING.get());

    }

    @Override
    protected void start(E dinosaur) {
        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_DRINKING.get(), true);
    }

    @Override
    protected void doDelayedAction(E dinosaur) {
        BrainUtil.clearMemory(dinosaur, MemoryModuleTypeInit.IS_DRINKING.get());
        DinoNeedsUtils.drink(dinosaur);
    }

    @Override
    protected void stop(E entity) {
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_DRINKING.get());
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_THIRSTY.get());
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.HAS_FOUND_WATER.get());
    }
}
