package net.dumbcode.projectnublar.api.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;


public class FossilItemFunction extends LootItemConditionalFunction {

    public static final MapCodec<FossilItemFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance, FossilItemFunction::new));

    public FossilItemFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> fossilItem() {
        return simpleBuilder(FossilItemFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        FossilBlock block = (FossilBlock) ((BlockItem) itemStack.getItem()).getBlock();
        Identifier dino = block.getEntityType();
        FossilPiece piece = block.getFossilPiece();
        Quality quality = block.getQuality();
        ItemInstance toolStack = lootContext.getOptionalParameter(LootContextParams.TOOL);
        if (toolStack != null) {
            HolderGetter<Enchantment> enchantments = lootContext.getResolver().lookupOrThrow(Registries.ENCHANTMENT);
            int i = EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.FORTUNE), toolStack);
            boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SILK_TOUCH), toolStack) > 0;
            if (quality == Quality.NONE) {
                quality = Quality.FRAGMENTED;
                for (int j = 0; j <= i; ++j) {
                    WeightedList.Builder<Quality> builder = WeightedList.builder();
                 //   builder.add(Quality.FRAGMENTED, FossilsConfig.INSTANCE.fragmented.weight().get());
                 //   builder.add(Quality.POOR, FossilsConfig.INSTANCE.poor.weight().get());
                 //   builder.add(Quality.COMMON, FossilsConfig.INSTANCE.common.weight().get());
                 //   builder.add(Quality.PRISTINE, FossilsConfig.INSTANCE.pristine.weight().get());
                    WeightedList<Quality> weightedrandomlist = builder.build();
                    if (!weightedrandomlist.isEmpty()) {
                        Optional<Quality> newQuality = weightedrandomlist.getRandom(lootContext.getRandom());
                        if (newQuality.isPresent() && newQuality.get().getValue() > quality.getValue()) {
                            quality = newQuality.get();
                        }
                    }
                }
            }
            if (!hasSilkTouch) {
                itemStack = new ItemStack(ItemInit.FOSSIL_ITEM.get());
                DNAData dnaData = new DNAData();
                dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.getValue(dino));
                dnaData.setQuality(quality);
                dnaData.setFossilPiece(piece);
                itemStack.set(DataComponentInit.DNA_DATA.get(), dnaData);
            } else {
               itemStack = new ItemStack(FossilCollection.COLLECTIONS.get(dino.toString()).fossilblocks().get(block.getBase()).get(quality).get(piece).get());
            }

        }
        return itemStack;
    }

    @Override
    public MapCodec<FossilItemFunction> codec() {
        return CODEC;
    }
}

