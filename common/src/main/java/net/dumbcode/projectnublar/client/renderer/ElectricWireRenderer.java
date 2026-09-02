package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.google.common.reflect.TypeToken;
import com.geckolib.renderer.base.GeoRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dumbcode.projectnublar.block.api.Connection;
import net.dumbcode.projectnublar.block.api.RenderUtils;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.dumbcode.projectnublar.client.renderer.state.NublarBlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 26.2: BERs are extract-then-submit; the animatable is only available during state extraction,
// so the connection geometry data is captured into the render state and submitted from there.
public class ElectricWireRenderer implements BlockEntityRenderer<BlockEntityElectricFence, NublarBlockEntityRenderState> {
    private static final DataTicket<List<Connection.RenderData>> CONNECTION_DATA =
            DataTicket.create("electric_wire_connection_data", new TypeToken<>() {});

    @Override
    public NublarBlockEntityRenderState createRenderState() {
        return new NublarBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityElectricFence blockEntity, NublarBlockEntityRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay damageOverlayState) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, damageOverlayState);
        renderState.addGeckolibData(CONNECTION_DATA,
                blockEntity.getConnections().stream().map(Connection::getRenderData).toList());
    }

    @Override
    public void submit(NublarBlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector renderTasks, CameraRenderState cameraRenderState) {
        List<Connection.RenderData> connections = renderState.getGeckolibData(CONNECTION_DATA);
        if (connections == null) {
            return;
        }
        renderTasks.submitCustomGeometry(poseStack, RenderTypes.leash(), (pose, consumer) -> {
            poseStack.pushPose();
            poseStack.last().set(pose);
            for (Connection.RenderData connection : connections) {
                drawConnection(poseStack, consumer, connection);
            }
            poseStack.popPose();
        });
    }

    private static void drawConnection(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer, Connection.RenderData renderData) {
        RenderUtils.drawSpacedCube(poseStack, consumer, 1, 1, 1, 1, 0x00F000F0, OverlayTexture.NO_OVERLAY,
                renderData.data()[0],
                renderData.data()[1], renderData.data()[2],
                renderData.data()[3], renderData.data()[4],
                renderData.data()[5], renderData.data()[6],
                renderData.data()[7], renderData.data()[8],
                renderData.data()[9], renderData.data()[10],
                renderData.data()[11], renderData.data()[12],
                renderData.data()[13], renderData.data()[14],
                renderData.data()[15], renderData.data()[16],
                renderData.data()[17], renderData.data()[18],
                renderData.data()[19], renderData.data()[20],
                renderData.data()[21], renderData.data()[22],
                renderData.data()[23], renderData.data()[24],
                renderData.data()[25], renderData.data()[26],
                renderData.data()[27], renderData.data()[28],
                renderData.data()[29], renderData.data()[30],
                renderData.data()[31], renderData.data()[32],
                renderData.data()[33], renderData.data()[34],
                renderData.data()[35], renderData.data()[36],
                renderData.data()[37], renderData.data()[38]
        );
    }
}
