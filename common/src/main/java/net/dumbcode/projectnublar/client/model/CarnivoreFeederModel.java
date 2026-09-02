package net.dumbcode.projectnublar.client.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.minecraft.resources.Identifier;

public class CarnivoreFeederModel extends DefaultedBlockGeoModel<DinosaurFeederBlockEntity> {
    // GeckoLib 5: the model receives the render state, not the animatable, so the full/empty
    // texture choice is captured into the render state by the renderer (see
    // CarnivoreFeederRenderer) and read here.
    public static final DataTicket<Boolean> FEEDER_FULL = DataTicket.create("carnivore_feeder_full", Boolean.class);

    private final Identifier FEEDER_MODEL = buildFormattedModelPath(Constants.modLoc("carnivore_feeder_one"));
    private final Identifier FEEDER_FULL_TEXTURE = Constants.modLoc("textures/block/feeder/meat/feeder_one/full.png");
    private final Identifier FEEDER_EMPTY_TEXTURE = Constants.modLoc("textures/block/feeder/meat/feeder_one/empty.png");
    private final Identifier FEEDER_ANIMATIONS = buildFormattedAnimationPath(Constants.modLoc("carnivore_feeder_one"));

    public CarnivoreFeederModel() {
        super(Constants.modLoc("carnivore_feeder_one"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return FEEDER_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Boolean.TRUE.equals(renderState.getGeckolibData(FEEDER_FULL)) ? FEEDER_FULL_TEXTURE : FEEDER_EMPTY_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DinosaurFeederBlockEntity animatable) {
        return FEEDER_ANIMATIONS;
    }
}
