package net.dumbcode.projectnublar.client.widget;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ParentWidget<T extends GuiEventListener> extends AbstractWidget implements ContainerEventHandler {

    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    public final List<Renderable> renderables = Lists.newArrayList();
    public final List<Renderable> renderableNoScissor = Lists.newArrayList();
    @javax.annotation.Nullable
    private GuiEventListener focused;
    private boolean isDragging;
    public T parent;
    private int scissorsTopYOffset = 0;
    private int scissorsBottomYOffset = 0;
    private int scissorsLeftXOffset = 0;
    private int scissorsRightXOffset = 0;


    public ParentWidget(@Nullable T parent, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.parent = parent;
        init(false);
    }

    public boolean doesScissor() {
        return false;
    }

    public abstract void init(boolean rebuild);

    protected abstract void renderBackground(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick);

    protected abstract void renderForeground(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick);

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        if (doesScissor()) {
//            pGuiGraphicsExtractor.enableScissor(getX(), getY() + getScissorsTopYOffset(), getX() + this.width, getY() + this.height);
            pGuiGraphicsExtractor.enableScissor(getX() + getScissorsLeftXOffset(), getY() + getScissorsTopYOffset(), getX() + this.width - getScissorsRightXOffset(), getY() + this.height - getScissorsBottomYOffset());
        }
        this.renderBackground(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        for (Renderable renderable : this.renderables) {
            renderable.extractRenderState(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        }
        this.renderForeground(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        if (doesScissor())
            pGuiGraphicsExtractor.disableScissor();
        this.renderAfterScissor(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
    }

    public int getScissorsTopYOffset() {
        return scissorsTopYOffset;
    }
    public int getScissorsBottomYOffset() {
        return scissorsBottomYOffset;
    }
    public int getScissorsLeftXOffset() {
        return scissorsLeftXOffset;
    }
    public int getScissorsRightXOffset() {
        return scissorsRightXOffset;
    }

    public void setScissorsTopYOffset(int scissorsTopYOffset) {
        this.scissorsTopYOffset = scissorsTopYOffset;
    }

    public void renderAfterScissor(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        for (Renderable renderable : this.renderableNoScissor) {
            renderable.extractRenderState(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
        }
        for(GuiEventListener renderable : this.children){
            if(renderable instanceof TooltipRenderer && pMouseX >= getX() && pMouseX <= getX() + width && pMouseY >= getY() && pMouseY <= getY() + height){
                ((TooltipRenderer) renderable).renderTooltip(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableNoScissorWidget(T pWidget) {
        this.renderableNoScissor.add(pWidget);
        return this.addWidget(pWidget);
    }
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T pWidget) {
        this.renderables.add(pWidget);
        return this.addWidget(pWidget);
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T pListener) {
        this.children.add(pListener);
        this.narratables.add(pListener);
        return pListener;
    }

    protected <T extends Renderable> T addRenderableOnly(T pRenderable) {
        this.renderables.add(pRenderable);
        return pRenderable;
    }

    protected void removeWidget(GuiEventListener pListener) {
        if (pListener instanceof Renderable) {
            this.renderables.remove((Renderable) pListener);
        }

        if (pListener instanceof NarratableEntry) {
            this.narratables.remove((NarratableEntry) pListener);
        }

        this.children.remove(pListener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.renderableNoScissor.clear();
        this.children.clear();
        this.narratables.clear();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    public final void setDragging(boolean pDragging) {
        this.isDragging = pDragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@javax.annotation.Nullable GuiEventListener pListener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (pListener != null) {
            pListener.setFocused(true);
        }

        this.focused = pListener;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (this.active && this.visible) {
            if(this.isMouseOver(event.x(), event.y())){
                for (GuiEventListener guieventlistener : this.children()) {
                    if (guieventlistener.mouseClicked(event, doubleClick)) {
                        this.setFocused(guieventlistener);
                        if (event.button() == 0) {
                            this.setDragging(true);
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }



    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        this.setDragging(false);
        return this.getChildAt(event.x(), event.y()).filter((p_94708_) -> {
            return p_94708_.mouseReleased(event);
        }).isPresent();
    }

    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double pDragX, double pDragY) {
        return this.getFocused() != null && this.isDragging() && event.button() == 0 ? this.getFocused().mouseDragged(event, pDragX, pDragY) : false;
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pDelta) {
        return this.getChildAt(pMouseX, pMouseY).filter((p_94693_) -> {
            return p_94693_.mouseScrolled(pMouseX, pMouseY, pScrollX, pDelta);
        }).isPresent();
    }

    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        return this.getFocused() != null && this.getFocused().keyPressed(event);
    }

    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        return this.getFocused() != null && this.getFocused().keyReleased(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        return this.getFocused() != null && this.getFocused().charTyped(event);
    }

    public void rebuild() {
        this.children.clear();
        this.narratables.clear();
        this.renderables.clear();
        this.renderableNoScissor.clear();
        this.init(true);
    }
}
