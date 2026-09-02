package net.dumbcode.projectnublar.api.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.NublarMath;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AmberItemFunction extends LootItemConditionalFunction {

    public static final MapCodec<AmberItemFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance, AmberItemFunction::new));

    public AmberItemFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    public static Builder<?> amberItem() {
        return simpleBuilder(AmberItemFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        AmberBlock block = (AmberBlock) ((BlockItem) itemStack.getItem()).getBlock();
        Identifier dino = block.getEntityType();
        ItemInstance toolStack = lootContext.getOptionalParameter(LootContextParams.TOOL);
        if (toolStack != null) {
            HolderGetter<net.minecraft.world.item.enchantment.Enchantment> enchantments = lootContext.getResolver().lookupOrThrow(Registries.ENCHANTMENT);
            boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SILK_TOUCH), toolStack) > 0;
            if (!hasSilkTouch) {
                itemStack = new ItemStack(ItemInit.AMBER_ITEM.get());
                DNAData dnaData = new DNAData();
                dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.getValue(dino));
                dnaData.setDnaPercentage(NublarMath.round(Math.pow(lootContext.getRandom().nextDouble(), 0.8d),2));
                itemStack.set(DataComponentInit.DNA_DATA.get(), dnaData);
            }
        }
        return itemStack;
    }

    @Override
    public MapCodec<AmberItemFunction> codec() {
        return CODEC;
    }
}

