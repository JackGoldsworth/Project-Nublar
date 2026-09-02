package net.dumbcode.projectnublar.network.c2s;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.dumbcode.projectnublar.network.PacketContext;

public record UpdateIncubatorSlotPacket(BlockPos pos, int index, int x, int y) implements CustomPacketPayload {
    public static final Type<UpdateIncubatorSlotPacket> TYPE = new Type<>(Constants.modLoc("update_incubator_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateIncubatorSlotPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateIncubatorSlotPacket::pos,
            ByteBufCodecs.INT, UpdateIncubatorSlotPacket::index,
            ByteBufCodecs.INT, UpdateIncubatorSlotPacket::x,
            ByteBufCodecs.INT, UpdateIncubatorSlotPacket::y,
            UpdateIncubatorSlotPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateIncubatorSlotPacket payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.pos()) instanceof IncubatorBlockEntity entity) {
                entity.updateSlot(payload.index(), payload.x(), payload.y());
                entity.updateBlock();
            }
        });
    }
}
