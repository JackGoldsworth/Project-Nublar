package net.dumbcode.projectnublar.platform;

import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.network.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.BiConsumer;

// Loader-specific hooks for everything common cannot do loader-neutrally.
public interface IPlatformHelper {

    /** Prepares the loader-side backend for a register (called from the DeferredRegister constructor). */
    <T> void prepareRegister(DeferredRegister<T> register);

    /** Registers one entry immediately (NeoForge defers to its own DeferredRegister; fabric collects for bind). */
    <T, I extends T> void registerEntry(DeferredRegister<T> register, String name, java.util.function.Supplier<? extends I> supplier, DeferredHolder<T, I> holder);

    /** Flushes a register's entries into the real registries. Called from each init class' registerTo(). */
    <T> void bindRegister(DeferredRegister<T> register);

    /** Creates the custom registry object backing a modded registry key (gene registry on both loaders). */
    <T> Registry<T> makeRegistry(DeferredRegister<T> register);

    /** Loader-correct registry key for EntityDataSerializer registration
     *  (NeoForge's own registry on NeoForge; a fabric-handled sentinel key on fabric). */
    ResourceKey<? extends Registry<EntityDataSerializer<?>>> entityDataSerializerRegistryKey();

    /** Registers a client-to-server payload type and handler. */
    <T extends CustomPacketPayload> void registerServerboundPacket(CustomPacketPayload.Type<T> type,
                                                                   StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                   BiConsumer<T, PacketContext> handler);

    /** Client-side: sends a payload to the server. */
    void sendToServer(CustomPacketPayload payload);

    /** Creates a menu type whose screen-open data is a BlockPos. */
    <T extends AbstractContainerMenu> MenuType<T> createPosMenuType(PosMenuFactory<T> factory);

    /** Opens a menu on the server thread with a BlockPos as screen-open data. */
    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    /** Pushes up to {@code amount} energy from {@code source} into the energy-capable
     *  block at {@code pos} (from {@code face}). Returns the amount moved. */
    int moveEnergy(net.minecraft.world.level.Level level, BlockPos pos, Direction face, MachineEnergyHandler source, int amount);
}
