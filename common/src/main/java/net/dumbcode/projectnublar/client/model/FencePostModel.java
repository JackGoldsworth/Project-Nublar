package net.dumbcode.projectnublar.client.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.minecraft.resources.Identifier;

// GeckoLib 5: model hooks receive the render state, not the animatable, so the per-post model and
// texture paths are captured into the render state by ElectricFenceRenderer and read here.
public class FencePostModel extends GeoModel<BlockEntityElectricFencePole> {
    public static final DataTicket<Identifier> MODEL_ID = DataTicket.create("fence_post_model_id", Identifier.class);
    public static final DataTicket<Identifier> TEXTURE_ID = DataTicket.create("fence_post_texture_id", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(MODEL_ID);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(TEXTURE_ID);
    }

    @Override
    public Identifier getAnimationResource(BlockEntityElectricFencePole animatable) {
        return null;
    }
}
