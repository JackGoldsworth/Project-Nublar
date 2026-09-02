package net.dumbcode.projectnublar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.init.RecipeInit;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public class UnincubatedEggRecipe implements CraftingRecipe {
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;

    public UnincubatedEggRecipe(String pGroup, CraftingBookCategory pCategory, ItemStack pResult, NonNullList<Ingredient> pIngredients) {
        this.group = pGroup;
        this.category = pCategory;
        this.result = pResult;
        this.ingredients = pIngredients;
    }

    public UnincubatedEggRecipe(String pGroup, CraftingBookCategory pCategory, ItemStack pResult, List<Ingredient> pIngredients) {
        this.group = pGroup;
        this.category = pCategory;
        this.result = pResult;
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        nonnulllist.addAll(pIngredients);
        this.ingredients = nonnulllist;
    }

    public RecipeSerializer<UnincubatedEggRecipe> getSerializer() {
        return RecipeInit.UNINCUBATED_EGG.get();
    }

    /**
     * Recipes with equal group are combined into one button in the recipe book
     */
    public String group() {
        return this.group;
    }

    public CraftingBookCategory category() {
        return this.category;
    }

    public boolean showNotification() {
        return true;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(CraftingInput pInput, Level pLevel) {
        List<ItemStack> nonEmpty = pInput.items().stream().filter(stack -> !stack.isEmpty()).toList();
        if (nonEmpty.size() != this.ingredients.size()) {
            return false;
        }

        boolean[] used = new boolean[this.ingredients.size()];
        for (ItemStack itemstack : nonEmpty) {
            int matched = -1;
            for (int i = 0; i < this.ingredients.size(); ++i) {
                if (!used[i] && this.ingredients.get(i).test(itemstack)) {
                    matched = i;
                    break;
                }
            }
            if (matched == -1) {
                return false;
            }
            used[matched] = true;
        }

        return true;
    }

    public ItemStack assemble(CraftingInput pContainer) {
        ItemStack result = this.result.copy();
        ItemStack testTubeItem = pContainer.items().stream().filter(itemStack -> itemStack.is(ItemInit.TEST_TUBE_ITEM.get())).findFirst().orElse(ItemStack.EMPTY);
        if (testTubeItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        DinoData dinoData = DinoData.fromStack(testTubeItem);
        dinoData.setIncubationProgress(0);
        dinoData.toStack(result);
        return result;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= this.ingredients.size();
    }

    public PlacementInfo placementInfo() {
        return PlacementInfo.create(this.ingredients);
    }

    public static final MapCodec<UnincubatedEggRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
        CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(recipe -> recipe.category),
        ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
        Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> List.copyOf(recipe.ingredients))
    ).apply(instance, UnincubatedEggRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnincubatedEggRecipe> STREAM_CODEC = StreamCodec.of(
        UnincubatedEggRecipe::write, UnincubatedEggRecipe::read
    );

    private static void write(RegistryFriendlyByteBuf pBuffer, UnincubatedEggRecipe pRecipe) {
        pBuffer.writeUtf(pRecipe.group);
        pBuffer.writeEnum(pRecipe.category);
        pBuffer.writeVarInt(pRecipe.ingredients.size());

        for (Ingredient ingredient : pRecipe.ingredients) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, ingredient);
        }

        ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.result);
    }

    private static UnincubatedEggRecipe read(RegistryFriendlyByteBuf pBuffer) {
        String s = pBuffer.readUtf();
        CraftingBookCategory craftingbookcategory = pBuffer.readEnum(CraftingBookCategory.class);
        int i = pBuffer.readVarInt();
        NonNullList<Ingredient> nonnulllist = NonNullList.create();

        for (int j = 0; j < i; ++j) {
            nonnulllist.add(Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer));
        }

        ItemStack itemstack = ItemStack.STREAM_CODEC.decode(pBuffer);
        return new UnincubatedEggRecipe(s, craftingbookcategory, itemstack, nonnulllist);
    }
}
