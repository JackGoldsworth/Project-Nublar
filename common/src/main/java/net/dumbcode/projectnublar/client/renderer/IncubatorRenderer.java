package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.dumbcode.projectnublar.client.renderer.state.NublarBlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class IncubatorRenderer extends GeoBlockRenderer<IncubatorBlockEntity, NublarBlockEntityRenderState> {
    private static final DataTicket<Boolean> HAS_NEST = DataTicket.create("incubator_has_nest", Boolean.class);
    private static final DataTicket<Boolean> HAS_LID = DataTicket.create("incubator_has_lid", Boolean.class);
    private static final DataTicket<Boolean> HAS_BASE = DataTicket.create("incubator_has_base", Boolean.class);
    private static final DataTicket<Boolean> HAS_ARM = DataTicket.create("incubator_has_arm", Boolean.class);

    public IncubatorRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Constants.modLoc("incubator")));
    }

    // GeckoLib 5: the default creates a plain BlockEntityRenderState, which would ClassCastException in addRenderData
    @Override
    public NublarBlockEntityRenderState createRenderState() {
        return new NublarBlockEntityRenderState();
    }

    @Override
    public void addRenderData(IncubatorBlockEntity animatable, @Nullable Void relatedObject, NublarBlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(HAS_NEST, !animatable.getNestStack().isEmpty());
        renderState.addGeckolibData(HAS_LID, !animatable.getLidStack().isEmpty());
        renderState.addGeckolibData(HAS_BASE, !animatable.getBaseStack().isEmpty());
        renderState.addGeckolibData(HAS_ARM, !animatable.getArmStack().isEmpty());
    }

    // GeckoLib 5: bone visibility is applied via the BoneSnapshots of the render pass instead of
    // per-bone renderRecursively overrides (that hook no longer exists).
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<NublarBlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        hideIfAbsent(renderPassInfo, snapshots, "nest", HAS_NEST);
        hideIfAbsent(renderPassInfo, snapshots, "cover", HAS_LID);
        hideIfAbsent(renderPassInfo, snapshots, "arm_base", HAS_BASE);
        hideIfAbsent(renderPassInfo, snapshots, "RoboticHand1", HAS_ARM);
    }

    private static void hideIfAbsent(RenderPassInfo<NublarBlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots,
                                     String boneName, DataTicket<Boolean> ticket) {
        if (!Boolean.TRUE.equals(renderPassInfo.getGeckolibData(ticket))) {
            snapshots.get(boneName).ifPresent(bone -> bone.skipRender(true).skipChildrenRender(true));
        }
    }

    @Override
    public @Nullable RenderType getRenderType(NublarBlockEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }
}
