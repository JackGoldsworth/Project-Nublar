package net.dumbcode.projectnublar.network;

import net.minecraft.world.entity.player.Player;

// Loader-neutral stand-in for the payload handler context. Mirrors the
// NeoForge IPayloadContext surface the packet handlers actually use.
public interface PacketContext {

    Player player();

    void enqueueWork(Runnable work);
}
