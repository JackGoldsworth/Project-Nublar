package net.dumbcode.projectnublar.init;

import com.mojang.serialization.MapCodec;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.loot.functions.AmberItemFunction;
import net.dumbcode.projectnublar.api.loot.functions.FossilItemFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

public class LootFunctionInit {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Constants.MODID);
    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<FossilItemFunction>> FOSSIL_PART_FUNCTION = FUNCTIONS.register("fossil_part", () -> FossilItemFunction.CODEC);
    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<AmberItemFunction>> AMBER_FUNCTION = FUNCTIONS.register("amber", () -> AmberItemFunction.CODEC);

    public static void registerTo() {
        FUNCTIONS.register();
    }
}
