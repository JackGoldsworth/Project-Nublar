package net.dumbcode.projectnublar.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import net.dumbcode.projectnublar.client.renderer.SimpleGeoItemRenderer;
import net.dumbcode.projectnublar.item.api.MultiBlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class GeoMultiBlockItem extends MultiBlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeoMultiBlockItem(Block block, Properties properties, int rows, int columns, int depth) {
        super(block, properties, rows, columns, depth);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // GeckoLib 5: item renderers are provided via GeoRenderProvider instead of the old
    // initializeClient/BEWLR mixin route.
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<GeoMultiBlockItem> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SimpleGeoItemRenderer<>();
                }
                return this.renderer;
            }
        });
    }
}
