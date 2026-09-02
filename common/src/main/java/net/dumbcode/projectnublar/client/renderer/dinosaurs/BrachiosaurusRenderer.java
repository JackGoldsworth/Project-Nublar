package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import com.geckolib.model.DefaultedEntityGeoModel;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BrachiosaurusRenderer extends DinosaurRenderer {

    public BrachiosaurusRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel<Dinosaur> model) {
        super(renderManager, model);
    }

    @Override
    protected float adultScaleOffset() {
        return 2.5F;
    }
}
