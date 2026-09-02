package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.dumbcode.projectnublar.client.renderer.state.NublarBlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

public class ProcessorRenderer extends GeoBlockRenderer<ProcessorBlockEntity, NublarBlockEntityRenderState> {
    private static final DataTicket<Integer> TANK_TIER = DataTicket.create("processor_tank_tier", Integer.class);

    public ProcessorRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Constants.modLoc("processor")));
    }

    // GeckoLib 5: the default creates a plain BlockEntityRenderState, which would ClassCastException in addRenderData
    @Override
    public NublarBlockEntityRenderState createRenderState() {
        return new NublarBlockEntityRenderState();
    }

    @Override
    public void addRenderData(ProcessorBlockEntity animatable, @Nullable Void relatedObject, NublarBlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(TANK_TIER, animatable.getMaxFluidLevel());
    }

    // GeckoLib 5: bone visibility is applied via the BoneSnapshots of the render pass instead of
    // per-bone renderRecursively overrides (that hook no longer exists).
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<NublarBlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        Integer tier = renderPassInfo.getGeckolibData(TANK_TIER);
        if (tier == null) {
            return;
        }
        switch (tier) {
            case 8000 -> hideBones(snapshots, false, false, false);
            case 4000 -> hideBones(snapshots, false, false, true);
            case 3000 -> hideBones(snapshots, false, true, true);
            default -> hideBones(snapshots, true, true, true);
        }
    }

    private static void hideBones(BoneSnapshots snapshots, boolean hideIron, boolean hideGold, boolean hideDiamond) {
        hideBone(snapshots, "iron_tanks", hideIron);
        hideBone(snapshots, "gold_tanks", hideGold);
        hideBone(snapshots, "diamond_tanks", hideDiamond);
    }

    private static void hideBone(BoneSnapshots snapshots, String boneName, boolean hide) {
        if (hide) {
            snapshots.get(boneName).ifPresent(bone -> bone.skipRender(true).skipChildrenRender(true));
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
