package net.dumbcode.projectnublar.client.model;

import com.geckolib.animatable.GeoItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class SimpleGeoItemModel<T extends Item & GeoItem> extends GeoModel<T> {
    protected final Map<Identifier, Identifier> geoCache = new HashMap<>();
    protected final Map<Identifier, Identifier> textureCache = new HashMap<>();

    // GeckoLib 5: model hooks receive the render state, so the rendered item is read from it
    // (populated by GeoItemRenderer.CURRENT_ITEM during state capture).
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        Item item = renderState.getGeckolibData(GeoItemRenderer.CURRENT_ITEM);
        if (item == null) {
            return null;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        // GeckoLib 5: models live under assets/<ns>/geckolib/models/ and are cached by their stripped path
        return this.geoCache.computeIfAbsent(id, k -> Identifier.fromNamespaceAndPath(k.getNamespace(), (item instanceof BlockItem ? "block/" : "item/") + k.getPath()));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        Item item = renderState.getGeckolibData(GeoItemRenderer.CURRENT_ITEM);
        if (item == null) {
            return null;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return this.textureCache.computeIfAbsent(id, k -> Identifier.fromNamespaceAndPath(k.getNamespace(), "textures/" + (item instanceof BlockItem ? "block/" : "item/") + k.getPath() + ".png"));
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return null;
    }
}
