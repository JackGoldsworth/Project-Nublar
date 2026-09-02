package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::biomeModifiers)
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::configuredFeature)
            .add(Registries.PLACED_FEATURE, ModWorldGenProvider::placedFeatures);
    private static final ResourceKey<BiomeModifier> FOSSIL = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Constants.modLoc( "fossil"));

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MODID));
    }

    public static void biomeModifiers(BootstrapContext<BiomeModifier> context) {

    }

    public static void dimension(BootstrapContext<DimensionType> context) {

    }
    public static void configuredFeature(BootstrapContext<ConfiguredFeature<?, ?>> context) {


    }


    public static void placedFeatures(BootstrapContext<PlacedFeature> context) {

    }


}
