package net.dumbcode.projectnublar.client.model.testtube;

import com.mojang.serialization.MapCodec;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 26.2 dynamic item model for the test tube item: a base test tube sprite (full or empty
 * depending on whether the stack carries DNA/Dino data) plus an optional dino overlay layer
 * from {@code item/dino_overlay/<path>}, both scaled down like the 1.20.1 baked model.
 */
public class TestTubeItemModel implements ItemModel {
    private static final ModelDebugName DEBUG_NAME = () -> "TestTubeItemModel";

    private final ItemModel.BakingContext context;
    private final Matrix4fc transformation;
    private final ItemTransforms itemTransforms;
    private final List<ItemModel> cache = new ArrayList<>();

    private TestTubeItemModel(ItemModel.BakingContext context, Matrix4fc transformation) {
        this.context = context;
        this.transformation = transformation;
        // Source ItemTransforms from the base item model
        this.itemTransforms = context.blockModelBaker().getModel(Identifier.withDefaultNamespace("item/generated")).getTopTransforms();
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        DinoData dinoData = stack.get(DataComponentInit.DINO_DATA.get());
        boolean full = data != null || dinoData != null;

        List<ItemModel> subModels;
        if (full) {
            if (cache.isEmpty()) {
                Material baseMaterial = new Material(Constants.modLoc("item/test_tube_full"));
                Material.Baked baseSprite = context.blockModelBaker().materials().get(baseMaterial, DEBUG_NAME);
                ModelState state = BlockModelRotation.IDENTITY;
                ModelRenderProperties renderProperties = new ModelRenderProperties(false, baseSprite, itemTransforms);
                QuadCollection baseQuads = context.blockModelBaker().compute(new ItemModelGenerator.ItemLayerKey(baseSprite, state, 0));
                subModels = new ArrayList<>();
                subModels.add(new CuboidItemModelWrapper(List.of(), baseQuads, renderProperties, transformation));

                Identifier overlay = overlayTexture(data, dinoData);
                if (overlay != null) {
                    Material overlayMaterial = new Material(overlay);
                    Material.Baked overlaySprite = context.blockModelBaker().materials().get(overlayMaterial, DEBUG_NAME);
                    QuadCollection overlayQuads = context.blockModelBaker().compute(new ItemModelGenerator.ItemLayerKey(overlaySprite, state, 1));
                    subModels.add(new CuboidItemModelWrapper(List.of(), overlayQuads, renderProperties, transformation));
                }
                cache.addAll(subModels);
            } else {
                subModels = cache;
            }
        } else {
            Material baseMaterial = new Material(Constants.modLoc("item/test_tube"));
            Material.Baked baseSprite = context.blockModelBaker().materials().get(baseMaterial, DEBUG_NAME);
            ModelState state = BlockModelRotation.IDENTITY;
            ModelRenderProperties renderProperties = new ModelRenderProperties(false, baseSprite, itemTransforms);
            QuadCollection baseQuads = context.blockModelBaker().compute(new ItemModelGenerator.ItemLayerKey(baseSprite, state, 0));
            subModels = List.of(new CuboidItemModelWrapper(List.of(), baseQuads, renderProperties, transformation));
        }
        new CompositeModel(subModels).update(renderState, stack, modelResolver, displayContext, level, owner, seed);
    }

    @Nullable
    private static Identifier overlayTexture(DNAData data, DinoData dinoData) {
        if (data != null && data.getNameSpace() != null && data.getPath() != null) {
            return Identifier.fromNamespaceAndPath(data.getNameSpace(), "item/dino_overlay/" + data.getPath());
        }
        if (dinoData != null && dinoData.getNameSpace() != null && dinoData.getPath() != null) {
            return Identifier.fromNamespaceAndPath(dinoData.getNameSpace(), "item/dino_overlay/" + dinoData.getPath());
        }
        return null;
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return new TestTubeItemModel(context, transformation);
        }

        @Override
        public void resolveDependencies(net.minecraft.client.resources.model.ResolvableModel.Resolver resolver) {
            // The overlay sprite is chosen at update time, not a static dependency
        }
    }
}
