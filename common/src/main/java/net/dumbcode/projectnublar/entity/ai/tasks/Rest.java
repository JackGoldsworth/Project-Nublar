package net.dumbcode.projectnublar.entity.ai.tasks;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.List;

public class Rest<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final Set<MemoryCondition<?, ?>> MEMORY_REQUIREMENTS = Set.of(new MemoryCondition.Present<>(MemoryModuleTypeInit.IS_TIRED.get()));

    public Rest(int delayTicks) {
        super(delayTicks);
    }

    boolean isNightTime;
    boolean isNocturnal;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        if (BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_DEHYDRATED.get()) || BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_STARVING.get())){
            return BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_EXHAUSTED.get());
        }
        return BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_TIRED.get()) && !BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
    }

    @Override
    protected void tick(E dinosaur) {
        super.tick(dinosaur);

        isNightTime = dinosaur.level().getOverworldClockTime() % 24000 > 12000;
        isNocturnal = dinosaur.getDinoBehaviour().isNocturnal();

        if((isNightTime && isNocturnal) || (!isNightTime && !isNocturnal)){
            if (DinoNeedsUtils.getCurrentStamina(dinosaur) >= DinoNeedsUtils.getMaxStamina(dinosaur)) {
                BrainUtil.clearMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
            }
            if (DinoNeedsUtils.isDehydratedOrStarving(dinosaur)) {
                BrainUtil.clearMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
                BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.GETTING_UP.get(), true);
                DinoAnimationUtils.setAnimationState(dinosaur, "rest", false);
            }
        }
    }


    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E dinosaur) {
        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_RESTING.get(), true);
        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_SITTING.get(), true);
    }

    @Override
    protected void doDelayedAction(E entity) {
        super.doDelayedAction(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_SITTING.get());
        DinoAnimationUtils.setAnimationState(entity,"sit",false);
        DinoAnimationUtils.setAnimationState(entity,"rest",true);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return BrainUtil.hasMemory(entity, MemoryModuleTypeInit.IS_RESTING.get());
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_TIRED.get());
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.IS_RESTING.get());
        BrainUtil.setMemory(entity, MemoryModuleTypeInit.GETTING_UP.get(), true);
        DinoAnimationUtils.setAnimationState(entity, "rest", false);
    }
}
