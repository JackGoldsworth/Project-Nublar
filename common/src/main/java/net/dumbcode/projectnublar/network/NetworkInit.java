package net.dumbcode.projectnublar.network;

import net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.dumbcode.projectnublar.platform.Services;

public class NetworkInit {
    public static void registerPackets() {
        Services.PLATFORM.registerServerboundPacket(UpdateEditInfoPacket.TYPE, UpdateEditInfoPacket.STREAM_CODEC, UpdateEditInfoPacket::handle);
        Services.PLATFORM.registerServerboundPacket(UpdateIncubatorSlotPacket.TYPE, UpdateIncubatorSlotPacket.STREAM_CODEC, UpdateIncubatorSlotPacket::handle);
        Services.PLATFORM.registerServerboundPacket(UpdateIncubatorPacket.TYPE, UpdateIncubatorPacket.STREAM_CODEC, UpdateIncubatorPacket::handle);
    }
}
