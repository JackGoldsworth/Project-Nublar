package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.animatable.GeoItem;
import com.geckolib.renderer.GeoItemRenderer;
import net.dumbcode.projectnublar.client.model.SimpleGeoItemModel;
import net.minecraft.world.item.Item;

public class SimpleGeoItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    public SimpleGeoItemRenderer() {
        super(new SimpleGeoItemModel<>());
    }
}
