package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.client.model.fossil.FossilItemModel;
import net.dumbcode.projectnublar.client.model.testtube.TestTubeItemModel;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.dumbcode.projectnublar.platform.DeferredHolder;

import java.util.stream.Stream;

public class ModBlockStateProvider extends ModelProvider {

    private static final TextureSlot SLOT_0 = TextureSlot.create("0");
    private static final TextureSlot SLOT_1 = TextureSlot.create("1");

    public ModBlockStateProvider(PackOutput generator) {
        super(generator, Constants.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators gen, ItemModelGenerators itemGen) {
        ExtendedModelTemplate fossilTemplate = ExtendedModelTemplateBuilder.builder()
                .parent(Constants.modLoc("block/fossil_base"))
                .requiredTextureSlot(SLOT_0)
                .requiredTextureSlot(SLOT_1)
                .build();
        ExtendedModelTemplate particleTemplate = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .build();

        FossilCollection.COLLECTIONS.forEach((s, fossilCollection) -> {
            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                qualityMap.forEach((quality, fossilPieceRegistryObjectMap) -> {
                    fossilPieceRegistryObjectMap.forEach((fossilPiece, blockRegistryObject) -> {
                        try {
                            simpleBlock(gen, fossilTemplate, SLOT_0, SLOT_1, blockRegistryObject.get(), block,
                                    "block/fossil_overlay/" + ((FossilBlock) blockRegistryObject.get()).getFossilPiece().folder() + "/" + ((FossilBlock) blockRegistryObject.get()).getFossilPiece().name());
                        } catch (Exception ignored) {
                        }
                    });
                });
            });
            fossilCollection.amberBlocks().forEach((block, blockRegistryObject) -> {
                try {
                    simpleBlock(gen, fossilTemplate, SLOT_0, SLOT_1, blockRegistryObject.get(), block, "block/fossil_overlay/amber/amber");
                } catch (Exception ignored) {
                }
            });
        });
        particleBlock(gen, particleTemplate, BlockInit.CARNIVORE_FEEDER_ONE.get(), "block/feeder/meat/feeder_one/empty");
        particleBlock(gen, particleTemplate, BlockInit.PROCESSOR.get(), "block/processor");
        particleBlock(gen, particleTemplate, BlockInit.SEQUENCER.get(), "block/sequencer");
        particleBlock(gen, particleTemplate, BlockInit.EGG_PRINTER.get(), "block/egg_printer");
        particleBlock(gen, particleTemplate, BlockInit.INCUBATOR.get(), "block/incubator");
        // 26.2: datagen now validates that every block has a blockstate — these are all
        // BER-drawn, so they only need a (particle-only) placeholder model
        particleBlock(gen, particleTemplate, BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get(), "block/low_security_electric_fence_post");
        particleBlock(gen, particleTemplate, BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get(), "block/high_security_electric_fence_post");
        particleBlock(gen, particleTemplate, BlockInit.ELECTRIC_FENCE.get(), "block/low_security_electric_fence_post");
        particleBlock(gen, particleTemplate, BlockInit.COAL_GENERATOR.get(), "block/processor");
        particleBlock(gen, particleTemplate, BlockInit.CREATIVE_GENERATOR.get(), "block/processor");

