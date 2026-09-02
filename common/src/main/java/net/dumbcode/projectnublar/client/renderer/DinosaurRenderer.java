package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.dumbcode.projectnublar.client.renderer.state.DinosaurRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DinosaurRenderer extends GeoEntityRenderer<Dinosaur, DinosaurRenderState> {
    private static final com.geckolib.constant.dataticket.DataTicket<Float> RENDER_SCALE =
            com.geckolib.constant.dataticket.DataTicket.create("dinosaur_render_scale", Float.class);

    public DinosaurRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel<Dinosaur> model) {
        super(renderManager, model);
    }

    /// Per-species adult scale offset, added to the size gene value / 100
    protected float adultScaleOffset() {
        return 1F;
    }

    // GeckoLib 5: the animatable is only available during state extraction, so layers (and their
    // textures/colors), the growth scale and the head-bone position are captured here and consumed
    // at submit time.
    private void createLayers(Dinosaur entity) {
        List<DinoLayer> layers = entity.getLayers();
        for (DinoLayer layer : layers) {
            if (layer.getRenderRequirement().apply(entity)) {
                int layerIndex = layers.indexOf(layer) + 1;
                this.withRenderLayer(new SimpleGeoLayerRenderer<>(this, layer.getLayerName()) {
                    @Override
                    public @Nullable Identifier getTextureResource(Dinosaur animatable) {
                        // The render requirement (e.g. the gender gene) can resolve after this layer was
                        // first created, so re-check it per render — otherwise a dino that was assumed
                        // male on its first frame keeps trying to load male-only textures as a female
                        if (!layer.getRenderRequirement().apply(animatable)) {
                            return null;
                        }
                        return layer.getTextureLocation(animatable).orElse(null);
                    }

                    @Override
                    public int getLayerColor(Dinosaur animatable) {
                        return animatable.layerColor(layerIndex, layer);
                    }
                });
            }
        }
    }

    @Override
    public void addRenderData(Dinosaur animatable, @Nullable Void relatedObject, DinosaurRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        if (this.getRenderLayers().isEmpty()) {
            createLayers(animatable);
        }

        float adultScale = ((float) animatable.getDinoData().getGeneValue(GeneInit.SIZE.get()) / 100) + adultScaleOffset();
        float renderScale = switch (animatable.getGrowthStage()) {
            case 1 -> adultScale * 0.25F;
            case 2 -> adultScale * 0.5F;
            case 3 -> adultScale * 0.75F;
            default -> adultScale;
        };
        renderState.addGeckolibData(RENDER_SCALE, renderScale);

        BakedGeoModel bakedModel = this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(renderState));
        bakedModel.getBone("head").ifPresent(head -> {
            Vec3 headPos = animatable.position().add(head.pivotX(), head.pivotY(), head.pivotZ());
            animatable.getEntityData().set(Dinosaur.DINOSAUR_HEAD_POS, headPos.toVector3f());
        });
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<DinosaurRenderState> renderPassInfo, float widthScale, float heightScale) {
        Float renderScale = renderPassInfo.getGeckolibData(RENDER_SCALE);
        if (renderScale != null) {
            super.scaleModelForRender(renderPassInfo, renderScale * widthScale, renderScale * heightScale);
        } else {
            super.scaleModelForRender(renderPassInfo, widthScale, heightScale);
        }
    }

    @Override
    public int getRenderColor(Dinosaur animatable, @Nullable Void relatedObject, float partialTick) {
        // 1.20.1 parity: GL4's Color(int) kept the raw RGB as ARGB (alpha 0), so the cutout base pass
        // discarded itself and the visible skin came purely from the texture layers. Keep that
        // behaviour — an opaque base pass would render the (nonexistent) default entity texture.
        return animatable.layerColor(0, null) & 0xFFFFFF;
    }

    @Override
    public float getMotionAnimThreshold(Dinosaur animatable) {
        return 0.005f;
    }

    @Override
    public DinosaurRenderState createRenderState(Dinosaur animatable, @Nullable Void relatedObject) {
        return new DinosaurRenderState();
    }
}
