package net.dumbcode.projectnublar.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface PosMenuFactory<T extends AbstractContainerMenu> {

    T create(int containerId, Inventory inventory, BlockPos pos);
}
