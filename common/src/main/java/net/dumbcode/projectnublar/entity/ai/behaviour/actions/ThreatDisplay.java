package net.dumbcode.projectnublar.entity.ai.behaviour.actions;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.init.SoundInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.List;

public class ThreatDisplay<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final Set<MemoryCondition<?, ?>> MEMORY_REQUIREMENTS = Set.of(new MemoryCondition.Present<>(MemoryModuleType.NEAREST_LIVING_ENTITIES));

    public ThreatDisplay(int delayTicks) {
        super(delayTicks);
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(!BrainUtil.hasMemory(entity, MemoryModuleTypeInit.IS_ROARING.get())){
            return false;
        }
        return !entity.isRoaring();
    }

    @Override
    protected void start(E entity) {
        DinoAnimationUtils.setAnimationState(entity, "roar", true);
        entity.playSound(SoundInit.TYRANNOSAUR_ROAR.get(), 10,1);
    }

    @Override
    protected void doDelayedAction(E entity) {
        DinoAnimationUtils.setAnimationState(entity, "roar", false);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_ROARING.get());
    }
}
