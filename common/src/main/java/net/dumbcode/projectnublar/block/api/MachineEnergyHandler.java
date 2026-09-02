package net.dumbcode.projectnublar.block.api;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Loader-neutral energy state for machine block entities (was a NeoForge
 * SimpleEnergyHandler subclass; that was the only NeoForge API common depended on
 * for energy). Loader modules expose it to mod interop via their own capability
 * wrapper (see the neoforge module's MachineEnergyView).
 */
public class MachineEnergyHandler {

    private int energy;
    private final int capacity;
    private final int maxInsert;
    private final int maxExtract;
    private final Runnable onChange;

    public MachineEnergyHandler(int capacity, int maxInsert, int maxExtract, Runnable onChange) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.onChange = onChange;
    }

    public long getStoredEnergy() {
        return this.energy;
    }

    public long getMaxCapacity() {
        return this.capacity;
    }

    public int getMaxInsert() {
        return this.maxInsert;
    }

    public int getMaxExtract() {
        return this.maxExtract;
    }

    public void set(int energy) {
        this.energy = energy;
        this.onChange.run();
    }

    public void serialize(ValueOutput output) {
        output.putInt("energy", this.energy);
    }

    public void deserialize(ValueInput input) {
        this.energy = input.getIntOr("energy", 0);
    }

    /** Insert bypassing the transfer limit (used by loader capability wrappers after checking limits). */
    public int internalInsert(int amount, boolean simulate) {
        int inserted = (int) Math.min(amount, Math.max(0, (long) this.capacity - this.energy));
        if (inserted > 0 && !simulate) {
            set(this.energy + inserted);
        }
        return inserted;
    }

    /** Extract bypassing the transfer limit (used by loader capability wrappers after checking limits). */
    public int internalExtract(int amount, boolean simulate) {
        int extracted = Math.min(amount, this.energy);
        if (extracted > 0 && !simulate) {
            set(this.energy - extracted);
        }
        return extracted;
    }
}
