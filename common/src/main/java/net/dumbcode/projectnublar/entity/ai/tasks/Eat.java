package net.dumbcode.projectnublar.entity.ai.tasks;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Set;
import java.util.List;
import java.util.function.BiPredicate;

public class Eat<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final Set<MemoryCondition<?, ?>> MEMORY_REQUIREMENTS = Set.of(new MemoryCondition.Present<>(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                    new MemoryCondition.Present<>(MemoryModuleTypeInit.IS_HUNGRY.get()));

    protected BiPredicate<E,? extends ItemEntity> targetPredicate = (dinosaur, foodItem) -> true ;

    private ItemEntity foodItem;

    public Eat(int delayTicks) {
        super(delayTicks);
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        foodItem = BrainUtil.getMemory(dinosaur, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);

        if(dinosaur.getDinoDiet().foodMap().containsKey(foodItem.getItem().getItem().getDescriptionId())){
            return dinosaur.distanceToSqr(foodItem) <= 5;
        } else return false;
    }

    @Override
    protected void doDelayedAction(E dinosaur) {
       if(foodItem != null) {
           ItemStack foodItemstack = foodItem.getItem();
           //Check to prevent bug where air sometimes ends up in the feed method.
           if(dinosaur.getDinoDiet().foodMap().containsKey(foodItemstack.getItem().getDescriptionId())) {
               DinoNeedsUtils.feed(dinosaur, foodItemstack.getItem().getDescriptionId());
           }
           foodItemstack.shrink(1);
       }
    }

    @Override
    protected void start(E dinosaur) {
      BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_EATING.get(), true);
    }

    @Override
    protected void stop(E dinosaur) {
        BrainUtil.clearMemory(dinosaur, MemoryModuleTypeInit.IS_EATING.get());
        BrainUtil.clearMemory(dinosaur, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

}
