package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.platform.MachineEnergyView;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Constants.MODID)
public class CommonModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        EntityInit.attributeSuppliers.forEach(p -> event.put(p.entityTypeSupplier().get(), p.factory().get().build()));
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockInit.GENERATOR.get(), (be, side) -> new MachineEnergyView(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockInit.PROCESSOR_BLOCK_ENTITY.get(), (be, side) -> new MachineEnergyView(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockInit.SEQUENCER_BLOCK_ENTITY.get(), (be, side) -> new MachineEnergyView(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockInit.INCUBATOR_BLOCK_ENTITY.get(), (be, side) -> new MachineEnergyView(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), (be, side) -> new MachineEnergyView(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(), (be, side) -> new MachineEnergyView(be.getEnergyHandler()));
    }
}
