package net.dumbcode.projectnublar.client.screen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.dumbcode.projectnublar.platform.Services;

import java.util.List;

public class IncubatorScreen extends AbstractContainerScreen<IncubatorMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/incubator.png");

    private static final int TEXTURE_WIDTH = 334;
    private static final int TEXTURE_HEIGHT = 222;

    private static final int OVERLAY_START_X = 9;
    private static final int OVERLAY_START_Y = 9;

    public static final int BED_WIDTH = 158;
    public static final int BED_HEIGHT = 115;

    private int draggedSlotIndex = -1;
    private int draggedX;
    private int draggedY;

    public IncubatorScreen(IncubatorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 222);
        this.titleLabelY = -100;
        this.inventoryLabelY = -100;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        pGuiGraphicsExtractor.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int x = this.leftPos;
        int y = this.topPos;
        pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 334, 222);
        pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 9, y + 9, imageWidth, 0, BED_WIDTH, BED_HEIGHT, 334, 222);
        int plantmatterMax = this.menu.getData().get(1);
        int plantmatter = this.menu.getData().get(0);
        int progress = Mth.floor(((plantmatter / (float) plantmatterMax)) * 63);
        pGuiGraphicsExtractor.fill(x + 28, y + 121, x + 28 + progress, y + 121 + 4, 0xFFA9E245);
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ContainerInput pInput) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pInput);
        if (pSlot instanceof IncubatorMenu.IncubatorSlot) {
            if (pInput == ContainerInput.PICKUP) {
                int x = pSlot.x;
                int y = pSlot.y;
                if (pSlotId == this.draggedSlotIndex) {
                    x = this.draggedX;
                    y = this.draggedY;
                } else if (pSlot.hasItem()) {
                    x = 0;
                    y = -100;
                }
                Services.PLATFORM.sendToServer(new UpdateIncubatorSlotPacket(menu.getPos(), pSlotId, x, y));
            }
        }
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        boolean hover = super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
        if (menu.getCarried().is(ItemInit.UNINCUBATED_EGG.get())) {
            if (!hover && pMouseX > this.leftPos + 17 && pMouseX < this.leftPos + 17 + 150 && pMouseY > this.topPos + 17 && pMouseY < this.topPos + 17 + 88) {
                List<Slot> slots = this.menu.slots;
                int index = -1;
                for (int i = 0; i < slots.size(); i++) {
                    Slot s = slots.get(i);
                    if (!s.hasItem() && s instanceof IncubatorMenu.IncubatorSlot) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    this.draggedSlotIndex = index;
                    this.draggedX = Mth.floor(pMouseX - leftPos - 8);
                    this.draggedY = Mth.floor(pMouseY - topPos - 8);
                }
            }
        }
        return hover;
    }

    @Override
    public void onClose() {
        super.onClose();
        menu.slots.forEach(
                slot -> {
                    if (slot instanceof IncubatorMenu.IncubatorSlot)
                        Services.PLATFORM.sendToServer(new UpdateIncubatorSlotPacket(menu.getPos(), menu.slots.indexOf(slot), slot.x, slot.y));
                }
        );
    }
}
