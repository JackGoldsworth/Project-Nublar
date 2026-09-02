package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.block.api.IMachineParts;
import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.block.api.NublarEnergyBlock;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.DataComponentInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.init.TagInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequencerBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity, IMachineParts, NublarEnergyBlock {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack storage = ItemStack.EMPTY;
    private ItemStack dna_input = ItemStack.EMPTY;
    private ItemStack empty_vial_output = ItemStack.EMPTY;
    private ItemStack water = ItemStack.EMPTY;
    private ItemStack bone_matter = ItemStack.EMPTY;
    private ItemStack sugar = ItemStack.EMPTY;
    private ItemStack plant_matter = ItemStack.EMPTY;
    private ItemStack empty_tube_input = ItemStack.EMPTY;
    private ItemStack dna_test_tube_output = ItemStack.EMPTY;
    private ItemStack computer_chip = ItemStack.EMPTY;
    private ItemStack tank = ItemStack.EMPTY;
    private float sequencingTime = 0;
    private boolean hasComputer = false;
    private boolean hasDoor = false;
    private boolean hasScreen = false;
    private int waterLevel = 0;
    private int boneMatterLevel = 0;
    private int sugarLevel = 0;
    private int plantMatterLevel = 0;
    private DinoData dinoData = new DinoData();
    private boolean isSynthesizing = false;
    private int synthTime = 0;
    private MachineEnergyHandler energyHandler;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int slot) {
            return switch (slot) {
                case 0 -> Mth.floor(SequencerBlockEntity.this.sequencingTime);
                case 1 -> SequencerBlockEntity.this.getTotalSequencingTime();
                case 2 -> SequencerBlockEntity.this.waterLevel;
                case 3 -> SequencerBlockEntity.this.boneMatterLevel;
                case 4 -> SequencerBlockEntity.this.sugarLevel;
                case 5 -> SequencerBlockEntity.this.plantMatterLevel;
                case 6 -> SequencerBlockEntity.this.synthTime;
                case 7 -> SequencerBlockEntity.this.getMaxPlantMatterLevel();
                case 8 -> SequencerBlockEntity.this.getMaxWaterLevel();
                case 9 -> SequencerBlockEntity.this.getMaxSynthTime();
                default -> 0;
            };
        }

        public void set(int slot, int value) {
            switch (slot) {
                case 0 -> SequencerBlockEntity.this.sequencingTime = value;
                case 2 -> SequencerBlockEntity.this.waterLevel = value;
                case 3 -> SequencerBlockEntity.this.boneMatterLevel = value;
                case 4 -> SequencerBlockEntity.this.sugarLevel = value;
                case 5 -> SequencerBlockEntity.this.plantMatterLevel = value;
                case 6 -> SequencerBlockEntity.this.synthTime = value;
            }
        }

        public int getCount() {
            return 10;
        }
    };

    public DinoData getDinoData() {
        return dinoData;
    }

    public void setDinoData(DinoData dinoData) {
        this.dinoData = dinoData;
        updateBlock();
    }
    public int getMaxSynthTime(){
        return computer_chip.isEmpty() ? 10 * 20 * 60 : ((ComputerChipItem)computer_chip.getItem()).getMaxSynthTime();
    }
    public int getMaxWaterLevel(){
        return tank.isEmpty() ? 1000 : ((TankItem)tank.getItem()).getSynthFluid();
    }
    public int getMaxPlantMatterLevel(){
        return tank.isEmpty() ? 16 : ((TankItem)tank.getItem()).getSynthPlant();
    }

    private int getTotalSequencingTime() {
        return storage.isEmpty() ? 0 : ((DiskStorageItem) storage.getItem()).getProcessingTime();
    }

    public SequencerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SEQUENCER_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean isHasComputer() {
        return hasComputer;
    }

    public void setHasComputer(boolean hasComputer) {
        this.hasComputer = hasComputer;
        updateBlock();
    }

    public boolean isHasDoor() {
        return hasDoor;
    }

    public void setHasDoor(boolean hasDoor) {
        this.hasDoor = hasDoor;
        updateBlock();
    }

    public boolean isHasScreen() {
        return hasScreen;
    }

    public void setHasScreen(boolean hasScreen) {
        this.hasScreen = hasScreen;
        updateBlock();
    }

    public boolean isSynthesizing() {
        return isSynthesizing;
    }

    public void tick(Level world, BlockPos pos, BlockState pState, SequencerBlockEntity be) {
        if (!world.isClientSide()) {
            boolean shouldUpdate = false;
            if (!storage.isEmpty() && !dna_input.isEmpty() && dna_input.has(DataComponentInit.DNA_DATA.get()) && ((empty_vial_output.isEmpty() || empty_vial_output.is(dna_input.getItem())) || empty_vial_output.getCount() < 64)) {
                double currentPercent = 0;
                DNAData dnaData = dna_input.get(DataComponentInit.DNA_DATA.get());
                String storageName = dnaData.getStorageName();
                DNAData storedDNA = null;
                Map<String, DNAData> stored = storage.getOrDefault(DataComponentInit.DISK_DNA.get(), Map.of());
                if (stored.containsKey(dnaData.getStorageName())) {
                    storedDNA = stored.get(dnaData.getStorageName());
                    currentPercent = storedDNA.getDnaPercentage();
                }
                if (currentPercent < 1) {
                    sequencingTime++;
                    if (sequencingTime >= getTotalSequencingTime()) {
                        if (empty_vial_output.isEmpty()) {
                            empty_vial_output = new ItemStack(dna_input.getItem());
                        } else {
                            empty_vial_output.grow(1);
                        }
                        Map<String, DNAData> updated = new HashMap<>(stored);
                        if (storedDNA != null) {
                            updated.put(storageName, DNAData.combineDNA(storedDNA, dnaData));
                        } else {
                            updated.put(storageName, dnaData);
                        }
                        storage.set(DataComponentInit.DISK_DNA.get(), updated);

                        dna_input.shrink(1);
                        sequencingTime = 0;
                    }
                }
                shouldUpdate = true;
            } else if (sequencingTime != 0) {
                sequencingTime = 0;
                shouldUpdate = true;
            }
            if(water.is(Items.WATER_BUCKET) && waterLevel <= getMaxWaterLevel() - 1000) {
                waterLevel+=1000;
                water = new ItemStack(Items.BUCKET);
                shouldUpdate = true;
            }
            if(bone_matter.is(TagInit.BONE_MATTER) && boneMatterLevel < getMaxPlantMatterLevel()) {
                boneMatterLevel++;
                bone_matter.shrink(1);
                shouldUpdate = true;
            }
            if(sugar.is(TagInit.SUGAR) && sugarLevel < getMaxPlantMatterLevel()) {
                sugarLevel++;
                sugar.shrink(1);
                shouldUpdate = true;
            }
            if(plant_matter.is(TagInit.PLANT_MATTER) && plantMatterLevel < getMaxPlantMatterLevel()) {
                plantMatterLevel++;
                plant_matter.shrink(1);
                shouldUpdate = true;
            }
            if(isSynthesizing && canSynth()){
                synthTime++;
                getEnergyHandler().internalExtract(calculateEnergyConsumption(),true);
                if(synthTime > getMaxSynthTime()){
                    synthTime = 0;
                    dna_test_tube_output = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
                    dinoData.toStack(dna_test_tube_output);
                    waterLevel -= 500;
                    boneMatterLevel -= 8;
                    sugarLevel -= 8;
                    plantMatterLevel -= 8;
                    empty_tube_input.shrink(1);
                    isSynthesizing = false;
                }
                shouldUpdate = true;
            }
            if(shouldUpdate) {
                updateBlock();
            }
        }
    }
    public int calculateEnergyConsumption(){
        int c = 32;
        if(computer_chip.getItem() == ItemInit.GOLD_COMPUTER_CHIP.get()) {
            c += 32;
        }
        if(computer_chip.getItem() == ItemInit.IRON_COMPUTER_CHIP.get()) {
            c += 16;
        }
        return c;
    }
    @Override
    public MachineEnergyHandler getEnergyHandler() {
        return energyHandler == null ? this.energyHandler = new MachineEnergyHandler(1000, 1000, 0, this::setChanged) : this.energyHandler;
    }

    @Override
    protected void saveData(ValueOutput output) {
        output.store("storage", ItemStack.OPTIONAL_CODEC, storage);
        output.store("dna_input", ItemStack.OPTIONAL_CODEC, dna_input);
        output.store("empty_vial_output", ItemStack.OPTIONAL_CODEC, empty_vial_output);
        output.store("water", ItemStack.OPTIONAL_CODEC, water);
        output.store("bone_matter", ItemStack.OPTIONAL_CODEC, bone_matter);
        output.store("sugar", ItemStack.OPTIONAL_CODEC, sugar);
        output.store("plant_matter", ItemStack.OPTIONAL_CODEC, plant_matter);
        output.store("empty_tube_input", ItemStack.OPTIONAL_CODEC, empty_tube_input);
        output.store("dna_test_tube_output", ItemStack.OPTIONAL_CODEC, dna_test_tube_output);
        output.putFloat("sequencingTime", sequencingTime);
        output.putBoolean("hasComputer", hasComputer);
        output.putBoolean("hasDoor", hasDoor);
        output.putBoolean("hasScreen", hasScreen);
        output.putInt("waterLevel", waterLevel);
        output.putInt("boneMatterLevel", boneMatterLevel);
        output.putInt("sugarLevel", sugarLevel);
        output.putInt("plantMatterLevel", plantMatterLevel);
        output.store("DinoData", DinoData.CODEC, dinoData);
        output.putBoolean("isSynthesizing", isSynthesizing);
        output.putInt("synthTime", synthTime);
        output.store("computer_chip", ItemStack.OPTIONAL_CODEC, computer_chip);
        output.store("tank", ItemStack.OPTIONAL_CODEC, tank);
    }

    @Override
    protected void loadData(ValueInput input) {
        storage = input.read("storage", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        dna_input = input.read("dna_input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        empty_vial_output = input.read("empty_vial_output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        water = input.read("water", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        bone_matter = input.read("bone_matter", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        sugar = input.read("sugar", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        plant_matter = input.read("plant_matter", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        empty_tube_input = input.read("empty_tube_input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        dna_test_tube_output = input.read("dna_test_tube_output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        sequencingTime = input.getFloatOr("sequencingTime", 0f);
        hasComputer = input.getBooleanOr("hasComputer", false);
        hasDoor = input.getBooleanOr("hasDoor", false);
        hasScreen = input.getBooleanOr("hasScreen", false);
        waterLevel = input.getIntOr("waterLevel", 0);
        boneMatterLevel = input.getIntOr("boneMatterLevel", 0);
        sugarLevel = input.getIntOr("sugarLevel", 0);
        plantMatterLevel = input.getIntOr("plantMatterLevel", 0);
        dinoData = input.read("DinoData", DinoData.CODEC).orElseGet(DinoData::new);
        isSynthesizing = input.getBooleanOr("isSynthesizing", false);
        synthTime = input.getIntOr("synthTime", 0);
        computer_chip = input.read("computer_chip", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        tank = input.read("tank", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.projectnublar.sequencer").withStyle(ChatFormatting.WHITE);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new SequencerMenu(i, inventory, null, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return switch (i) {
            case 0 -> storage;
            case 1 -> dna_input;
            case 2 -> empty_vial_output;
            case 3 -> water;
            case 4 -> bone_matter;
            case 5 -> sugar;
            case 6 -> plant_matter;
            case 7 -> empty_tube_input;
            case 8 -> dna_test_tube_output;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        switch (i) {
            case 0 -> {
                if (!storage.isEmpty()) {
                    if (storage.getCount() <= i1) {
                        ItemStack itemstack = storage;
                        storage = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = storage.split(i1);
                    if (storage.isEmpty()) {
                        storage = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 1 -> {
                if (!dna_input.isEmpty()) {
                    if (dna_input.getCount() <= i1) {
                        ItemStack itemstack = dna_input;
                        dna_input = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = dna_input.split(i1);
                    if (dna_input.isEmpty()) {
                        dna_input = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 2 -> {
                if (!empty_vial_output.isEmpty()) {
                    if (empty_vial_output.getCount() <= i1) {
                        ItemStack itemstack = empty_vial_output;
                        empty_vial_output = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = empty_vial_output.split(i1);
                    if (empty_vial_output.isEmpty()) {
                        empty_vial_output = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 3 -> {
                if (!water.isEmpty()) {
                    if (water.getCount() <= i1) {
                        ItemStack itemstack = water;
                        water = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = water.split(i1);
                    if (water.isEmpty()) {
                        water = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 4 -> {
                if (!bone_matter.isEmpty()) {
                    if (bone_matter.getCount() <= i1) {
                        ItemStack itemstack = bone_matter;
                        bone_matter = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = bone_matter.split(i1);
                    if (bone_matter.isEmpty()) {
                        bone_matter = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 5 -> {
                if (!sugar.isEmpty()) {
                    if (sugar.getCount() <= i1) {
                        ItemStack itemstack = sugar;
                        sugar = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = sugar.split(i1);
                    if (sugar.isEmpty()) {
                        sugar = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 6 -> {
                if (!plant_matter.isEmpty()) {
                    if (plant_matter.getCount() <= i1) {
                        ItemStack itemstack = plant_matter;
                        plant_matter = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = plant_matter.split(i1);
                    if (plant_matter.isEmpty()) {
                        plant_matter = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 7 -> {
                if (!empty_tube_input.isEmpty()) {
                    if (empty_tube_input.getCount() <= i1) {
                        ItemStack itemstack = empty_tube_input;
                        empty_tube_input = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = empty_tube_input.split(i1);
                    if (empty_tube_input.isEmpty()) {
                        empty_tube_input = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 8 -> {
                if (!dna_test_tube_output.isEmpty()) {
                    if (dna_test_tube_output.getCount() <= i1) {
                        ItemStack itemstack = dna_test_tube_output;
                        dna_test_tube_output = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = dna_test_tube_output.split(i1);
                    if (dna_test_tube_output.isEmpty()) {
                        dna_test_tube_output = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            default -> {
                return ItemStack.EMPTY;
            }
        }
    }


    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        switch (i) {
            case 0 -> {
                storage = itemStack;
                sequencingTime = 0;
            }
            case 1 -> {
                if(!ItemStack.isSameItemSameComponents(dna_input, itemStack)) {
                    sequencingTime = 0;
                }
                dna_input = itemStack;
            }
            case 2 -> empty_vial_output = itemStack;
            case 3 -> water = itemStack;
            case 4 -> bone_matter = itemStack;
            case 5 -> sugar = itemStack;
            case 6 -> plant_matter = itemStack;
            case 7 -> empty_tube_input = itemStack;
            case 8 -> dna_test_tube_output = itemStack;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        storage = ItemStack.EMPTY;
        dna_input = ItemStack.EMPTY;
        empty_vial_output = ItemStack.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    public boolean canSynth(){
        return !empty_tube_input.isEmpty() && plantMatterLevel >= 8 && sugarLevel >= 8 && boneMatterLevel >= 8 && waterLevel >= 500 && getEnergyHandler().getStoredEnergy() > calculateEnergyConsumption();
    }
    public void toggleSynth() {
        if(canSynth()) {
            isSynthesizing = !isSynthesizing;
        } else {
            isSynthesizing = false;
            synthTime = 0;
        }
        updateBlock();
    }

    @Override
    public NonNullList<ItemStack> getMachineParts() {
        NonNullList<ItemStack> parts = NonNullList.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, computer_chip, tank);
        if(hasComputer)
            parts.set(0, new ItemStack(ItemInit.SEQUENCER_COMPUTER.get()));
        if(hasDoor)
            parts.set(1, new ItemStack(ItemInit.SEQUENCER_DOOR.get()));
        if(hasScreen)
            parts.set(2, new ItemStack(ItemInit.SEQUENCER_SCREEN.get()));
        return parts;

    }

    public void setChip(ItemStack chipItem) {
        computer_chip = chipItem;
        updateBlock();
    }

    public void setTank(ItemStack mainHandItem) {
        tank = mainHandItem;
        updateBlock();
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
