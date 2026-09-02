package net.dumbcode.projectnublar.platform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {

    public DeferredItem(ResourceKey<Item> key) {
        super(key);
    }
}
