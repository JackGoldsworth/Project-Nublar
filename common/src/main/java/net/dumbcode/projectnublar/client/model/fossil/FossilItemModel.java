package net.dumbcode.projectnublar.client.model.fossil;

import com.mojang.serialization.MapCodec;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 26.2 dynamic item model for the fossil item: renders an item-layer quad set built from the
 * fossil overlay sprite selected by the stack's DNA_DATA fossil piece (block atlas texture
 * {@code block/fossil_overlay/<folder>/<name>}), scaled down like the 1.20.1 baked model.
 */
public class FossilItemModel implements ItemModel {
    private static final ModelDebugName DEBUG_NAME = () -> "FossilItemModel";

    private final ItemModel.BakingContext context;
    private final Matrix4fc transformation;
    private final ItemTransforms itemTransforms;
    private final Map<FossilPiece, ItemModel> cache = new HashMap<>();

    private FossilItemModel(ItemModel.BakingContext context, Matrix4fc transformation) {
        this.context = context;
        this.transformation = transformation;
        // Source ItemTransforms from the base item model
        this.itemTransforms = context.blockModelBaker().getModel(Identifier.withDefaultNamespace("item/generated")).getTopTransforms();
    }

    private ItemModel modelForPiece(FossilPiece piece) {
        Material material = new Material(Constants.modLoc("block/fossil_overlay/" + piece.folder() + "/" + piece.name()));
        Material.Baked sprite = context.blockModelBaker().materials().get(material, DEBUG_NAME);
        ModelState state = BlockModelRotation.IDENTITY;
        QuadCollection quads = context.blockModelBaker().compute(new ItemModelGenerator.ItemLayerKey(sprite, state, 0));
        ModelRenderProperties renderProperties = new ModelRenderProperties(false, sprite, itemTransforms);
        return new CuboidItemModelWrapper(List.of(), quads, renderProperties, transformation);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        FossilPiece piece = data != null ? data.getFossilPiece() : null;
        if (piece == null) {
            context.missingItemModel(transformation).update(renderState, stack, modelResolver, displayContext, level, owner, seed);
            return;
        }
        cache.computeIfAbsent(piece, this::modelForPiece)
                .update(renderState, stack, modelResolver, displayContext, level, owner, seed);
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return new FossilItemModel(context, transformation);
        }

        @Override
        public void resolveDependencies(net.minecraft.client.resources.model.ResolvableModel.Resolver resolver) {
            // The overlay sprite is chosen at update time, not a static dependency
        }
    }
}
