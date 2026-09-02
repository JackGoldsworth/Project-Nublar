package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.block.api.IMachineParts;
import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.block.api.NublarEnergyBlock;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.BulbItem;
import net.dumbcode.projectnublar.item.ContainerUpgradeItem;
import net.dumbcode.projectnublar.item.PlantTankItem;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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

public class IncubatorBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity, IMachineParts, NublarEnergyBlock {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack plantMatterStack = ItemStack.EMPTY;
    private NonNullList<Slot> items = NonNullList.withSize(9, Slot.EMPTY);
    private NonNullList<Integer> eggProgress = NonNullList.withSize(9, 0);
    private int plantMatter = 0;
    private ItemStack containerStack = ItemStack.EMPTY;
    private ItemStack bulbStack = ItemStack.EMPTY;
    private ItemStack tankStack = ItemStack.EMPTY;
    private ItemStack nestStack = ItemStack.EMPTY;
    private ItemStack lidStack = ItemStack.EMPTY;
    private ItemStack baseStack = ItemStack.EMPTY;
    private ItemStack armStack = ItemStack.EMPTY;
    private MachineEnergyHandler energyHandler;

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.INCUBATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getContainerStack() {
        return containerStack;
    }

    public void setContainerStack(ItemStack containerStack) {
        this.containerStack = containerStack;
        updateBlock();
    }

    public ItemStack getBulbStack() {
        return bulbStack;
    }

    public void setBulbStack(ItemStack bulbStack) {
        this.bulbStack = bulbStack;
        updateBlock();
    }

    public ItemStack getTankStack() {
        return tankStack;
    }

    public void setTankStack(ItemStack tankStack) {
        this.tankStack = tankStack;
        updateBlock();
    }

    protected final ContainerData dataAccess = new ContainerData() {

        @Override
        public int get(int pIndex) {
            return switch (pIndex) {
                case 0 -> plantMatter;
                case 1 -> getMaxPlantMatter();
                case 2 -> getSlotCount();
                default -> eggProgress.get(pIndex - 3);
            };
        }

        @Override
        public void set(int pIndex, int pValue) {

        }

        @Override
        public int getCount() {
            return 11;
        }
    };

    public int getSlotCount() {
        return containerStack.isEmpty()? 3 : ((ContainerUpgradeItem) containerStack.getItem()).getContainerSize();
    }

    public int getX(int index) {
        return items.get(index).x;
    }

    public int getY(int index) {
        return items.get(index).y;
    }

    public void updateSlot(int index, int x, int y) {
        items.set(index - 1, items.get(index - 1).withX(x).withY(y));
        updateBlock();
    }

    @Override
    protected void saveData(ValueOutput output) {
        items.forEach(slot -> {
            ValueOutput slotTag = output.child("slot" + items.indexOf(slot));
            slotTag.putInt("x", slot.x);
            slotTag.putInt("y", slot.y);
            slotTag.store("stack", ItemStack.OPTIONAL_CODEC, slot.stack);
        });
        output.store("plantMatterStack", ItemStack.OPTIONAL_CODEC, plantMatterStack);
        output.putInt("plantMatter", plantMatter);
        output.store("containerStack", ItemStack.OPTIONAL_CODEC, containerStack);
        output.store("bulbStack", ItemStack.OPTIONAL_CODEC, bulbStack);
        output.store("tankStack", ItemStack.OPTIONAL_CODEC, tankStack);
        output.store("nestStack", ItemStack.OPTIONAL_CODEC, nestStack);
        output.store("lidStack", ItemStack.OPTIONAL_CODEC, lidStack);
        output.store("baseStack", ItemStack.OPTIONAL_CODEC, baseStack);
        output.store("armStack", ItemStack.OPTIONAL_CODEC, armStack);
        getEnergyHandler().serialize(output.child("energy"));
    }

