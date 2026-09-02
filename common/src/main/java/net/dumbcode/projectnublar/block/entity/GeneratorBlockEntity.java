package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.block.GeneratorBlock;
import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.block.api.NublarEnergyBlock;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.dumbcode.projectnublar.platform.Services;

public class GeneratorBlockEntity extends SyncingContainerBlockEntity implements NublarEnergyBlock {


    private MachineEnergyHandler energyHandler;
    private ItemStack fuelStack = ItemStack.EMPTY;


    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.GENERATOR.get(), pos, state);
    }
    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int slot) {
            return switch (slot) {
                case 0 -> (int)GeneratorBlockEntity.this.getEnergyHandler().getStoredEnergy();
                case 1 -> (int)GeneratorBlockEntity.this.getEnergyHandler().getMaxCapacity();
                default -> 0;
            };
        }

        public void set(int slot, int value) {

        }

        public int getCount() {
            return 2;
        }
    };



    @Override
    protected void saveData(ValueOutput output) {
        output.store("fuel", ItemStack.OPTIONAL_CODEC, fuelStack);
        getEnergyHandler().serialize(output.child("energy"));
    }

    @Override
    protected void loadData(ValueInput input) {
        fuelStack = input.read("fuel", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        getEnergyHandler().deserialize(input.childOrEmpty("energy"));
    }

    @Override
    public MachineEnergyHandler getEnergyHandler() {
        Block block = getBlockState().getBlock();
        if (block instanceof GeneratorBlock gb && this.energyHandler == null) {
            this.energyHandler = new MachineEnergyHandler((int) gb.getMaxEnergy(), gb.getEnergyOutput(), gb.getEnergyInput(), this::setChanged);
        }
        return this.energyHandler;
    }

    public void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (state.getBlock() == BlockInit.CREATIVE_GENERATOR.get()) {
            getEnergyHandler().internalInsert(999999, false);
            distributeEnergy(256);
        } else {
            if(!fuelStack.isEmpty()){
                if(level.getGameTime() % 20 == 0 && getEnergyHandler().getStoredEnergy() < getEnergyHandler().getMaxCapacity()){
                    fuelStack.shrink(1);
                    getEnergyHandler().internalInsert(4,false);
                }
            }
            distributeEnergy(Math.min(((GeneratorBlock)state.getBlock()).getEnergyOutput(), (int) be.getEnergyHandler().getStoredEnergy()));
            updateBlock();
        }
    }

    private void distributeEnergy(int amount) {
        if (amount <= 0 || this.level == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            Services.PLATFORM.moveEnergy(this.level, this.getBlockPos().relative(direction), direction.getOpposite(), getEnergyHandler(), amount);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Generator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new GeneratorMenu(pContainerId,pInventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return fuelStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return fuelStack;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return fuelStack.split(pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        fuelStack = pStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        fuelStack = ItemStack.EMPTY;
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
}
