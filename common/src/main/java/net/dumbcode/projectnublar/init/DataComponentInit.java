package net.dumbcode.projectnublar.init;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.DNAData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

import java.util.Map;

public class DataComponentInit {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Constants.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DNAData>> DNA_DATA = COMPONENTS.register("dna_data",
        () -> DataComponentType.<DNAData>builder().persistent(DNAData.CODEC).networkSynchronized(DNAData.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DinoData>> DINO_DATA = COMPONENTS.register("dino_data",
        () -> DataComponentType.<DinoData>builder().persistent(DinoData.CODEC).networkSynchronized(DinoData.STREAM_CODEC).build());

    // Sequencer disks store sequenced DNA keyed by "type[_variant]" storage name
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, DNAData>>> DISK_DNA = COMPONENTS.register("disk_dna",
        () -> DataComponentType.<Map<String, DNAData>>builder()
            .persistent(Codec.unboundedMap(Codec.STRING, DNAData.CODEC))
            .networkSynchronized(DinoData.MAP_STREAM_CODEC)
            .build());

    public static void registerTo() {
        COMPONENTS.register();
    }
}
