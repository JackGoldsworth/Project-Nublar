package net.dumbcode.projectnublar.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.GeneData;
import net.dumbcode.projectnublar.api.Genes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class GeneDataReloadListener extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    private static final FileToIdConverter CONVERTER = FileToIdConverter.json("gene_data");

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<Identifier, JsonElement> map = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : CONVERTER.listMatchingResources(pResourceManager).entrySet()) {
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                map.put(CONVERTER.fileToId(entry.getKey()), JsonParser.parseReader(reader));
            } catch (Exception e) {
                Constants.LOG.error("Failed to parse gene data file: {} - Error: {}", entry.getKey(), e.getMessage());
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pObject.forEach((resourceLocation, jsonElement) -> {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(resourceLocation.toString())).orElse(null);
            if (type != null) {
                GeneData geneData = GeneData.CODEC.decode(JsonOps.INSTANCE,jsonElement).result().get().getFirst();
                GeneData.register(type,geneData);
                geneData.genes().forEach((gene, value) -> {
                    Genes.addToGene(gene, type, value);
                });
            }
        });
    }
}
