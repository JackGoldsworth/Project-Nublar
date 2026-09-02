package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

public class RecipeInit {
    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MODID);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UnincubatedEggRecipe>> UNINCUBATED_EGG = RECIPE_SERIALIZERS.register("unincubated_egg",
        () -> new RecipeSerializer<>(UnincubatedEggRecipe.MAP_CODEC, UnincubatedEggRecipe.STREAM_CODEC));

    public static void registerTo() {
        RECIPE_SERIALIZERS.register();
    }
}
