package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.renderer.GeoBlockRenderer;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.client.model.CarnivoreFeederModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.dumbcode.projectnublar.client.renderer.state.NublarBlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

public class CarnivoreFeederRenderer extends GeoBlockRenderer<DinosaurFeederBlockEntity, NublarBlockEntityRenderState> {
    public CarnivoreFeederRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new CarnivoreFeederModel());
    }

    // GeckoLib 5: the default creates a plain BlockEntityRenderState, which would ClassCastException in addRenderData
    @Override
    public NublarBlockEntityRenderState createRenderState() {
        return new NublarBlockEntityRenderState();
    }

    @Override
    public void addRenderData(DinosaurFeederBlockEntity animatable, @Nullable Void relatedObject, NublarBlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(CarnivoreFeederModel.FEEDER_FULL, animatable.shouldDisplayFood && animatable.shouldDispenseFood);
    }
}
