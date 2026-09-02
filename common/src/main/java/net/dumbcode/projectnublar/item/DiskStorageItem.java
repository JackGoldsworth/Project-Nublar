package net.dumbcode.projectnublar.item;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiskStorageItem extends Item {
    final int processingTime;

    public DiskStorageItem(Properties properties, int processingTime) {
        super(properties);
        this.processingTime = processingTime;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public static double getGeneCompletion(Genes.Gene gene, ItemStack stack) {
        Map<String, DNAData> storedDna = stack.getOrDefault(DataComponentInit.DISK_DNA.get(), Map.of());
        double totalPercent = 0;
        for (DNAData data : storedDna.values()) {
            if (Genes.GENE_STORAGE.get(gene).stream().map(Pair::getFirst).toList().contains(data.getEntityType())) {
                totalPercent += data.getDnaPercentage();
            }
        }
        if(gene == GeneInit.COLOR.get()){
            Set<DyeColor> blah = new HashSet<>();
            storedDna.values().forEach(data -> {
                blah.add(data.gettFish1());
                blah.add(data.gettFish2());
            });
            return blah.size()/ 16.0;
        }
        return totalPercent / Genes.GENE_STORAGE.get(gene).size();
    }
}
