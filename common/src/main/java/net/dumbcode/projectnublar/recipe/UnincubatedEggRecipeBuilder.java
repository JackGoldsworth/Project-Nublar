package net.dumbcode.projectnublar.recipe;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;

import net.dumbcode.projectnublar.init.RecipeInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class UnincubatedEggRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Item result;
   private final int count;
   private final List<Ingredient> ingredients = Lists.newArrayList();
   private final RecipeUnlockAdvancementBuilder advancement = new RecipeUnlockAdvancementBuilder();
   @Nullable
   private String group;

   public UnincubatedEggRecipeBuilder(RecipeCategory pCategory, ItemLike pResult, int pCount) {
      this.category = pCategory;
      this.result = pResult.asItem();
      this.count = pCount;
   }

   /**
    * Creates a new builder for a shapeless recipe.
    */
   public static UnincubatedEggRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult) {
      return new UnincubatedEggRecipeBuilder(pCategory, pResult, 1);
   }

   /**
    * Creates a new builder for a shapeless recipe.
    */
   public static UnincubatedEggRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult, int pCount) {
      return new UnincubatedEggRecipeBuilder(pCategory, pResult, pCount);
   }

   /**
    * Adds an ingredient that can be any item in the given tag.
    */
   public UnincubatedEggRecipeBuilder requires(TagKey<Item> pTag) {
      return this.requires(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(pTag)));
   }

   /**
    * Adds an ingredient of the given item.
    */
   public UnincubatedEggRecipeBuilder requires(ItemLike pItem) {
      return this.requires(pItem, 1);
   }

   /**
    * Adds the given ingredient multiple times.
    */
   public UnincubatedEggRecipeBuilder requires(ItemLike pItem, int pQuantity) {
      for(int i = 0; i < pQuantity; ++i) {
         this.requires(Ingredient.of(pItem));
      }

      return this;
   }

   /**
    * Adds an ingredient.
    */
   public UnincubatedEggRecipeBuilder requires(Ingredient pIngredient) {
      return this.requires(pIngredient, 1);
   }

   /**
    * Adds an ingredient multiple times.
    */
   public UnincubatedEggRecipeBuilder requires(Ingredient pIngredient, int pQuantity) {
      for(int i = 0; i < pQuantity; ++i) {
         this.ingredients.add(pIngredient);
      }

      return this;
   }

   public UnincubatedEggRecipeBuilder unlockedBy(String pCriterionName, Criterion<?> pCriterion) {
      this.advancement.unlockedBy(pCriterionName, pCriterion);
      return this;
   }

   public UnincubatedEggRecipeBuilder group(@Nullable String pGroupName) {
      this.group = pGroupName;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public ResourceKey<Recipe<?>> defaultId() {
      return RecipeBuilder.getDefaultRecipeId(new ItemStackTemplate(this.result, this.count));
   }

   public void save(RecipeOutput pRecipeOutput, ResourceKey<Recipe<?>> pRecipeId) {
      CraftingBookCategory craftingbookcategory = RecipeBuilder.determineCraftingBookCategory(this.category);
      UnincubatedEggRecipe recipe = new UnincubatedEggRecipe(this.group == null ? "" : this.group, craftingbookcategory,
         new ItemStackTemplate(this.result, this.count).create(), List.copyOf(this.ingredients));
      pRecipeOutput.accept(pRecipeId, recipe, this.advancement.build(pRecipeOutput, pRecipeId, this.category));
   }
}
