package net.dumbcode.projectnublar.network.c2s;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.init.DataSerializerInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.dumbcode.projectnublar.network.PacketContext;

public record UpdateEditInfoPacket(DinoData info, BlockPos pos) implements CustomPacketPayload {
    public static final Type<UpdateEditInfoPacket> TYPE = new Type<>(Constants.modLoc("update_edit_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEditInfoPacket> STREAM_CODEC = StreamCodec.composite(
            DataSerializerInit.DINO_DATA_STREAM_CODEC, UpdateEditInfoPacket::info,
            BlockPos.STREAM_CODEC, UpdateEditInfoPacket::pos,
            UpdateEditInfoPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateEditInfoPacket payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.pos()) instanceof SequencerBlockEntity sequencer) {
                sequencer.setDinoData(payload.info());
            }
        });
    }
}
