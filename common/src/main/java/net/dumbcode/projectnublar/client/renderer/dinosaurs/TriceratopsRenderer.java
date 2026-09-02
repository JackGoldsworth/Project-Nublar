package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import com.geckolib.model.DefaultedEntityGeoModel;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TriceratopsRenderer extends DinosaurRenderer {

    public TriceratopsRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel<Dinosaur> model) {
        super(renderManager, model);
    }

    @Override
    protected float adultScaleOffset() {
        return 2F;
    }
}
