package net.dumbcode.projectnublar.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.ProjectNublar;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class DNAData {

    public static final Codec<DNAData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType").forGetter(DNAData::getEntityType),
        Codec.DOUBLE.optionalFieldOf("dnaPercentage", 0.0).forGetter(DNAData::getDnaPercentage),
        Codec.STRING.optionalFieldOf("variant").forGetter(dna -> Optional.ofNullable(dna.getVariant())),
        Codec.STRING.optionalFieldOf("fossilPiece").forGetter(dna -> Optional.ofNullable(dna.getFossilPiece()).map(FossilPiece::name)),
        Codec.STRING.optionalFieldOf("quality").forGetter(dna -> Optional.ofNullable(dna.getQuality()).map(Quality::getName)),
        Codec.BOOL.optionalFieldOf("isEmbryo", false).forGetter(DNAData::isEmbryo)
    ).apply(instance, (entityType, dnaPercentage, variant, fossilPiece, quality, isEmbryo) -> {
        DNAData dnaData = new DNAData();
        dnaData.setEntityType(entityType);
        dnaData.setDnaPercentage(dnaPercentage);
        variant.ifPresent(dnaData::setVariant);
        fossilPiece.map(FossilPieces::getPieceByName).ifPresent(dnaData::setFossilPiece);
        quality.map(Quality::byName).ifPresent(dnaData::setQuality);
        dnaData.setEmbryo(isEmbryo);
        return dnaData;
    }));

    public static final StreamCodec<io.netty.buffer.ByteBuf, DNAData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    private EntityType<?> entityType;
    private double dnaPercentage;
    private String variant;
    private FossilPiece fossilPiece;
    private Quality quality;
    boolean isEmbryo;
    private DyeColor tFish1 = DyeColor.BLACK;
    private DyeColor tFish2 = DyeColor.BLACK;

    public DNAData() {
    }

    public DyeColor gettFish1() {
        return tFish1;
    }

    public DyeColor gettFish2() {
        return tFish2;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public double getDnaPercentage() {
        if (quality != null) {
            //  return FossilsConfig.getQuality(quality.getName()).dnaYield().get() / 100d;
            return dnaPercentage;
        }
        return dnaPercentage;
    }

    public void setDnaPercentage(double dnaPercentage) {
        this.dnaPercentage = dnaPercentage;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public boolean isEmbryo() {
        return isEmbryo;
    }

    public void setEmbryo(boolean embryo) {
        isEmbryo = embryo;
    }

   public FossilPiece getFossilPiece() {
        return fossilPiece;
   }

    public void setFossilPiece(FossilPiece fossilPiece) {
        this.fossilPiece = fossilPiece;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public String getNameSpace() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getNamespace();
    }

    public String getPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath();
    }


    public String getStorageName() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType) + (variant == null ? "" : "_" + variant);
    }

    public static void createTooltip(ItemStack stack, List<Component> tooltip) {
        DNAData dnaData = stack.get(DataComponentInit.DNA_DATA.get());
        if (dnaData != null) {
            tooltip.add(dnaData.getFormattedType());
            if (dnaData.getDnaPercentage() != 0)
                tooltip.add(dnaData.getFormattedDNA());
            if (dnaData.getQuality() != null) {
                tooltip.add(Component.translatable("quality." + Constants.MODID + "." + dnaData.getQuality().getName()));
            }
            if (dnaData.variant != null) {
                tooltip.add(Component.literal(ProjectNublar.checkReplace(dnaData.variant)));
            }
            dnaData.addTFishTT(tooltip);
        }
    }

    public void addTFishTT(List<Component> tooltip) {
        if (tFish1 != DyeColor.BLACK) {
            tooltip.add(Component.translatable("tooltip." + Constants.MODID + ".tropical", Component.translatable("color.minecraft." + tFish1.getName()), Component.translatable("color.minecraft." + tFish2.getName())));
        }
    }

    public static DNAData combineDNA(DNAData dna1, DNAData dna2) {
        DNAData dnaData = new DNAData();
        if (dna1.getStorageName().equals(dna2.getStorageName())) {
            dnaData.setEntityType(dna1.getEntityType());
            dnaData.setDnaPercentage(Math.min(1.0d, dna1.getDnaPercentage() + dna2.getDnaPercentage()));
            dnaData.setVariant(dna1.getVariant());
            dnaData.setFossilPiece(dna1.getFossilPiece());
            dnaData.setQuality(dna1.getQuality());
            dnaData.setEmbryo(false);
            return dnaData;
        }
        return null;
    }

    public MutableComponent getFormattedType() {
        String localVariant = "";
        if (getVariant() != null) {
            if (entityType.getDescription().getString().toLowerCase().contains("parrot"))
                localVariant = ProjectNublar.checkReplace(variant);
            else if (entityType.getDescription().getString().toLowerCase().contains("cat"))
                localVariant = ProjectNublar.checkReplace(Identifier.parse(variant).getPath());
        }
        return Component.literal(localVariant + getEntityType().getDescription().getString());
    }

    public MutableComponent getFormattedDNA() {
        return Component.literal(Mth.floor(getDnaPercentage() * 100) + "% DNA");
    }

    public MutableComponent getFormattedDNANoDescriptor() {
        return Component.literal(Mth.floor(getDnaPercentage() * 100) + "%");
    }

    public CompoundTag saveToNBT(CompoundTag tag) {
        tag.putString("entityType", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        if (dnaPercentage != 0)
            tag.putDouble("dnaPercentage", dnaPercentage);
        if (variant != null)
            tag.putString("variant", variant);
        if (fossilPiece != null)
            tag.putString("fossilPiece", fossilPiece.name());
        if (quality != null)
            tag.putString("quality", quality.getName());
        tag.putBoolean("isEmbryo", isEmbryo);
        return tag;
    }

    public void addTFish(TropicalFish tropicalFish) {
        tFish1 = tropicalFish.getPatternColor();
        tFish2 = tropicalFish.getBaseColor();
    }

    public static DNAData loadFromNBT(CompoundTag tag) {
        DNAData dnaData = new DNAData();
        tag.getString("entityType").map(Identifier::parse).map(BuiltInRegistries.ENTITY_TYPE::getValue).ifPresent(dnaData::setEntityType);
        tag.getDouble("dnaPercentage").ifPresent(dnaData::setDnaPercentage);
        tag.getString("variant").ifPresent(dnaData::setVariant);
        tag.getString("fossilPiece").map(FossilPieces::getPieceByName).ifPresent(dnaData::setFossilPiece);
        tag.getString("quality").map(Quality::byName).ifPresent(dnaData::setQuality);
        dnaData.setEmbryo(tag.getBooleanOr("isEmbryo", false));
        return dnaData;
    }

    public static DNAData fromDrive(ItemStack stack, EntityType<?> entityType) {

        return stack.get(DataComponentInit.DNA_DATA.get());
    }

    public static String createStorageKey(EntityType<?> entityType, String variant) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType) + (variant == null ? "" : "_" + variant);
    }

    public DinoData.EntityInfo getEntityInfo() {
        return new DinoData.EntityInfo(entityType, variant);
    }

    // 26.2: data components must implement equals/hashCode (registration validation crashes otherwise)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DNAData other)) return false;
        return Double.compare(dnaPercentage, other.dnaPercentage) == 0
                && isEmbryo == other.isEmbryo
                && java.util.Objects.equals(entityType, other.entityType)
                && java.util.Objects.equals(variant, other.variant)
                && fossilPiece == other.fossilPiece
                && quality == other.quality
                && tFish1 == other.tFish1
                && tFish2 == other.tFish2;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(entityType, dnaPercentage, variant, fossilPiece, quality, isEmbryo, tFish1, tFish2);
    }
}
