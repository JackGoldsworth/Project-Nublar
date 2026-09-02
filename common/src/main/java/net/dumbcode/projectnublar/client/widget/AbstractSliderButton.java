package net.dumbcode.projectnublar.client.widget;

//COPYRIGHT OF MOJANG.

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.dumbcode.projectnublar.annotation.OnlyIn.Dist;
import net.dumbcode.projectnublar.annotation.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSliderButton extends AbstractWidget {
    public static final Identifier SLIDER_LOCATION = Identifier.parse("textures/gui/slider.png");
    protected static final int TEXTURE_WIDTH = 200;
    protected static final int TEXTURE_HEIGHT = 20;
    protected static final int TEXTURE_BORDER_X = 20;
    protected static final int TEXTURE_BORDER_Y = 4;
    protected static final int TEXT_MARGIN = 2;
    private static final int HEIGHT = 20;
    private static final int HANDLE_HALF_WIDTH = 4;
    private static final int HANDLE_WIDTH = 8;
    protected double value;
    protected boolean canChangeValue;
    protected boolean dragging;

    public AbstractSliderButton(int x, int y, int width, int height, Component message, double value) {
        super(x, y, width, height, message);
        this.value = value;
    }

    public int getTextureY() {
        int i = this.isFocused() && !this.canChangeValue ? 1 : 0;
        return i * 20;
    }

    public int getHandleTextureY() {
        int i = !this.isHovered && !this.canChangeValue ? 2 : 3;
        return i * 20;
    }

    protected MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", new Object[]{this.getMessage()});
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }

    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiHelper.blitWithBorder(guiGraphics, SLIDER_LOCATION, this.getX(), this.getY(), 0, this.getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);
        GuiHelper.blitWithBorder(guiGraphics, SLIDER_LOCATION, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 0, this.getHandleTextureY(), 8, 20, 200, 20, 2, 3, 2, 2);
        int i = this.active ? 16777215 : 10526880;
        this.extractScrollingStringWithColor(guiGraphics, i);
        this.handleCursor(guiGraphics);
    }

    protected void extractScrollingStringWithColor(GuiGraphicsExtractor guiGraphics, int color) {
        Component colored = this.getMessage().copy().withStyle(style -> style.withColor(color & 0xFFFFFF));
        this.extractScrollingStringOverContents(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), colored, TEXT_MARGIN);
    }

    protected void renderScrollingString(GuiGraphicsExtractor guiGraphics, Font font, int margin, int color) {
        int i = this.getX() + margin;
        int j = this.getX() + this.getWidth() - margin;
        renderScrollingString(guiGraphics, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), color);
    }

    protected void renderScrollingString(GuiGraphicsExtractor guiGraphics, Font font, Component message, int minX, int minY, int maxX, int maxY, int color) {
        Component colored = message.copy().withStyle(style -> style.withColor(color & 0xFFFFFF));
        ActiveTextCollector collector = guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE);
        collector.acceptScrolling(colored, (minX + maxX) / 2, minX, maxX, minY, maxY);
    }

    @Override
    protected void handleCursor(GuiGraphicsExtractor guiGraphics) {
        if (this.isHovered()) {
            guiGraphics.requestCursor(this.isActive() ? (this.dragging ? CursorTypes.RESIZE_EW : CursorTypes.POINTING_HAND) : CursorTypes.NOT_ALLOWED);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.dragging = this.active;
        this.setValueFromMouse(event.x());
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        this.dragging = false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.canChangeValue = false;
        } else {
            InputType inputtype = Minecraft.getInstance().getLastInputType();
            if (inputtype == InputType.MOUSE || inputtype == InputType.KEYBOARD_TAB) {
                this.canChangeValue = true;
            }
        }

    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isSelection()) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        } else {
            if (this.canChangeValue) {
                boolean flag = event.isLeft();
                if (flag || event.isRight()) {
                    float f = flag ? -1.0F : 1.0F;
                    this.setValue(this.value + (double) (f / (float) (this.width - 8)));
                    return true;
                }
            }

            return false;
        }
    }

    private void setValueFromMouse(double mouseX) {
        this.setValue((mouseX - (double) (this.getX() + 4)) / (double) (this.width - 8));
    }

    private void setValue(double value) {
        double d0 = this.value;
        this.value = Mth.clamp(value, (double) 0.0F, (double) 1.0F);
        if (d0 != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        this.setValueFromMouse(event.x());
        super.onDrag(event, dragX, dragY);
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}
