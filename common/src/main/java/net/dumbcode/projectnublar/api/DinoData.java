package net.dumbcode.projectnublar.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DinoData {

    public static final Codec<DinoData> CODEC = CompoundTag.CODEC.xmap(DinoData::fromNBT, DinoData::toNBT);

    public static final StreamCodec<io.netty.buffer.ByteBuf, DinoData> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(DinoData::fromNBT, DinoData::toNBT);

    public static final StreamCodec<io.netty.buffer.ByteBuf, Map<String, DNAData>> MAP_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, DNAData.STREAM_CODEC);

    public double basePercentage;
    public double incubationProgress = -1;
    public int incubationTimeLeft = -1;
    private EntityType<?> baseDino = null;
    private Map<EntityInfo, Double> entityPercentages = new HashMap<>();
    private Map<Genes.Gene, Double> advancedGenes = new HashMap<>();
    private Map<Genes.Gene, Double> finalGenes = new HashMap<>();
    private List<Integer> layerColors = Arrays.asList(
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF
    );
    private Identifier textureLocation = null;

    public DinoData() {
    }

    public Integer getLayerColor(int layer) {
        return layerColors.get(layer);
    }

    public List<Integer> getLayerColors() {
        return layerColors;
    }

    public static DinoData fromStack(ItemStack stack) {
        DinoData data = stack.get(DataComponentInit.DINO_DATA.get());
        return data == null ? new DinoData() : data;
    }

    public Map<EntityInfo, Double> getEntityPercentages() {
        return entityPercentages;
    }


    public void addEntity(EntityInfo type, double percentage) {
        entityPercentages.put(type, percentage);
    }

    public void removeEntity(EntityType<?> type) {
        entityPercentages.remove(type);
    }

    public void setBasePercentage(double basePercentage) {
        this.basePercentage = basePercentage;
    }

    public void setGeneValue(Genes.Gene gene, double value) {
        advancedGenes.put(gene, value);
    }

    public void addGeneValue(Genes.Gene gene, double value) {
        if (!advancedGenes.containsKey(gene)) {
            advancedGenes.put(gene, value);
        } else {
            advancedGenes.put(gene, advancedGenes.get(gene) + value);
        }
    }

    public double getBasePercentage() {
        return basePercentage;
    }

    public EntityType<?> getBaseDino() {
        return baseDino;
    }

    public double getEntityPercentage(EntityInfo type) {
        return entityPercentages.getOrDefault(type, 0D);
    }

    public void createToolTip(List<Component> components) {
//        components.add(baseDino.getDescription());
        if (incubationProgress != -1) {
            components.add(Component.literal(("Incubation Progress: " + (int) NublarMath.round(incubationProgress * 100, 0)) + "%"));
        }
        if (incubationTimeLeft != -1) {
            components.add(Component.literal(StringUtil.formatTickDuration(incubationTimeLeft, 20.0F)));
        }
        if (finalGenes.isEmpty()) {
            finalizeGenes();
        }
        finalGenes.forEach((gene, value) -> components.add(gene.getTooltip(value)));
    }

    public void finalizeGenes() {
        finalGenes.clear();
        for (Genes.Gene gene : GeneInit.getList()) {
            double value = getFinalGeneValue(gene);
            if (value != 0) {
                finalGenes.put(gene, value);
            }
        }
    }

    public void setBaseDino(EntityType<?> baseDino) {
        this.baseDino = baseDino;
    }

    public double getGeneValue(Genes.Gene gene) {
        if (advancedGenes.containsKey(gene)) {
            return advancedGenes.get(gene);
        }
        double value = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            if (GeneData.getData(entry.getKey().type) != null) {
                if (GeneData.getData(entry.getKey().type).genes().containsKey(gene)) {
                    value += GeneData.getData(entry.getKey().type).genes().get(gene) * (entry.getValue() * 2);
                }
            }
        }

        return value;
    }

    public double getFinalGeneValue(Genes.Gene gene) {
        if (finalGenes.containsKey(gene)) {
            return finalGenes.get(gene);
        }
        if (advancedGenes.containsKey(gene)) {
            finalGenes.put(gene, advancedGenes.get(gene));
            return finalGenes.get(gene);
        }
        double value = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            if (GeneData.getData(entry.getKey().type) != null) {
                if (GeneData.getData(entry.getKey().type).genes().containsKey(gene)) {
                    value += GeneData.getData(entry.getKey().type).genes().get(gene) * (entry.getValue() * 2);
                }
            }
        }
        return value;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("basePercentage", basePercentage);
        CompoundTag entityTag = new CompoundTag();
        int i = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            CompoundTag entityInfo = new CompoundTag();
            entityInfo.putString("type", BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey().type).toString());
            if (entry.getKey().variant != null) {
                entityInfo.putString("variant", entry.getKey().variant);
            }
            entityInfo.putDouble("percentage", entry.getValue());
            entityTag.put("entity_" + i, entityInfo);
            i++;
        }
        tag.put("entityPercentages", entityTag);
        CompoundTag geneTag = new CompoundTag();
        for (Map.Entry<Genes.Gene, Double> entry : advancedGenes.entrySet()) {
            geneTag.putDouble(entry.getKey().name(), entry.getValue());
        }
        tag.put("genes", geneTag);
        tag.putString("baseDino", BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).toString());
        tag.putDouble("incubationProgress", incubationProgress);
        tag.putInt("incubationTimeLeft", incubationTimeLeft);
        return tag;
    }

    public static DinoData fromNBT(CompoundTag tag) {
        double basePercentage = tag.getDoubleOr("basePercentage", 0);
        Map<EntityInfo, Double> entityPercentages = new HashMap<>();
        tag.getCompound("entityPercentages").ifPresent(entityTag -> {
            for (String key : entityTag.keySet()) {
                entityTag.getCompound(key).ifPresent(entityInfo -> {
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entityInfo.getStringOr("type", "minecraft:pig")));
                    String variant = entityInfo.getString("variant").orElse(null);
                    double percentage = entityInfo.getDoubleOr("percentage", 0);
                    entityPercentages.put(new EntityInfo(type, variant), percentage);
                });
            }
        });
        DinoData data = new DinoData();
        tag.getCompound("genes").ifPresent(geneTag -> {
            for (String key : geneTag.keySet()) {
                data.advancedGenes.put(Genes.byName(key), geneTag.getDoubleOr(key, 0));
            }
        });
        data.basePercentage = basePercentage;
        data.entityPercentages = entityPercentages;
        if (tag.contains("baseDino"))
            data.baseDino = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(tag.getStringOr("baseDino", "minecraft:pig")));
        if (tag.contains("incubationProgress"))
            data.incubationProgress = tag.getDoubleOr("incubationProgress", -1);
        if (tag.contains("incubationTimeLeft"))
            data.incubationTimeLeft = tag.getIntOr("incubationTimeLeft", -1);
        return data;
    }

    public DinoData copy() {
        DinoData data = new DinoData();
        data.advancedGenes = new HashMap<>(advancedGenes);
        data.entityPercentages = new HashMap<>(entityPercentages);
        data.basePercentage = basePercentage;
        return data;
    }

    public String getNameSpace() {
        if (baseDino == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).getNamespace();
    }

    public String getPath() {
        if (baseDino == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).getPath();
    }

    public MutableComponent getFormattedType() {
        return MutableComponent.create(baseDino.getDescription().getContents());
    }

    public void setIncubationProgress(double i) {
        incubationProgress = i;
    }

    public void toStack(ItemStack stack) {
        stack.set(DataComponentInit.DINO_DATA.get(), this);
    }

    public double getIncubationProgress() {
        return incubationProgress;
    }

    public void setIncubationTimeLeft(int v) {
        incubationTimeLeft = v;
    }

    public record EntityInfo(EntityType<?> type, @Nullable String variant) {
        public static Codec<EntityInfo> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(EntityInfo::type),
                        Codec.STRING.optionalFieldOf("variant", null).forGetter(EntityInfo::variant)
                ).apply(instance, EntityInfo::new)
        );

    }

    // 26.2: data components must implement equals/hashCode (registration validation crashes otherwise)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DinoData other)) return false;
        return Double.compare(basePercentage, other.basePercentage) == 0
                && Double.compare(incubationProgress, other.incubationProgress) == 0
                && incubationTimeLeft == other.incubationTimeLeft
                && java.util.Objects.equals(baseDino, other.baseDino)
                && entityPercentages.equals(other.entityPercentages)
                && advancedGenes.equals(other.advancedGenes)
                && layerColors.equals(other.layerColors)
                && java.util.Objects.equals(textureLocation, other.textureLocation);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(basePercentage, incubationProgress, incubationTimeLeft, baseDino, entityPercentages, advancedGenes, layerColors, textureLocation);
    }
}
