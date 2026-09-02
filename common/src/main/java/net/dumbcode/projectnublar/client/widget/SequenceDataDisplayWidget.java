package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class SequenceDataDisplayWidget extends AbstractButton {

    private Supplier<ItemStack> stack;
    private String value;
    public boolean selected = false;
    private OnClick onClick;

    public SequenceDataDisplayWidget(int x, int y, int width, int height, Supplier<ItemStack> stack, String value, OnClick onClick) {
        super(x, y, width, height, Component.empty());
        this.stack = stack;
        this.value = value;
        this.onClick = onClick;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void onPress(net.minecraft.client.input.InputWithModifiers input) {
        this.selected = !selected;
        onClick.onClick(this, selected);
    }

    // 26.2: renderWidget is now extractContents (extract-then-submit widget rendering)
    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        SequencerScreen.drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), Constants.BORDER_COLOR, 1);
        int color = 0xFF193B59;
        if (selected) {
            color = 0xFF063B6B;
        }
        if (isHovered()) {
            color = 0xFF063B6B;
        }
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, color);
        if (stack.get() != null && !stack.get().isEmpty()) {
            // 26.2: stack NBT is gone — the disk's DNA entries live in the DISK_DNA map component.
            // typed local: chaining getOrDefault off the wildcard holder breaks generic inference
            java.util.Map<String, DNAData> dnaOnDisk = stack.get().getOrDefault(net.dumbcode.projectnublar.init.DataComponentInit.DISK_DNA.get(), java.util.Map.of());
            DNAData dnaData = dnaOnDisk.get(value);
            if (dnaData != null) {
                guiGraphics.centeredText(Minecraft.getInstance().font, dnaData.getFormattedType().getString() + ": " + dnaData.getFormattedDNANoDescriptor().getString(), getX() + width / 2, getY() + 3, 0xFFFFFFFF);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public interface OnClick {
        void onClick(SequenceDataDisplayWidget widget, boolean selected);
    }
}
