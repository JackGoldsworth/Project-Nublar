package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

/**
 * GeckoLib 5 port of the old per-bone/whole-model texture layer.
 *
 * <p>GeckoLib 5 is render-state based: the animatable is only available during state extraction, so
 * the layer texture and color are captured into the render state there ({@link #addRenderData}) and
 * consumed at submission time ({@link #submitRenderTask}).</p>
 *
 * <p>The old double {@code reRender} (tinted pass + untinted pass over the same layer texture) is
 * preserved as two ordered custom-geometry submissions.</p>
 */
public abstract class SimpleGeoLayerRenderer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    private final String layerName;
    private final DataTicket<Identifier> textureTicket;
    private final DataTicket<Integer> colorTicket;

    public SimpleGeoLayerRenderer(GeoRenderer<T, O, R> renderer, String layerName) {
        super(renderer);
        this.layerName = layerName;
        this.textureTicket = DataTicket.create("simple_geo_layer_texture_" + layerName, Identifier.class);
        this.colorTicket = DataTicket.create("simple_geo_layer_color_" + layerName, Integer.class);
    }

    public String getLayerName() {
        return this.layerName;
    }

    public GeoModel<T> getGeoModel() {
        return this.getRenderer().getGeoModel();
    }

    /**
     * The texture this layer renders the model with, or {@code null} to skip the layer entirely.
     * Only called during state extraction, while the animatable is still available.
     */
    public @Nullable Identifier getTextureResource(T animatable) {
        return null;
    }

    /** The tint applied to the first (tinted) render pass. */
    public int getLayerColor(T animatable) {
        return -1;
    }

    @Override
    public void addRenderData(T animatable, @Nullable O relatedObject, R renderState, float partialTick) {
        Identifier texture = this.getTextureResource(animatable);
        if (texture != null) {
            renderState.addGeckolibData(this.textureTicket, texture);
            renderState.addGeckolibData(this.colorTicket, this.getLayerColor(animatable));
        }
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender()) {
            return;
        }

        Identifier texture = renderPassInfo.getGeckolibData(this.textureTicket);
        if (texture == null) {
            return;
        }

        RenderType renderType = RenderTypes.entityTranslucent(texture);
        int color = renderPassInfo.getOrDefaultGeckolibData(this.colorTicket, -1);
        this.render(renderPassInfo, renderTasks, renderType, color);
    }

    public void render(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks, RenderType renderType, int color) {
        // Tinted pass, then the untinted pass on top of it (matching the old double reRender)
        this.submitModelPass(renderPassInfo, renderTasks.order(1), renderType, color);
        this.submitModelPass(renderPassInfo, renderTasks.order(2), renderType, -1);
    }

    private void submitModelPass(RenderPassInfo<R> renderPassInfo, OrderedSubmitNodeCollector renderTasks, RenderType renderType, int renderColor) {
        renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            renderPassInfo.poseStack().pushPose();
            renderPassInfo.poseStack().last().set(pose);
            renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, vertexConsumer,
                    renderPassInfo.packedLight(), renderPassInfo.packedOverlay(), renderColor));
            renderPassInfo.poseStack().popPose();
        });
    }
}
