package net.dumbcode.projectnublar.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FluidRenderWidget extends AbstractWidget {


    private static final Identifier FLOW_TEXTURE = Identifier.parse("textures/block/water_flow.png");
    private static final Identifier STILL_TEXTURE = Identifier.parse("textures/block/water_still.png");

    private Identifier foreground;
    private float progress = 0;
    private int color;
    private boolean flowDown;
    private boolean flow;

    public FluidRenderWidget(int x, int y, int width, int height, Identifier foreground, int color, boolean flow, boolean flowDown) {
        super(x, y, width, height, Component.empty());
        this.foreground = foreground;
        this.color = color;
        this.flow = flow;
        this.flowDown = flowDown;
    }

    // 26.2: renderWidget is now extractWidgetRenderState; the global setShaderColor tint
    // is gone — blit takes a per-call ARGB color instead
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        long tick = Minecraft.getInstance().level.getGameTime();
        int offset = (int) (tick % 32) * (flowDown ? -1 : 1);
        guiGraphics.enableScissor(getX() + 1, getY() + 1 + ((height - 1) - (int) ((height - 1) * getProgress())), getX() + width - 1, getY() + height - 1);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, flow ? FLOW_TEXTURE : STILL_TEXTURE, getX() + 1, getY() + 1, 0, offset * 32, width - 2, height - 2, 32, 1024, 32, 1024, color);
        guiGraphics.disableScissor();
        if(foreground != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, foreground, getX(), getY(), 0, 0, width, height, width, height);
        }
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
