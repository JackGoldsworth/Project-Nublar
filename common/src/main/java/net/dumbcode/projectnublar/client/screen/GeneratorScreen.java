package net.dumbcode.projectnublar.client.screen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/coal_generator.png");
    public GeneratorScreen(GeneratorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractRenderState(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        pGuiGraphicsExtractor.centeredText(Minecraft.getInstance().font, menu.getData(0) + "/" + menu.getData(1) + " FE", this.width / 2, this.topPos + 64, 0xFFFFFF);
    }
}
