package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AmberItem extends DNADataItem {
    public AmberItem(Properties $$0) {
        super($$0);
    }

    @Override
    public Component getName(ItemStack stack) {
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        if(data != null){
            return Component.translatable("item." + Constants.MODID + ".amber", data.getFormattedType());
        }
        return super.getName(stack);
    }

}
