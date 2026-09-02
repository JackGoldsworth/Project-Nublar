package net.dumbcode.projectnublar.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class GuiHelper {
    // 26.2 port of the old InventoryScreen.renderEntityInInventory convenience overload —
    // vanilla's version became mouse-follow-only, so the plain centered variant lives here.
    // Entity-in-gui is now a pictures-in-picture draw; (x, y) is the box centre and `scale`
    // is GUI pixels per block, as before.
    public static void renderEntityInInventory(GuiGraphicsExtractor graphics, int x, int y, int scale, Quaternionfc rotation, LivingEntity entity) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + 0.0625F, 0.0F);
        graphics.entity(renderState, scale, translation, rotation, null, x - scale, y - scale, x + scale, y + scale);
    }

    public static void blitWithBorder(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder)
    {
        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;

        // Draw Border
        // Top Left
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, leftBorder, topBorder, 256, 256);
        // Top Right
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, 256, 256);
        // Bottom Left
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, 256, 256);
        // Bottom Right
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, 256, 256);

        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++)
        {
            // Top Border
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, 256, 256);
            // Bottom Border
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, 256, 256);

            // Throw in some filler for good measure
            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), 256, 256);
        }

        // Side Borders
        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
        {
            // Left Border
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), 256, 256);
            // Right Border
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), 256, 256);
        }
    }
}
