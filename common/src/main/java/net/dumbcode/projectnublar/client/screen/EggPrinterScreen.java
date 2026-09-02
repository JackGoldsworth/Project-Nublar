package net.dumbcode.projectnublar.client.screen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class EggPrinterScreen extends AbstractContainerScreen<EggPrinterMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/egg_printer.png");
    private static final Identifier BONEMEAL_BAR = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/bonemeal_bar.png");
    private static final Identifier EGG = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/egg.png");

    public EggPrinterScreen(EggPrinterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 230);
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        pGuiGraphicsExtractor.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int x = this.leftPos;
        int y = this.topPos;
        pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,x, y, 0, 0, imageWidth, imageHeight,imageWidth,imageHeight);
        float bmProgress = menu.getDataSlot(0) / 30f;
        int bmImageHeight = Mth.floor(54 * bmProgress);
        pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, BONEMEAL_BAR, x + 16, y + 17 + 54 - bmImageHeight, 0, 54-bmImageHeight, 14, bmImageHeight, 14, 54);
        float eggProgress = menu.getDataSlot(2) / (float)menu.getDataSlot(3);
        int eggImageHeight = Mth.floor(66 * eggProgress);
        pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, EGG, x + 65, y + 28 + 66 - eggImageHeight, 0, 66-eggImageHeight, 43, eggImageHeight, 43, 66);

    }
}