        registerItemModels(gen, itemGen);
    }

    // 26.2 item models live in items/<name>.json definitions (generated via ItemModelOutput)
    private void registerItemModels(BlockModelGenerators gen, ItemModelGenerators itemGen) {
        Stream.of(
                        ItemInit.IRON_FILTER,
                        ItemInit.IRON_COMPUTER_CHIP,
                        ItemInit.IRON_TANK_UPGRADE,
                        ItemInit.GOLD_FILTER,
                        ItemInit.GOLD_COMPUTER_CHIP,
                        ItemInit.GOLD_TANK_UPGRADE,
                        ItemInit.DIAMOND_FILTER,
                        ItemInit.DIAMOND_COMPUTER_CHIP,
                        ItemInit.DIAMOND_TANK_UPGRADE,
                        ItemInit.HARD_DRIVE,
                        ItemInit.SSD,
                        ItemInit.SEQUENCER_COMPUTER,
                        ItemInit.SEQUENCER_DOOR,
                        ItemInit.SEQUENCER_SCREEN,
                        ItemInit.CRACKED_ARTIFICIAL_EGG,
                        ItemInit.ARTIFICIAL_EGG,
                        ItemInit.SMALL_CONTAINER_UPGRADE,
                        ItemInit.LARGE_CONTAINER_UPGRADE,
                        ItemInit.WARM_BULB,
                        ItemInit.WARMER_BULB,
                        ItemInit.HOT_BULB,
                        ItemInit.INCUBATOR_ARM,
                        ItemInit.INCUBATOR_LID,
                        ItemInit.INCUBATOR_ARM_BASE,
                        ItemInit.INCUBATOR_NEST,
                        ItemInit.UNINCUBATED_EGG,
                        ItemInit.INCUBATED_EGG,
                        ItemInit.WIRE_SPOOL
                )
                .map(java.util.function.Supplier::get)
                .forEach(item -> itemGen.generateFlatItem(item, ModelTemplates.FLAT_ITEM));

        // 26.2: datagen validates that every item has an item-model definition — these were
        // never in the 1.20.1 provider (which didn't validate)
        Stream.of(
                        ItemInit.AMBER_ITEM,
                        ItemInit.DEV_FILTER,
                        ItemInit.DEV_COMPUTER_CHIP,
                        ItemInit.DEV_SSD,
                        ItemInit.DEV_BULB,
                        ItemInit.IRON_PLANT_TANK,
                        ItemInit.GOLD_PLANT_TANK,
                        ItemInit.LEVELING_SENSOR
                )
                .map(java.util.function.Supplier::get)
                .forEach(item -> itemGen.generateFlatItem(item, ModelTemplates.FLAT_ITEM));

        // Machine/fence-post items use flat sprites like 1.20.1 (their block models are
        // particle-only placeholders for the GeckoLib BERs, so parenting to them renders nothing).
        // PROCESSOR is excluded: its 1.20.1 item model was builtin/entity (BEWLR, dead in 26.2),
        // so the flat sprite replaces it (main/resources' hand-written processor.json is gone).
        Stream.of(BlockInit.SEQUENCER, BlockInit.EGG_PRINTER, BlockInit.INCUBATOR,
                        BlockInit.COAL_GENERATOR, BlockInit.CREATIVE_GENERATOR,
                        BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST, BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST)
                .forEach(block -> itemGen.generateFlatItem(block.get().asItem(), ModelTemplates.FLAT_ITEM));

        // CARNIVORE_FEEDER_ONE has a hand-written Blockbench item model in main/resources — don't
        // generate a colliding flat model, just point the definition at the existing model
        itemGen.itemModelOutput.accept(BlockInit.CARNIVORE_FEEDER_ONE.get().asItem(),
                ItemModelUtils.plainModel(Constants.modLoc("item/carnivore_feeder_one")));

        // Dynamic fossil / test tube models (registered in ClientModEvents via RegisterItemModelsEvent)
        itemGen.itemModelOutput.accept(ItemInit.FOSSIL_ITEM.get(), new FossilItemModel.Unbaked());
        itemGen.itemModelOutput.accept(ItemInit.TEST_TUBE_ITEM.get(), new TestTubeItemModel.Unbaked());

        // DEV_STICK keeps the 1.20.1 parent-only model (item/stick)
        ExtendedModelTemplate stickTemplate = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("item/stick"))
                .build();
        Identifier stickModel = stickTemplate.create(Constants.modLoc("item/dev_stick"), new TextureMapping(), gen.modelOutput);
        itemGen.itemModelOutput.accept(ItemInit.DEV_STICK.get(), ItemModelUtils.plainModel(stickModel));

        // TODO(port 3.6): syringe "filled" conditional item model (was ItemProperties.register + override predicates
        // on the DNA data component; 26.2 needs a ConditionalItemModelProperty against DataComponentInit.DNA_DATA)
        itemGen.generateFlatItem(ItemInit.SYRINGE.get(), ModelTemplates.FLAT_ITEM);
    }

    // Two-slot model parented to block/fossil_base: "0" = base block texture, "1" = fossil overlay
    private void simpleBlock(BlockModelGenerators gen, ExtendedModelTemplate template,
                             TextureSlot slot0, TextureSlot slot1, Block block, Block base, String overlay) {
        Identifier modelId = Constants.modLoc("block/" + getName(block));
        TextureMapping mapping = new TextureMapping()
                .put(slot0, baseTexture(base))
                .put(slot1, new Material(Constants.modLoc(overlay)));
        template.create(modelId, mapping, gen.modelOutput);
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(modelId)))));
        gen.registerSimpleItemModel(block, modelId);
    }

    // Model with only a particle texture (machines render via GeckoLib)
    private void particleBlock(BlockModelGenerators gen, ExtendedModelTemplate template, Block block, String particle) {
        Identifier modelId = Constants.modLoc("block/" + getName(block));
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, new Material(Constants.modLoc(particle)));
        template.create(modelId, mapping, gen.modelOutput);
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(modelId)))));
    }

    private static Material baseTexture(Block block) {
        return new Material(Identifier.withDefaultNamespace("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath()));
    }

    protected String getName(Block item) {
        return BuiltInRegistries.BLOCK.getKey(item).getPath();
    }

}
