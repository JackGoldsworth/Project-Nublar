package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        UnincubatedEggRecipeBuilder.shapeless(RecipeCategory.MISC, ItemInit.UNINCUBATED_EGG.get())
                .requires(ItemInit.ARTIFICIAL_EGG.get())
                .requires(ItemInit.TEST_TUBE_ITEM.get())
                .unlockedBy("has_artificial_egg", has(ItemInit.ARTIFICIAL_EGG.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE, Constants.modLoc("unincubated_egg")));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Project Nublar Recipes";
        }
    }
}
