package net.dumbcode.projectnublar.platform;

import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.network.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {

    private static IEventBus modBus;

    private final Map<DeferredRegister<?>, net.neoforged.neoforge.registries.DeferredRegister<?>> realRegisters = new IdentityHashMap<>();

    /** Must be called from the mod constructor before ProjectNublar.init(). */
    public static void setModBus(IEventBus bus) {
        modBus = bus;
    }

    @Override
    public <T> void prepareRegister(DeferredRegister<T> register) {
        this.realRegisters.put(register,
            net.neoforged.neoforge.registries.DeferredRegister.create(register.registryKey(), register.modid()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, I extends T> void registerEntry(DeferredRegister<T> register, String name, Supplier<? extends I> supplier, DeferredHolder<T, I> holder) {
        var real = (net.neoforged.neoforge.registries.DeferredRegister<T>) this.realRegisters.get(register);
        var realHolder = real.register(name, supplier);
        holder.setDelegate(realHolder::get);
        holder.setHolderDelegate((Holder<I>) (Holder<?>) realHolder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void bindRegister(DeferredRegister<T> register) {
        if (modBus == null) {
            throw new IllegalStateException("NeoForgePlatformHelper.setModBus was not called before ProjectNublar.init()");
        }
        ((net.neoforged.neoforge.registries.DeferredRegister<T>) this.realRegisters.get(register)).register(modBus);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Registry<T> makeRegistry(DeferredRegister<T> register) {
        return ((net.neoforged.neoforge.registries.DeferredRegister<T>) this.realRegisters.get(register)).makeRegistry(builder -> {});
    }

    @Override
    public ResourceKey<? extends Registry<EntityDataSerializer<?>>> entityDataSerializerRegistryKey() {
        return NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS;
    }

    // --- Networking ---

    private record PendingPacket<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                                StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                BiConsumer<T, PacketContext> handler) {
    }

    private final List<PendingPacket<?>> pendingPackets = new ArrayList<>();

    @Override
    public <T extends CustomPacketPayload> void registerServerboundPacket(CustomPacketPayload.Type<T> type,
                                                                          StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                          BiConsumer<T, PacketContext> handler) {
        this.pendingPackets.add(new PendingPacket<>(type, codec, handler));
    }

    /** Called from the mod entrypoint's RegisterPayloadHandlersEvent listener. */
    public void bindPayloads(PayloadRegistrar registrar) {
        for (PendingPacket<?> pending : this.pendingPackets) {
            registerOne(registrar, pending);
        }
        this.pendingPackets.clear();
    }

    private static <T extends CustomPacketPayload> void registerOne(PayloadRegistrar registrar, PendingPacket<T> pending) {
        registrar.playToServer(pending.type(), pending.codec(),
            (payload, context) -> pending.handler().accept(payload, wrap(context)));
    }

    private static PacketContext wrap(IPayloadContext context) {
        return new PacketContext() {
            @Override
            public net.minecraft.world.entity.player.Player player() {
                return context.player();
            }

            @Override
            public void enqueueWork(Runnable work) {
                context.enqueueWork(work);
            }
        };
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    // --- Menus ---

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createPosMenuType(PosMenuFactory<T> factory) {
        return IMenuTypeExtension.create((containerId, inventory, buf) -> factory.create(containerId, inventory, buf.readBlockPos()));
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, buf -> buf.writeBlockPos(pos));
    }

    // --- Energy ---

    @Override
    public int moveEnergy(Level level, BlockPos pos, Direction face, MachineEnergyHandler source, int amount) {
        EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos, face);
        if (target == null) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int moved = EnergyHandlerUtil.move(new MachineEnergyView(source), target, amount, transaction);
            transaction.commit();
            return moved;
        }
    }
}
