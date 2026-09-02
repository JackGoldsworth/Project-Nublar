package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

public class DinosaurFeederBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity {

    private AnimatableInstanceCache INSTANCE = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation CLOSED_IDLE = RawAnimation.begin().thenPlayAndHold("idle");
    protected static final RawAnimation DISPENSE_FOOD = RawAnimation.begin().thenPlayAndHold("open");

    protected PlayState deployAnimController(final AnimationTest<DinosaurFeederBlockEntity> state) {
            if(this.shouldDispenseFood) {
                return state.setAndContinue(DISPENSE_FOOD);
            } else {
                return state.setAndContinue(CLOSED_IDLE);
            }
    }
    private ItemStack foodSlot1 = ItemStack.EMPTY;
    private ItemStack foodSlot2 = ItemStack.EMPTY;
    private ItemStack foodSlot3 = ItemStack.EMPTY;

    private int progress;

    private final int maxProgress = 100;
    public boolean shouldDispenseFood;
    public boolean coolDown = false;
    public boolean shouldDisplayFood;

    public DinosaurFeederBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.FEEDER_BLOCK_ENTITY.get(), pos, blockState);
    }
    public void tick(Level world, BlockPos pos, BlockState pState, DinosaurFeederBlockEntity be) {
        if (this.shouldDispenseFood) {
            progress++;
            if (progress >= maxProgress) {
                progress = 0;
                coolDown = false;
                updateBlock();
            }
        } else if (progress > 0 && !this.shouldDispenseFood) {
            progress = 0;
            updateBlock();
        }
        if(!this.shouldDispenseFood) {
            coolDown = false;
        }
    }
    @Override
    protected Component getDefaultName() {
        return Component.nullToEmpty("Dinosaur Feeder");
    }

    public void setTexture(boolean flag) {
        this.shouldDisplayFood = flag;
        this.setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return null;
    }

    @Override
    protected void saveData(ValueOutput output) {
        output.putInt("progress", progress);
        output.putBoolean("shouldDispenseFood", shouldDispenseFood);
        output.putBoolean("shouldDisplayFood", shouldDisplayFood);
    }

    @Override
    protected void loadData(ValueInput input) {
        shouldDispenseFood = input.getBooleanOr("shouldDispenseFood", false);
        shouldDisplayFood = input.getBooleanOr("shouldDisplayFood", false);
        progress = input.getIntOr("progress", 0);
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    public void dispenseFood(){
        this.shouldDispenseFood = !this.shouldDispenseFood;
        coolDown = true;
        if(this.shouldDispenseFood) {
            this.setTexture(!this.isEmpty());
        } else {
            this.setTexture(false);
        }
        this.setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public boolean isEmpty() {
        return foodSlot1.isEmpty() && foodSlot2.isEmpty() && foodSlot3.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return switch(i){
            case 0 -> foodSlot1;
            case 1 -> foodSlot2;
            case 3 -> foodSlot3;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        return switch(i){
            case 0 -> {
                if(!foodSlot1.isEmpty()){
                    ItemStack stack;
                    if(foodSlot1.getCount() <= i1) {
                        stack = foodSlot1;
                        foodSlot1 = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = foodSlot1.split(i1);
                        if(foodSlot1.isEmpty()){
                            foodSlot1 = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 1 -> {
                if(!foodSlot2.isEmpty()){
                    ItemStack stack;
                    if(foodSlot2.getCount() <= i1) {
                        stack = foodSlot2;
                        foodSlot2 = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = foodSlot2.split(i1);
                        if(foodSlot2.isEmpty()){
                            foodSlot2 = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 2 -> {
                if(!foodSlot3.isEmpty()){
                    ItemStack stack;
                    if(foodSlot3.getCount() <= i1) {
                        stack = foodSlot3;
                        foodSlot3 = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = foodSlot3.split(i1);
                        if(foodSlot3.isEmpty()){
                            foodSlot3 = ItemStack.EMPTY;
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
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        switch (i){
            case 0:
                foodSlot1 = itemStack;
                break;
            case 1:
                foodSlot2 = itemStack;
                break;
            case 2:
                foodSlot3 = itemStack;
                break;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        shouldDisplayFood = false;
        foodSlot1 = ItemStack.EMPTY;
        foodSlot2 = ItemStack.EMPTY;
        foodSlot3 = ItemStack.EMPTY;
        this.setChanged();
        if (!level.isClientSide()) {
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this::deployAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return INSTANCE;
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
