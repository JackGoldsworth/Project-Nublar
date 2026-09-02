package net.dumbcode.projectnublar.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.resources.Identifier;

// NOTE: currently unreferenced by any live code. The 1.20.1 no-op getTextureResource/
// setCustomAnimations overrides and the `turnsHead` ctor have no GeckoLib 5 equivalent.
public class DinosaurGeoModel extends DefaultedEntityGeoModel<Dinosaur> {

    public DinosaurGeoModel(Identifier assetSubpath) {
        super(assetSubpath);
    }
}
