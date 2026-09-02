package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.dumbcode.projectnublar.platform.DeferredHolder;

import java.util.concurrent.CompletableFuture;

public class ModSoundProvider extends SoundDefinitionsProvider {
    public ModSoundProvider(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
        super(generator, Constants.MODID);
    }

    @Override
    public void registerSounds() {
        // SoundInit.SOUNDS.getEntries().forEach(this::addSound);
    }

    public void addSound(DeferredHolder<SoundEvent, SoundEvent> entry) {
        add(entry.holder(), SoundDefinition.definition().with(sound(entry.getId())));
    }
}
