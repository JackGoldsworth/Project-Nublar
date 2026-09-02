package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.google.common.reflect.TypeToken;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.api.Connection;
import net.dumbcode.projectnublar.block.api.RenderUtils;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.client.model.FencePostModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.dumbcode.projectnublar.client.renderer.state.NublarBlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectricFenceRenderer extends GeoBlockRenderer<BlockEntityElectricFencePole, NublarBlockEntityRenderState> {
    private static final DataTicket<List<Connection.RenderData>> CONNECTION_DATA =
            DataTicket.create("electric_fence_connection_data", new TypeToken<>() {});
    private static final DataTicket<Float> POLE_ROTATION = DataTicket.create("electric_fence_pole_rotation", Float.class);
    private static final DataTicket<Boolean> RENDER_MODEL = DataTicket.create("electric_fence_render_model", Boolean.class);

    public ElectricFenceRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new FencePostModel());
    }

    // GeckoLib 5: the default creates a plain BlockEntityRenderState, which would ClassCastException in addRenderData
    @Override
    public NublarBlockEntityRenderState createRenderState() {
        return new NublarBlockEntityRenderState();
    }

    @Override
    public void addRenderData(BlockEntityElectricFencePole animatable, @Nullable Void relatedObject, NublarBlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(animatable.getBlockState().getBlock());
        renderState.addGeckolibData(FencePostModel.MODEL_ID,
                // GeckoLib 5: models live under assets/<ns>/geckolib/models/ and are cached by their stripped path
                Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath()));
        renderState.addGeckolibData(FencePostModel.TEXTURE_ID,
                Identifier.fromNamespaceAndPath(blockId.getNamespace(), "textures/block/" + blockId.getPath() + ".png"));
        renderState.addGeckolibData(CONNECTION_DATA,
                animatable.getConnections().stream().map(Connection::getRenderData).toList());
        renderState.addGeckolibData(POLE_ROTATION, (float) animatable.getCachedRotation());
        renderState.addGeckolibData(RENDER_MODEL,
                animatable.getBlockState().getValue(((ElectricFencePostBlock) animatable.getBlockState().getBlock()).getIndexProperty()) == 0);
    }

    // GeckoLib 5: the model pose is adjusted at submit time via the render pass info. The
    // translate/rotate/translate-back keeps the stack position while rotating the model about
    // its center (the old push/render/pop wrapper).
    @Override
    public void adjustRenderPose(RenderPassInfo<NublarBlockEntityRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);
        if (Boolean.TRUE.equals(renderPassInfo.getGeckolibData(RENDER_MODEL))) {
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(renderPassInfo.getGeckolibData(POLE_ROTATION)));
            poseStack.translate(-0.5, -0.5, -0.5);
        }
    }

    @Override
    public void postRenderPass(RenderPassInfo<NublarBlockEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);
        List<Connection.RenderData> connections = renderPassInfo.getGeckolibData(CONNECTION_DATA);
        if (connections == null) {
            return;
        }
        PoseStack poseStack = renderPassInfo.poseStack();
        renderTasks.submitCustomGeometry(poseStack, RenderTypes.leash(), (pose, consumer) -> {
            poseStack.pushPose();
            poseStack.last().set(pose);
            for (Connection.RenderData connection : connections) {
                RenderUtils.drawSpacedCube(poseStack, consumer, 1, 1, 1, 1, 0x00F000F0, OverlayTexture.NO_OVERLAY,
                        connection.data()[0],
                        connection.data()[1], connection.data()[2],
                        connection.data()[3], connection.data()[4],
                        connection.data()[5], connection.data()[6],
                        connection.data()[7], connection.data()[8],
                        connection.data()[9], connection.data()[10],
                        connection.data()[11], connection.data()[12],
                        connection.data()[13], connection.data()[14],
                        connection.data()[15], connection.data()[16],
                        connection.data()[17], connection.data()[18],
                        connection.data()[19], connection.data()[20],
                        connection.data()[21], connection.data()[22],
                        connection.data()[23], connection.data()[24],
                        connection.data()[25], connection.data()[26],
                        connection.data()[27], connection.data()[28],
                        connection.data()[29], connection.data()[30],
                        connection.data()[31], connection.data()[32],
                        connection.data()[33], connection.data()[34],
                        connection.data()[35], connection.data()[36],
                        connection.data()[37], connection.data()[38]
                );
            }
            poseStack.popPose();
        });
    }
}
