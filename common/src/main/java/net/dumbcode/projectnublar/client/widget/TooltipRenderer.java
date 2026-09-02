package net.dumbcode.projectnublar.client.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface TooltipRenderer {
    void renderTooltip(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick);
}
