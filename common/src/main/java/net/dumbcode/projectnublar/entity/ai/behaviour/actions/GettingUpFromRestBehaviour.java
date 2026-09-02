package net.dumbcode.projectnublar.entity.ai.behaviour.actions;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.List;

public class GettingUpFromRestBehaviour <E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final Set<MemoryCondition<?, ?>> MEMORY_REQUIREMENTS = Set.of(new MemoryCondition.Present<>(MemoryModuleTypeInit.GETTING_UP.get()));

    public GettingUpFromRestBehaviour(int delayTicks) {
        super(delayTicks);
    }


    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void doDelayedAction(E entity) {
        super.doDelayedAction(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.GETTING_UP.get());
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.GETTING_UP.get());
    }
}