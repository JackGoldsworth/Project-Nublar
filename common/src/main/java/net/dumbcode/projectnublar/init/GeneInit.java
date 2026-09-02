package net.dumbcode.projectnublar.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.Genes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GeneInit {

    public static ResourceKey<Registry<Genes.Gene>> GENE_KEY = ResourceKey.createRegistryKey(Constants.modLoc("gene"));
    public static DeferredRegister<Genes.Gene> GENES = DeferredRegister.create(GENE_KEY, Constants.MODID);
    public static Registry<Genes.Gene> GENE_REGISTRY = GENES.makeRegistry();

    public static DeferredHolder<Genes.Gene, Genes.Gene> AGGRESSION = register("aggression");
    public static DeferredHolder<Genes.Gene, Genes.Gene> DEFENSE = register("defense");
    public static DeferredHolder<Genes.Gene, Genes.Gene> EAT_RATE = register("eat_rate");
    public static DeferredHolder<Genes.Gene, Genes.Gene> HEALTH = register("health");
    public static DeferredHolder<Genes.Gene, Genes.Gene> HEALTH_REGEN = register("health_regen");
    public static DeferredHolder<Genes.Gene, Genes.Gene> HEAT_RESISTANCE = register("heat_resistance");
    public static DeferredHolder<Genes.Gene, Genes.Gene> HERD_SIZE = register("herd_size");
    public static DeferredHolder<Genes.Gene, Genes.Gene> PACK_SIZE = register("pack_size");
    public static DeferredHolder<Genes.Gene, Genes.Gene> IMMUNITY = register("immunity");
    public static DeferredHolder<Genes.Gene, Genes.Gene> INTELLIGENCE = register("intelligence");
    public static DeferredHolder<Genes.Gene, Genes.Gene> JUMP = register("jump");
    public static DeferredHolder<Genes.Gene, Genes.Gene> NOCTURNAL = register("nocturnal");
    public static DeferredHolder<Genes.Gene, Genes.Gene> FERTILITY = register("fertility");
    public static DeferredHolder<Genes.Gene, Genes.Gene> SIZE = register("size");
    public static DeferredHolder<Genes.Gene, Genes.Gene> SPEED = register("speed");
    public static DeferredHolder<Genes.Gene, Genes.Gene> STOMACH_CAPACITY = register("stomach_capacity");
    public static DeferredHolder<Genes.Gene, Genes.Gene> STRENGTH = register("strength");
    public static DeferredHolder<Genes.Gene, Genes.Gene> TAMABILITY = register("tamability");
    public static DeferredHolder<Genes.Gene, Genes.Gene> UNDERWATER_CAPACITY = register("underwater_capacity");
    public static DeferredHolder<Genes.Gene, Genes.Gene> COLOR = register("color", 0);
    public static DeferredHolder<Genes.Gene, Genes.Gene> GENDER = register("gender");


    public static DeferredHolder<Genes.Gene, Genes.Gene> register(String name) {
        return GENES.register(name, () -> new Genes.Gene(name));
    }

    public static DeferredHolder<Genes.Gene, Genes.Gene> register(String name, double requirement) {
        return GENES.register(name, () -> new Genes.Gene(name, requirement));
    }

    public static void registerTo() {
        GENES.register();
    }

    public static List<Genes.Gene> getList() {
        List<Genes.Gene> genes = new ArrayList<>();
        for (DeferredHolder<Genes.Gene, ? extends Genes.Gene> gene : GENES.getEntries()) {
            genes.add(gene.get());
        }
        return genes;
    }

    public static Codec<Genes.Gene> byNameCodec() {
        Codec<Genes.Gene> nameCodec = Identifier.CODEC.flatXmap(
                (location) -> Optional.ofNullable(GENE_REGISTRY.getValue(location)).map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + GENE_KEY + ": " + location)),
                (gene) -> Optional.ofNullable(GENE_REGISTRY.getKey(gene))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + GENE_KEY + ": " + gene)));
        Codec<Genes.Gene> idCodec = ExtraCodecs.idResolverCodec(
                (gene) -> GENE_REGISTRY.getKey(gene) != null ? GENE_REGISTRY.getId(gene) : -1,
                GENE_REGISTRY::byId, -1);
        return ExtraCodecs.orCompressed(nameCodec, idCodec);
    }
}
