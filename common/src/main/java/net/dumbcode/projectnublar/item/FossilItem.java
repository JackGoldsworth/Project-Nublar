package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FossilItem extends DNADataItem {
    public FossilItem(Properties properties) {
        super(properties);
    }


    @Override
    public Component getName(ItemStack stack) {
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        if(data != null){
            return Component.translatable("item." + Constants.MODID + ".fossil", data.getFormattedType(), Component.translatable("piece.projectnublar." + data.getFossilPiece().name())).withStyle(data.getQuality().getColor());
        }
        return super.getName(stack);
    }

}
