package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.init.TagInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LeavesBlock;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModTagProvider {

    public static class ItemTag extends TagsProvider<Item>{

        public ItemTag(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Registries.ITEM, registries, Constants.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(TagInit.SUGAR, Items.SUGAR);
            populateTag(TagInit.BONE_MATTER, Items.BONE_MEAL);
            populateTag(TagInit.FEEDER_MEAT, Items.PORKCHOP,Items.BEEF,Items.CHICKEN,Items.MUTTON,Items.COOKED_PORKCHOP,Items.COOKED_BEEF,Items.COOKED_MUTTON,Items.COOKED_CHICKEN);
            tag(TagInit.PLANT_MATTER).addTag(ItemTags.LEAVES);
            populateTag(TagInit.PLANT_MATTER, ComposterBlock.COMPOSTABLES.keySet().stream().filter(item -> !(item instanceof LeavesBlock)).toArray(ItemLike[]::new));

        }

        public void populateTag(TagKey<Item> tag, ItemLike... items){
            for (ItemLike item : items) {
                tag(tag).add(BuiltInRegistries.ITEM.getResourceKey(item.asItem()).get());
            }
        }
    }
    public static class BlockTag extends TagsProvider<Block>{

        public BlockTag(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Registries.BLOCK, registries, Constants.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {

        }
        public <T extends Block> void populateTag(TagKey<Block> tag, Supplier<?>... items){
            for (Supplier<?> item : items) {
                tag(tag).add(BuiltInRegistries.BLOCK.getResourceKey((Block)item.get()).get());
            }
        }
    }
    public static class EntityTypeTag extends TagsProvider<EntityType<?>>{

        public EntityTypeTag(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Registries.ENTITY_TYPE, registries, Constants.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(TagInit.EMBRYO_ENTITY, EntityTypes.PARROT,EntityTypes.CHICKEN);

        }
        public void populateTag(TagKey<EntityType<?>> tag, EntityType<?>... items){
            for (EntityType<?> item : items) {
                tag(tag).add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(item).get());
            }
        }
    }
}
