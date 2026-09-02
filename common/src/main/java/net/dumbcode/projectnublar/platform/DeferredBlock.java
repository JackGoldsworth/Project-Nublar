package net.dumbcode.projectnublar.platform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {

    public DeferredBlock(ResourceKey<Block> key) {
        super(key);
    }
}
