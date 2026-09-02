package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Constants.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TYRANNOSAUR_ROAR = registerSoundEvents("entity.tyrannosaurus_rex.vocals.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> TYRANNOSAUR_GROWL = registerSoundEvents("entity.tyrannosaurus_rex.vocals.growl");
    public static final DeferredHolder<SoundEvent, SoundEvent> TYRANNOSAUR_BREATH = registerSoundEvents("entity.tyrannosaurus_rex.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TYRANNOSAUR_SNARL = registerSoundEvents("entity.tyrannosaurus_rex.attack.snarl");
    public static final DeferredHolder<SoundEvent, SoundEvent> TYRANNOSAUR_BITE = registerSoundEvents("entity.tyrannosaurus_rex.attack.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> TYRANNOSAUR_HURT = registerSoundEvents("entity.tyrannosaurus_rex.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TYRANNOSAUR_DEATH = registerSoundEvents("entity.tyrannosaurus_rex.death");

    public static void registerTo() {
        SOUND_EVENTS.register();
    }

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvents(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Constants.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
