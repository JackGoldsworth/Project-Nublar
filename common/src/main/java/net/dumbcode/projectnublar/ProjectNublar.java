package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.init.*;
import net.dumbcode.projectnublar.network.NetworkInit;
import net.minecraft.util.random.WeightedList;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectNublar {
    public static Map<String, Map<String, WeightedList.Builder<String>>> WEIGHTED_PERIOD_BIOME_FOSSIL_MAP = new HashMap<>();;

    public static void init() {
        EntityInit.registerTo();
        BlockInit.registerTo();
        ItemInit.registerTo();
        DataComponentInit.registerTo();
        LootFunctionInit.registerTo();
        FeatureInit.registerTo();
        MenuTypeInit.registerTo();
        CreativeTabInit.registerTo();
        DataSerializerInit.SERIALIZERS.register();
        RecipeInit.registerTo();
        AttributesInit.registerTo();
        GeneInit.registerTo();
        MemoryModuleTypeInit.registerTo();
        SoundInit.registerTo();
        SensorTypesInit.registerTo();
        NetworkInit.registerPackets();
    }
    public static String checkReplace(String registryObject) {
        return Arrays.stream(registryObject.split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}