    @Override
    protected void loadData(ValueInput input) {
        for (int i = 0; i < 9; i++) {
            ValueInput slotTag = input.childOrEmpty("slot" + i);
            items.set(i, new Slot(slotTag.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY), slotTag.getIntOr("x", 0), slotTag.getIntOr("y", 0)));
        }
        plantMatterStack = input.read("plantMatterStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        plantMatter = input.getIntOr("plantMatter", 0);
        containerStack = input.read("containerStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        bulbStack = input.read("bulbStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        tankStack = input.read("tankStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        nestStack = input.read("nestStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        lidStack = input.read("lidStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        baseStack = input.read("baseStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        armStack = input.read("armStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        getEnergyHandler().deserialize(input.childOrEmpty("energy"));
    }

    public ItemStack getNestStack() {
        return nestStack;
    }

    public void setNestStack(ItemStack nestStack) {
        this.nestStack = nestStack;
        updateBlock();
    }

    public ItemStack getLidStack() {
        return lidStack;
    }

    public void setLidStack(ItemStack lidStack) {
        this.lidStack = lidStack;
        updateBlock();
    }

    public ItemStack getBaseStack() {
        return baseStack;
    }

    public void setBaseStack(ItemStack baseStack) {
        this.baseStack = baseStack;
        updateBlock();
    }

    public ItemStack getArmStack() {
        return armStack;
    }

    public void setArmStack(ItemStack armStack) {
        this.armStack = armStack;
        updateBlock();
    }

    public int getTicksPerPercent() {
        return bulbStack.isEmpty() ? 18 * 20 : ((BulbItem) bulbStack.getItem()).getTicksPerPercent();
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Incubator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new IncubatorMenu(pContainerId, pInventory, this, dataAccess, worldPosition);
    }

    public int getMaxPlantMatter() {
        return getTankStack().isEmpty() ? 64 : ((PlantTankItem) getTankStack().getItem()).getMaxPlantMatter();
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        return plantMatterStack.isEmpty() && items.stream().allMatch(slot -> slot.stack.isEmpty());
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return switch (pSlot) {
            case 9 -> plantMatterStack;
            default -> items.get(pSlot).stack;
        };
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        switch (pSlot) {
            case 9 -> {
                ItemStack stack = plantMatterStack.split(pAmount);
                if (plantMatterStack.isEmpty()) {
                    plantMatterStack = ItemStack.EMPTY;
                }
                return stack;
            }
            default -> {
                Slot slot = items.get(pSlot);
                ItemStack stack = slot.stack.split(pAmount);
                if (slot.stack.isEmpty()) {
                    items.set(pSlot, Slot.EMPTY);
                }
                return stack;
            }
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        switch (pSlot) {
            case 9 -> {
                ItemStack stack = plantMatterStack;
                plantMatterStack = ItemStack.EMPTY;
                return stack;
            }
            default -> {
                Slot slot = items.get(pSlot);
                ItemStack stack = slot.stack;
                items.set(pSlot, Slot.EMPTY);
                return stack;
            }
        }
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        switch (pSlot) {
            case 9 -> plantMatterStack = pStack;
            default -> items.set(pSlot, items.get(pSlot).withStack(pStack));
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        plantMatterStack = ItemStack.EMPTY;
        items = NonNullList.withSize(9, Slot.EMPTY);
    }

    public void tick(Level world, BlockPos pos, BlockState pState, IncubatorBlockEntity be) {
        if (!world.isClientSide()) {
            if (!be.getItem(9).isEmpty()) {
                if (be.plantMatter < be.getMaxPlantMatter()) {
                    be.plantMatter += 1;
                    be.getItem(9).shrink(1);
                    updateBlock();
                }
            }
            if (level.getGameTime() % getTicksPerPercent() == 0)
                if (be.items.stream().anyMatch(slot -> !slot.stack.isEmpty())) {
                    getEnergyHandler().internalExtract(calculateEnergyConsumption(),true);
                    for (int i = 0; i < be.getSlotCount(); i++) {
                        Slot slot = be.items.get(i);
                        if (!slot.stack.isEmpty() && slot.stack.is(ItemInit.UNINCUBATED_EGG.get())) {
                            DinoData data = DinoData.fromStack(slot.stack);
                            if (data.getIncubationProgress() < 1) {
                                data.setIncubationProgress(data.getIncubationProgress() + 0.01);
                                data.setIncubationTimeLeft(Mth.floor(be.getTicksPerPercent() * ((1 - data.getIncubationProgress()) * 100)));
                                data.toStack(slot.stack);
                                updateBlock();
                            } else if (data.getIncubationProgress() >= 1) {
                                ItemStack dinoEgg = ItemInit.INCUBATED_EGG.get().getDefaultInstance();
                                data.setIncubationProgress(-1);
                                data.setIncubationTimeLeft(-1);
                                data.toStack(dinoEgg);
                                slot = slot.withStack(dinoEgg);
                                be.items.set(i, slot);
                                updateBlock();
                            }
                        }
                    }
                }
        }
    }
    public int calculateEnergyConsumption(){
        int c = 32;
        if(tankStack.getItem() == ItemInit.GOLD_PLANT_TANK.get()) {
            c += 8;
        }
        if(tankStack.getItem() == ItemInit.IRON_PLANT_TANK.get()) {
            c += 4;
        }
        if(containerStack.getItem() == ItemInit.SMALL_CONTAINER_UPGRADE.get()) {
            c += 4;
        }
        if(containerStack.getItem() == ItemInit.LARGE_CONTAINER_UPGRADE.get()) {
            c += 8;
        }
        if(bulbStack.getItem() == ItemInit.WARM_BULB.get()) {
            c += 5;
        }
        if(bulbStack.getItem() == ItemInit.WARMER_BULB.get()) {
            c += 10;
        }
        if(bulbStack.getItem() == ItemInit.HOT_BULB.get()) {
            c += 16;
        }
        return c;
    }
    @Override
    public MachineEnergyHandler getEnergyHandler() {
        return energyHandler == null ? this.energyHandler = new MachineEnergyHandler(1000, 1000, 0, this::setChanged) : this.energyHandler;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public NonNullList<ItemStack> getMachineParts() {
        return NonNullList.of(ItemStack.EMPTY,containerStack, bulbStack, tankStack, nestStack, lidStack, baseStack, armStack);
    }

    public record Slot(ItemStack stack, int x, int y) {
        public static final Slot EMPTY = new Slot(ItemStack.EMPTY, 0, -100);

        public Slot withStack(ItemStack stack) {
            return new Slot(stack, x, y);
        }

        public Slot withX(int x) {
            return new Slot(stack, x, y);
        }

        public Slot withY(int y) {
            return new Slot(stack, x, y);
        }
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
