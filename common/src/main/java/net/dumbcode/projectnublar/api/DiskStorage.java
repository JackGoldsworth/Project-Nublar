package net.dumbcode.projectnublar.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiskStorage {
    private Map<EntityType<?>, Double> synthedEntityMap = new HashMap<>();
    private Set<DyeColor> tropicalFishColors = new HashSet<>();

    public void increaseSynthedEntity(EntityType<?> entityType, double amount) {
        synthedEntityMap.put(entityType, synthedEntityMap.getOrDefault(entityType, 0.0) + amount);
    }

    public void addTropicalFishColor(DyeColor color) {
        tropicalFishColors.add(color);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag synthedEntityTag = new CompoundTag();
        for (Map.Entry<EntityType<?>, Double> entry : synthedEntityMap.entrySet()) {
            synthedEntityTag.putDouble(BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()).toString(), entry.getValue());
        }
        tag.put("synthedEntities", synthedEntityTag);
        int[] colors = new int[tropicalFishColors.size()];
        int i = 0;
        for (DyeColor color : tropicalFishColors) {
            colors[i++] = color.getId();
        }
        tag.putIntArray("tropicalFishColors", colors);
        return tag;
    }

    public void load(CompoundTag tag) {
        CompoundTag synthedEntityTag = tag.getCompoundOrEmpty("synthedEntities");
        for (String key : synthedEntityTag.keySet()) {
            synthedEntityMap.put(BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(key)), synthedEntityTag.getDoubleOr(key, 0.0));
        }
        tag.getIntArray("tropicalFishColors").ifPresent(colors -> {
            for (int color : colors) {
                tropicalFishColors.add(DyeColor.byId(color));
            }
        });
    }
    public static DiskStorage createFromTag(CompoundTag tag) {
        DiskStorage storage = new DiskStorage();
        storage.load(tag);
        return storage;
    }

}
