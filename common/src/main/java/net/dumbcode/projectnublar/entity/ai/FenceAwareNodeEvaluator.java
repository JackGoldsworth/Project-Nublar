package net.dumbcode.projectnublar.entity.ai;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FenceAwareNodeEvaluator extends WalkNodeEvaluator {
    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        PathType original = super.getPathType(context, x, y, z);
        BlockPos pos = new BlockPos(x,y,z);
        BlockState ground = context.getBlockState(pos);
        BlockState above = context.getBlockState(pos.above());
        BlockState above2 = context.getBlockState(pos.above(2));

        if(isFenceOrWire(ground) || isFenceOrWire(above) || isFenceOrWire(above2)){
            Mob mob = this.mob;
            if(mob instanceof Dinosaur dinosaur && DinoNeedsUtils.allNeedsAtZero(dinosaur)) {
                    return PathType.OPEN;
            }

            return PathType.FENCE;
        }

        return original;
    }

    private boolean isFenceOrWire(BlockState state){
        return state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get()) || state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get()) ||
                state.is(BlockInit.ELECTRIC_FENCE.get());
    }
}
