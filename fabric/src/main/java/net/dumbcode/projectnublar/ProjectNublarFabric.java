package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.block.api.BlockConnectableBase;
import net.dumbcode.projectnublar.block.api.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.Connection;
import net.dumbcode.projectnublar.data.BehaviourDataReloadListener;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.data.FossilConfigReloadListener;
import net.dumbcode.projectnublar.data.GeneDataReloadListener;
import net.dumbcode.projectnublar.entity.DeathMessageHandler;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener.SharedState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ProjectNublarFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ProjectNublar.init();

        EntityInit.attributeSuppliers.forEach(
            p -> FabricDefaultAttributeRegistry.register(p.entityTypeSupplier().get(), p.factory().get().build())
        );

        registerReloadListeners();
        registerEvents();
    }

    private static void registerReloadListeners() {
        ResourceManagerHelper manager = ResourceManagerHelper.get(PackType.SERVER_DATA);
        manager.registerReloadListener(wrap(Constants.modLoc("gene_data"), new GeneDataReloadListener()));
        manager.registerReloadListener(wrap(Constants.modLoc("behaviour_data"), new BehaviourDataReloadListener()));
        manager.registerReloadListener(wrap(Constants.modLoc("diet_data"), new DietReloadListener()));
        manager.registerReloadListener(wrap(Constants.modLoc("fossil_config"), new FossilConfigReloadListener()));
    }

    private static IdentifiableResourceReloadListener wrap(Identifier id, PreparableReloadListener delegate) {
        return new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id;
            }

            @Override
            public CompletableFuture<Void> reload(SharedState sharedState, Executor backgroundExecutor, PreparationBarrier barrier, Executor gameExecutor) {
                return delegate.reload(sharedState, backgroundExecutor, barrier, gameExecutor);
            }
        };
    }

    private static void registerEvents() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof Dinosaur dinosaur) {
                DeathMessageHandler.onLivingDeath(dinosaur, source);
            }
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            Direction side = hitResult.getDirection();
            if (level.isClientSide() || player.getItemInHand(hand).isEmpty()
                || player.getItemInHand(hand).getItem() != ItemInit.WIRE_SPOOL.get()) {
                return InteractionResult.PASS;
            }
            BlockEntity tile = level.getBlockEntity(hitResult.getBlockPos().relative(side));
            if (!(tile instanceof ConnectableBlockEntity cb)) {
                return InteractionResult.PASS;
            }
            if (side.getAxis() == Direction.Axis.Y) {
                double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
                Connection ref = null;
                for (Connection connection : cb.getConnections()) {
                    if (connection.isBroken()) {
                        double[] in = connection.getIn();
                        double yin = (in[4] + in[5]) / 2D;
                        if (side == Direction.DOWN == yin > yRef) {
                            yRef = yin;
                            ref = connection;
                        }
                    }
                }
                if (ref != null) {
                    ref.setBroken(false);
                    BlockConnectableBase.placeEffect(player, hand, level, hitResult.getBlockPos());
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
            for (Connection connection : cb.getConnections()) {
                if (connection.isBroken()) {
                    connection.setBroken(false);
                    BlockConnectableBase.placeEffect(player, hand, level, hitResult.getBlockPos());
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }
}
