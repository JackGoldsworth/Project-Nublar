package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.entity.dinosaur.carnivore.DilophosaurusEntity;
import net.dumbcode.projectnublar.entity.dinosaur.carnivore.TyrannosaurusRexEntity;
import net.dumbcode.projectnublar.entity.dinosaur.carnivore.VelociraptorEntity;
import net.dumbcode.projectnublar.entity.dinosaur.herbivore.BrachiosaurusEntity;
import net.dumbcode.projectnublar.entity.dinosaur.omnivore.GallimimusEntity;
import net.dumbcode.projectnublar.entity.dinosaur.herbivore.TriceratopsEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MODID);
    public static final List<AttributesRegister<?>> attributeSuppliers = new ArrayList<>();

    //Carnivores
    public static final DeferredHolder<EntityType<?>, EntityType<TyrannosaurusRexEntity>> TYRANNOSAURUS_REX = registerEntity("tyrannosaurus_rex", ()-> EntityType.Builder.of(TyrannosaurusRexEntity::new, MobCategory.MONSTER).sized(1,3), Dinosaur::createAttributes);
    public static final DeferredHolder<EntityType<?>, EntityType<VelociraptorEntity>> VELOCIRAPTOR = registerEntity("velociraptor", () -> EntityType.Builder.of(VelociraptorEntity::new, MobCategory.MONSTER).sized(0.6F,0.6F), Dinosaur::createAttributes);
    public static final DeferredHolder<EntityType<?>, EntityType<DilophosaurusEntity>> DILOPHOSAURUS = registerEntity("dilophosaurus", () -> EntityType.Builder.of(DilophosaurusEntity::new, MobCategory.MONSTER).sized(1,1), Dinosaur::createAttributes);

    //Herbivores
    public static final DeferredHolder<EntityType<?>, EntityType<TriceratopsEntity>> TRICERATOPS = registerEntity("triceratops", ()-> EntityType.Builder.of(TriceratopsEntity::new, MobCategory.MONSTER).sized(2,3), Dinosaur::createAttributes);
    public static final DeferredHolder<EntityType<?>, EntityType<BrachiosaurusEntity>> BRACHIOSAURUS = registerEntity("brachiosaurus", ()-> EntityType.Builder.of(BrachiosaurusEntity::new, MobCategory.MONSTER).sized(2,3), Dinosaur::createAttributes);

    //Omnivores
    public static final DeferredHolder<EntityType<?>, EntityType<GallimimusEntity>> GALLIMIMUS = registerEntity("gallimimus", ()-> EntityType.Builder.of(GallimimusEntity::new, MobCategory.MONSTER).sized(2,3), Dinosaur::createAttributes);

    public static final DeferredHolder<EntityType<?>, EntityType<DinosaurPart>> DINOSAUR_PART = registerEntity("dinosaur_part_entity", () -> EntityType.Builder.<DinosaurPart>of(DinosaurPart::new,
            MobCategory.MISC).sized(0.5f,0.5f));


    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> supplier) {
        return ENTITIES.register(name, () -> supplier.get().build(ResourceKey.create(Registries.ENTITY_TYPE, Constants.modLoc(name))));
    }
    private static <T extends LivingEntity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> supplier,
                                                                                                        Supplier<AttributeSupplier.Builder> attributeSupplier) {
        DeferredHolder<EntityType<?>, EntityType<T>> entityTypeSupplier = registerEntity(name, supplier);
        attributeSuppliers.add(new AttributesRegister<>(entityTypeSupplier, attributeSupplier));
        return entityTypeSupplier;
    }

    public static void registerTo() {
        ENTITIES.register();
    }

    public record AttributesRegister<E extends LivingEntity>(Supplier<EntityType<E>> entityTypeSupplier, Supplier<AttributeSupplier.Builder> factory) {}
}
