package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.block.api.IMachineParts;
import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.block.api.NublarEnergyBlock;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class EggPrinterBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity, IMachineParts, NublarEnergyBlock {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack embryoInput = ItemStack.EMPTY;
    private ItemStack bonemealInput = ItemStack.EMPTY;
    private ItemStack eggOutput = ItemStack.EMPTY;
    private ItemStack syringeOutput = ItemStack.EMPTY;
    private int bonemealAmount = 0;
    private int bonemealMax = 30;
    private int progress = 0;
    private int maxProgress = 20 * 60 * 10;
    private boolean isPrinting = false;
    private ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index){
                case 0:
                    return bonemealAmount;
                case 1:
                    return bonemealMax;
                case 2:
                    return progress;
                case 3:
                    return getMaxProgress();
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index){
                case 0:
                    bonemealAmount = value;
                    break;
                case 1:
                    bonemealMax = value;
                    break;
                case 2:
                    progress = value;
                    break;
                case 3:
                    maxProgress = value;
                    break;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private ItemStack sensor = ItemStack.EMPTY;
    private ItemStack chip = ItemStack.EMPTY;
    private MachineEnergyHandler energyHandler;

    public EggPrinterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), pos, state);
    }

    public float getBreakChance() {
        return sensor.isEmpty() ? 0 : -1;
    }
    public int getMaxProgress(){
        return chip.isEmpty() ? maxProgress : ((ComputerChipItem)chip.getItem()).getMaxPrintTime();
    }
    public void tick(Level world, BlockPos pos, BlockState pState, EggPrinterBlockEntity be) {
        boolean shouldUpdate = false;
        if(bonemealAmount < bonemealMax && !bonemealInput.isEmpty()){
            bonemealAmount += 1;
            bonemealInput.shrink(1);
            shouldUpdate = true;
        }
        isPrinting = !embryoInput.isEmpty() && bonemealAmount >= 16 && eggOutput.isEmpty() && getEnergyHandler().getStoredEnergy() > 32;
        if(isPrinting){
            getEnergyHandler().internalExtract(calculateEnergyConsumption(),true);
            progress += 1;
            if(progress >= getMaxProgress()){
                progress = 0;
                bonemealAmount-=16;
                isPrinting = false;
                eggOutput = new ItemStack(level.getRandom().nextInt(10) == getBreakChance() ? ItemInit.CRACKED_ARTIFICIAL_EGG.get() : ItemInit.ARTIFICIAL_EGG.get());
                embryoInput = ItemStack.EMPTY;
                if(syringeOutput.isEmpty()) {
                    syringeOutput = new ItemStack(ItemInit.SYRINGE.get());
                } else {
                    syringeOutput.grow(1);
                }
            }
            shouldUpdate = true;
        }
        if(shouldUpdate){
            updateBlock();
        }
    }
    public int calculateEnergyConsumption(){
        int c = 32;
        if(chip.getItem() == ItemInit.DIAMOND_COMPUTER_CHIP.get()) {
            c += 24;
        }
        if(chip.getItem() == ItemInit.IRON_COMPUTER_CHIP.get()) {
            c += 8;
        }
        if(chip.getItem() == ItemInit.GOLD_COMPUTER_CHIP.get()) {
            c += 16;
        }
        if(!sensor.isEmpty()){
            c +=8;
        }
        return c;
    }
    @Override
    public MachineEnergyHandler getEnergyHandler() {
        return energyHandler == null ? this.energyHandler = new MachineEnergyHandler(1000, 1000, 0, this::setChanged) : this.energyHandler;
    }
    @Override
    protected void saveData(ValueOutput output) {
        output.store("embryoInput", ItemStack.OPTIONAL_CODEC, embryoInput);
        output.store("bonemealInput", ItemStack.OPTIONAL_CODEC, bonemealInput);
        output.store("eggOutput", ItemStack.OPTIONAL_CODEC, eggOutput);
        output.store("syringeOutput", ItemStack.OPTIONAL_CODEC, syringeOutput);
        output.putInt("bonemealAmount", bonemealAmount);
        output.putInt("bonemealMax", bonemealMax);
        output.putInt("progress", progress);
        output.putInt("maxProgress", maxProgress);
        output.putBoolean("isPrinting", isPrinting);
    }

    @Override
    protected void loadData(ValueInput input) {
        embryoInput = input.read("embryoInput", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        bonemealInput = input.read("bonemealInput", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        eggOutput = input.read("eggOutput", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        syringeOutput = input.read("syringeOutput", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        bonemealAmount = input.getIntOr("bonemealAmount", 0);
        bonemealMax = input.getIntOr("bonemealMax", 0);
        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("maxProgress", 0);
        isPrinting = input.getBooleanOr("isPrinting", false);
    }

    @Override
    protected Component getDefaultName() {
        return Component.nullToEmpty("Egg Printer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new EggPrinterMenu(pContainerId, pInventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return embryoInput.isEmpty() && bonemealInput.isEmpty() && eggOutput.isEmpty() && syringeOutput.isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return switch (pSlot) {
            case 0 -> embryoInput;
            case 1 -> bonemealInput;
            case 2 -> eggOutput;
            case 3 -> syringeOutput;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return switch (pSlot) {
            case 0 -> {
                if (!embryoInput.isEmpty()) {
                    ItemStack stack;
                    if (embryoInput.getCount() <= pAmount) {
                        stack = embryoInput;
                        embryoInput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = embryoInput.split(pAmount);
                        if (embryoInput.isEmpty()) {
                            embryoInput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 1 -> {
                if (!bonemealInput.isEmpty()) {
                    ItemStack stack;
                    if (bonemealInput.getCount() <= pAmount) {
                        stack = bonemealInput;
                        bonemealInput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = bonemealInput.split(pAmount);
                        if (bonemealInput.isEmpty()) {
                            bonemealInput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 2 -> {
                if (!eggOutput.isEmpty()) {
                    ItemStack stack;
                    if (eggOutput.getCount() <= pAmount) {
                        stack = eggOutput;
                        eggOutput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = eggOutput.split(pAmount);
                        if (eggOutput.isEmpty()) {
                            eggOutput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 3 -> {
                if (!syringeOutput.isEmpty()) {
                    ItemStack stack;
                    if (syringeOutput.getCount() <= pAmount) {
                        stack = syringeOutput;
                        syringeOutput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = syringeOutput.split(pAmount);
                        if (syringeOutput.isEmpty()) {
                            syringeOutput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        switch (pSlot){
            case 0:
                embryoInput = pStack;
                break;
            case 1:
                bonemealInput = pStack;
                break;
            case 2:
                eggOutput = pStack;
                break;
            case 3:
                syringeOutput = pStack;
                break;
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        embryoInput = ItemStack.EMPTY;
        bonemealInput = ItemStack.EMPTY;
        eggOutput = ItemStack.EMPTY;
        syringeOutput = ItemStack.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setSensor(ItemStack copy) {
        this.sensor = copy;
    }

    public void setChip(ItemStack copy) {
        this.chip = copy;
    }

    @Override
    public NonNullList<ItemStack> getMachineParts() {
        return NonNullList.of(ItemStack.EMPTY, sensor, chip);
    }

    @Override
    protected net.minecraft.core.NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, getItem(i));
        }
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        for (int i = 0; i < Math.min(items.size(), getContainerSize()); i++) {
            setItem(i, items.get(i));
        }
    }

    // 26.2: machine-part items are dropped here instead of in the old Block#onRemove
    @Override
    public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            net.minecraft.world.Containers.dropContents(this.level, pos, this.getMachineParts());
        }
    }
}
