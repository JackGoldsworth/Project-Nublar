package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.dumbcode.projectnublar.client.renderer.state.NublarBlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

public class SequencerRenderer extends GeoBlockRenderer<SequencerBlockEntity, NublarBlockEntityRenderState> {
    private static final DataTicket<Boolean> HAS_COMPUTER = DataTicket.create("sequencer_has_computer", Boolean.class);
    private static final DataTicket<Boolean> HAS_DOOR = DataTicket.create("sequencer_has_door", Boolean.class);
    private static final DataTicket<Boolean> HAS_SCREEN = DataTicket.create("sequencer_has_screen", Boolean.class);

    public SequencerRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Constants.modLoc("sequencer")));
    }

    // GeckoLib 5: the default creates a plain BlockEntityRenderState, which would ClassCastException in addRenderData
    @Override
    public NublarBlockEntityRenderState createRenderState() {
        return new NublarBlockEntityRenderState();
    }

    @Override
    public void addRenderData(SequencerBlockEntity animatable, @Nullable Void relatedObject, NublarBlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(HAS_COMPUTER, animatable.isHasComputer());
        renderState.addGeckolibData(HAS_DOOR, animatable.isHasDoor());
        renderState.addGeckolibData(HAS_SCREEN, animatable.isHasScreen());
    }

    // GeckoLib 5: bone visibility is applied via the BoneSnapshots of the render pass instead of
    // per-bone renderRecursively overrides (that hook no longer exists).
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<NublarBlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        for (GeoBone bone : renderPassInfo.model().boneLookup().get().values()) {
            String boneName = bone.name();
            if (boneName.contains("computer")) {
                hideIfAbsent(renderPassInfo, snapshots, bone, HAS_COMPUTER);
            } else if (boneName.contains("Door")) {
                hideIfAbsent(renderPassInfo, snapshots, bone, HAS_DOOR);
            } else if (boneName.contains("monitor")) {
                hideIfAbsent(renderPassInfo, snapshots, bone, HAS_SCREEN);
            }
        }
    }

    private static void hideIfAbsent(RenderPassInfo<NublarBlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots,
                                     GeoBone bone, DataTicket<Boolean> ticket) {
        if (!Boolean.TRUE.equals(renderPassInfo.getGeckolibData(ticket))) {
            snapshots.get(bone).skipRender(true).skipChildrenRender(true);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
