package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.platform.DeferredRegister;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class FeatureInit {
    public static DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Constants.MODID);

    public static void registerTo() {
        FEATURES.register();
    }
}