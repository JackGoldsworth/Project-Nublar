package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.block.LowSecurityElectricFencePostBlock;
import net.dumbcode.projectnublar.block.*;
import net.dumbcode.projectnublar.block.api.EnumConnectionType;
import net.dumbcode.projectnublar.block.entity.*;
import net.dumbcode.projectnublar.item.GeoMultiBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.dumbcode.projectnublar.platform.DeferredBlock;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

import java.util.Set;
import java.util.function.Function;

public class BlockInit {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MODID);


     public static FossilCollection TYRANNOSAURUS_REX_FOSSILS = FossilCollection.create(EntityInit.TYRANNOSAURUS_REX.getId());
     public static FossilCollection TRICERATOPS_FOSSILS = FossilCollection.create(EntityInit.TRICERATOPS.getId());

    public static DeferredBlock<Block> PROCESSOR = registerBlock("processor", props -> new ProcessorBlock(props.noOcclusion(),3,2, 2), block-> props-> new GeoMultiBlockItem(block.get(),props,3,2, 2));
    public static DeferredBlock<Block> SEQUENCER = registerBlock("sequencer", props -> new SequencerBlock(props.noOcclusion(),2,2, 2), block-> props-> new GeoMultiBlockItem(block.get(),props,2,2, 2));
    public static DeferredBlock<Block> EGG_PRINTER = registerBlock("egg_printer", props -> new EggPrinterBlock(props.noOcclusion(),1,2, 1), block-> props-> new GeoMultiBlockItem(block.get(),props,1,2, 1));
    public static DeferredBlock<Block> INCUBATOR = registerBlock("incubator", props -> new IncubatorBlock(props.noOcclusion(),2,2, 1), block-> props-> new GeoMultiBlockItem(block.get(),props,2,2, 1));
    public static DeferredBlock<Block> ELECTRIC_FENCE = registerBlock("electric_fence", props -> new ElectricFenceBlock(props.noLootTable().noOcclusion()));
    public static DeferredBlock<Block> LOW_SECURITY_ELECTRIC_FENCE_POST = registerBlock("low_security_electric_fence_post", props -> new LowSecurityElectricFencePostBlock(props.noOcclusion(), EnumConnectionType.LOW_SECURITY));
    public static DeferredBlock<Block> HIGH_SECURITY_ELECTRIC_FENCE_POST = registerBlock("high_security_electric_fence_post", props -> new HighSecurityElectricFencePostBlock(props.noOcclusion(), EnumConnectionType.HIGH_SECURITY));
    public static DeferredBlock<Block> COAL_GENERATOR = registerBlock("coal_generator", props-> new GeneratorBlock(props,256,16,0));
    public static DeferredBlock<Block> CREATIVE_GENERATOR = registerBlock("creative_generator", props-> new GeneratorBlock(props,99999,99999,0));

    //Feeders
    public static DeferredBlock<Block> CARNIVORE_FEEDER_ONE = registerBlock("carnivore_feeder_one", props -> new DinosaurFeederBlock(props.noOcclusion(),"feeder/meat/feeder_one"));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<DinosaurFeederBlockEntity>> FEEDER_BLOCK_ENTITY = BLOCK_ENTITIES.register("carrnivore_feeder_one",() -> new BlockEntityType<>(DinosaurFeederBlockEntity::new, Set.of(CARNIVORE_FEEDER_ONE.get())));
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessorBlockEntity>> PROCESSOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("processor", () -> new BlockEntityType<>(ProcessorBlockEntity::new, Set.of(PROCESSOR.get())));
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SequencerBlockEntity>> SEQUENCER_BLOCK_ENTITY = BLOCK_ENTITIES.register("sequencer", () -> new BlockEntityType<>(SequencerBlockEntity::new, Set.of(SEQUENCER.get())));
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<EggPrinterBlockEntity>> EGG_PRINTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("egg_printer", () -> new BlockEntityType<>(EggPrinterBlockEntity::new, Set.of(EGG_PRINTER.get())));
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("incubator", () -> new BlockEntityType<>(IncubatorBlockEntity::new, Set.of(INCUBATOR.get())));
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityElectricFence>> ELECTRIC_FENCE_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_fence", () -> new BlockEntityType<>(BlockEntityElectricFence::new, Set.of(BlockInit.ELECTRIC_FENCE.get())));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityElectricFencePole>> ELECTRIC_FENCE_POST_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_fence_pole", () -> new BlockEntityType<>(BlockEntityElectricFencePole::new, Set.of(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get(),BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get())));
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR = BLOCK_ENTITIES.register("coal_generator", ()-> new BlockEntityType<>(GeneratorBlockEntity::new, Set.of(COAL_GENERATOR.get(), CREATIVE_GENERATOR.get())));

    public static void registerTo() {
        BLOCKS.register();
        BLOCK_ENTITIES.register();
    }

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> block) {
        var reg = BLOCKS.registerBlock(name, block);
        ItemInit.ITEMS.registerSimpleBlockItem(name, reg);
        return reg;
    }

    // For blocks that build their own Properties (e.g. fossils copying stone characteristics)
    public static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, java.util.function.Supplier<T> block) {
        var reg = BLOCKS.register(name, block);
        ItemInit.ITEMS.registerSimpleBlockItem(name, reg);
        return reg;
    }

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> block, Function<DeferredBlock<T>, Function<Item.Properties, ? extends BlockItem>> item) {
        var reg = BLOCKS.registerBlock(name, block);
        ItemInit.ITEMS.registerItem(name, item.apply(reg));
        return reg;
    }
}
