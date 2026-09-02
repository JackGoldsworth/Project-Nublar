package net.dumbcode.projectnublar.network.c2s;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.dumbcode.projectnublar.network.PacketContext;

public record UpdateIncubatorPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<UpdateIncubatorPacket> TYPE = new Type<>(Constants.modLoc("update_incubator"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateIncubatorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateIncubatorPacket::pos, UpdateIncubatorPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateIncubatorPacket payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.pos()) instanceof IncubatorBlockEntity entity) {
                entity.updateBlock();
            }
        });
    }
}
