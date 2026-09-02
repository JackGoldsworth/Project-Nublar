package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TestTubeItem extends DNADataItem {
    public TestTubeItem(Properties properties) {
        super(properties);
    }


    @Override
    public Component getName(ItemStack stack) {
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        if(data != null) {
            return Component.translatable("item.projectnublar.test_tube2", data.getFormattedType());
        }
        DinoData dinoData = stack.get(DataComponentInit.DINO_DATA.get());
        if(dinoData != null) {
            return Component.translatable("item.projectnublar.test_tube2", dinoData.getFormattedType());
        }
        return super.getName(stack);
    }
}
