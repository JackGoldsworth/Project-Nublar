package net.dumbcode.projectnublar.entity.ai.behaviour.actions;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.List;

public class BreakFenceBehaviour<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final Set<MemoryCondition<?, ?>> MEMORY_REQUIREMENTS = Set.of(new MemoryCondition.Present<>(MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get()));

    @Nullable BlockEntityElectricFence beElectricFence;
    @Nullable BlockEntity beToTest;

    public BreakFenceBehaviour(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(BrainUtil.hasMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get())){
            List<BlockInWorld> nearby_blocks = BrainUtil.getMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get());

            if(nearby_blocks == null) {return false;}

            for(BlockInWorld blockToTest: nearby_blocks){
                if (blockToTest.getState().is(BlockInit.ELECTRIC_FENCE.get())){
                  if(level.getBlockEntity(blockToTest.getPos()) != null) {
                      BlockPos testPos = blockToTest.getPos();
                      beToTest = level.getBlockEntity(blockToTest.getPos());
                      if(beToTest != null) {
                          if(beToTest instanceof BlockEntityElectricFence entityElectricFence && entity.distanceToSqr(Vec3.atCenterOf(testPos)) < 2) {
                              beElectricFence = entityElectricFence;
                          }
                      }
                  }
                }
            }

        }
        return beElectricFence != null;
    }

    @Override
    protected void start(E entity) {
        BrainUtil.clearMemory(entity, MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get());
        BrainUtil.clearMemory(entity,SBLMemoryTypes.NEARBY_BLOCKS.get());
        DinoAnimationUtils.setAnimationState(entity,"attack", true);

    }

    @Override
    protected void doDelayedAction(E entity) {
        DinoAnimationUtils.setAnimationState(entity, "attack",false);
        this.beElectricFence.breakFence(10);
        entity.level().players().forEach(p ->
                p.sendSystemMessage(Component.literal("WARNING: Fence Destroyed at: " + entity.position() + ", by a: " + entity)));
    }

    @Override
    protected void stop(E entity) {
        this.beElectricFence = null;
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }
}
