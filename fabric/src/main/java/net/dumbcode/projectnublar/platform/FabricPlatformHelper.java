package net.dumbcode.projectnublar.platform;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.block.api.NublarEnergyBlock;
import net.dumbcode.projectnublar.network.PacketContext;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {

    // Sentinel key for DataSerializerInit: vanilla 26.2 has no serializer registry on the
    // fabric side, so these register through FabricEntityDataRegistry instead.
    private static final ResourceKey<Registry<EntityDataSerializer<?>>> ENTITY_DATA_SERIALIZERS_KEY =
        ResourceKey.createRegistryKey(Constants.modLoc("entity_data_serializers"));

    private final Map<DeferredRegister<?>, Registry<?>> customRegistries = new IdentityHashMap<>();

    @Override
    public <T> void prepareRegister(DeferredRegister<T> register) {
        // fabric registers eagerly at bind time; nothing to prepare
    }

    @Override
    public <T, I extends T> void registerEntry(DeferredRegister<T> register, String name, Supplier<? extends I> supplier, DeferredHolder<T, I> holder) {
        // entries are collected by the DeferredRegister itself and flushed in bindRegister
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void bindRegister(DeferredRegister<T> register) {
        Registry<T> registry = this.customRegistries.containsKey(register)
            ? (Registry<T>) this.customRegistries.get(register)
            : (Registry<T>) BuiltInRegistries.REGISTRY.getValue(register.registryKey().identifier());

        for (DeferredRegister.Entry entry : register.entries()) {
            Identifier id = register.id(entry.name());
            Object value = entry.supplier().get();
            if (register.registryKey().equals(ENTITY_DATA_SERIALIZERS_KEY)) {
                FabricEntityDataRegistry.register(id, (EntityDataSerializer<?>) value);
                entry.bind(value, Holder.direct(value));
            } else {
                Holder.Reference<Object> ref = Registry.registerForHolder((Registry<Object>) registry, id, value);
                entry.bind(value, ref);
            }
        }
    }

    @Override
    public <T> Registry<T> makeRegistry(DeferredRegister<T> register) {
        MappedRegistry<T> registry = new MappedRegistry<>((ResourceKey<Registry<T>>) register.registryKey(), com.mojang.serialization.Lifecycle.stable(), false);
        Registry.<Registry<?>, Registry<T>>register((Registry<Registry<?>>) BuiltInRegistries.REGISTRY, register.registryKey().identifier(), registry);
        this.customRegistries.put(register, registry);
        return registry;
    }

    @Override
    public ResourceKey<? extends Registry<EntityDataSerializer<?>>> entityDataSerializerRegistryKey() {
        return ENTITY_DATA_SERIALIZERS_KEY;
    }

    // --- Networking ---

    @Override
    public <T extends CustomPacketPayload> void registerServerboundPacket(CustomPacketPayload.Type<T> type,
                                                                          StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                          BiConsumer<T, PacketContext> handler) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
            handler.accept(payload, new PacketContext() {
                @Override
                public Player player() {
                    return context.player();
                }

                @Override
                public void enqueueWork(Runnable work) {
                    context.server().execute(work);
                }
            }));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    // --- Menus ---

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createPosMenuType(PosMenuFactory<T> factory) {
        return new ExtendedMenuType<>(factory::create, BlockPos.STREAM_CODEC);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer player) {
                return pos;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return provider.createMenu(containerId, inventory, player);
            }
        });
    }

    // --- Energy ---

    // Fabric has no bundled FE-style energy API in 26.2 (teamreborn energy is optional), so
    // cross-mod energy interop is skipped for now. Own-machine pushing still works.
    @Override
    public int moveEnergy(Level level, BlockPos pos, Direction face, MachineEnergyHandler source, int amount) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof NublarEnergyBlock energyBlock) {
            int accepted = energyBlock.getEnergyHandler().internalInsert(amount, true);
            if (accepted <= 0) {
                return 0;
            }
            int moved = source.internalExtract(accepted, false);
            energyBlock.getEnergyHandler().internalInsert(moved, false);
            return moved;
        }
        return 0;
    }
}
