package net.dumbcode.projectnublar.item.api;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DNADataItem extends Item {

    public DNADataItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        List<Component> tooltips = new ArrayList<>();
        DNAData dnaData = stack.get(DataComponentInit.DNA_DATA.get());
        if (dnaData != null) {
            DNAData.createTooltip(stack, tooltips);
        } else {
            DinoData dinoData = stack.get(DataComponentInit.DINO_DATA.get());
            if (dinoData != null) {
                dinoData.createToolTip(tooltips);
            }
        }
        tooltips.forEach(tooltipAdder);
    }
}
