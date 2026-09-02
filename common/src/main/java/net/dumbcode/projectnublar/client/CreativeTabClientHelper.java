package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

// Client-only helper for CreativeTabInit. The displayItems lambdas only run on the
// client, but class verification of CreativeTabInit would eagerly load ClientLevel
// (via Minecraft.getInstance().level) and explode on a dedicated server — keeping
// the reference here defers the load until the tab actually renders.
public class CreativeTabClientHelper {

    public static ItemStack createSyringeStackIfLiving(Identifier entry) {
        if (BuiltInRegistries.ENTITY_TYPE.getValue(entry).create(Minecraft.getInstance().level, EntitySpawnReason.COMMAND) instanceof LivingEntity) {
            ItemStack stack = new ItemStack(ItemInit.SYRINGE.get());
            DNAData dnaData = new DNAData();
            dnaData.setEntityType(BuiltInRegistries.ENTITY_TYPE.getValue(entry));
            dnaData.setDnaPercentage(0.5);
            stack.set(DataComponentInit.DNA_DATA.get(), dnaData);
            return stack;
        }
        return ItemStack.EMPTY;
    }
}
