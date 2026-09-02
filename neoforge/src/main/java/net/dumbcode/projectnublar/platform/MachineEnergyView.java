package net.dumbcode.projectnublar.platform;

import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

// Capability view of the common MachineEnergyHandler for NeoForge's transfer API.
// Mirrors net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler's behaviour
// (limit checks + transaction snapshots) while delegating state to the loader-neutral
// handler the block entities actually own.
public class MachineEnergyView implements EnergyHandler {

    private final MachineEnergyHandler inner;
    private final SnapshotJournal<Integer> journal = new SnapshotJournal<>() {
        @Override
        protected Integer createSnapshot() {
            return (int) MachineEnergyView.this.inner.getStoredEnergy();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            MachineEnergyView.this.inner.set(snapshot);
        }

        @Override
        protected void onRootCommit(Integer snapshot) {
            // state was already applied to inner; nothing extra to commit
        }
    };

    public MachineEnergyView(MachineEnergyHandler inner) {
        this.inner = inner;
    }

    @Override
    public long getAmountAsLong() {
        return this.inner.getStoredEnergy();
    }

    @Override
    public long getCapacityAsLong() {
        return this.inner.getMaxCapacity();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        int accepted = Math.min(this.inner.getMaxInsert(), this.inner.internalInsert(amount, true));
        if (accepted > 0) {
            this.journal.updateSnapshots(transaction);
            this.inner.internalInsert(accepted, false);
        }
        return accepted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        int extracted = Math.min(this.inner.getMaxExtract(), this.inner.internalExtract(amount, true));
        if (extracted > 0) {
            this.journal.updateSnapshots(transaction);
            this.inner.internalExtract(extracted, false);
        }
        return extracted;
    }
}
