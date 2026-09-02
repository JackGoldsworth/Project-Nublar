package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.TagInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DinosaurFeederBlock extends BaseEntityBlock {

    public final String path;
    public DinosaurFeederBlock(Properties properties, String path) {
        super(properties);
        this.path = path;
    }

    // 26.2: feeder blocks have no Properties-only ctor; the codec is only used for
    // block-state serialization, which never re-creates these blocks
    @Override
    public com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return com.mojang.serialization.MapCodec.unit(this);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DinosaurFeederBlockEntity fbe) {
                if ((stack.is(TagInit.FEEDER_MEAT) && fbe.getItem(0).isEmpty()) || (!fbe.getItem(0).isEmpty() && stack.is(fbe.getItem(0).getItem()))) {
                    fbe.setItem(0, stack);
                    stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if ((stack.is(TagInit.FEEDER_MEAT) && fbe.getItem(1).isEmpty()) || (!fbe.getItem(1).isEmpty() && stack.is(fbe.getItem(1).getItem()))) {
                    fbe.setItem(1, stack);
                    stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if ((stack.is(TagInit.FEEDER_MEAT) && fbe.getItem(2).isEmpty()) || (!fbe.getItem(2).isEmpty() && stack.is(fbe.getItem(2).getItem()))) {
                    fbe.setItem(2, stack);
                    stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DinosaurFeederBlockEntity fbe) {
                if (player.getItemInHand(hand).isEmpty() && !fbe.coolDown && !player.isCrouching()) {
                    fbe.dispenseFood();
                    return InteractionResult.SUCCESS;
                }
                if (player.getItemInHand(hand).isEmpty() && player.isCrouching()) {
                    if (!fbe.getItem(0).isEmpty()) {
                        ItemStack copy = fbe.getItem(0).copy();
                        player.setItemInHand(hand, copy);
                        fbe.setItem(0, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS;
                    }
                    if (!fbe.getItem(1).isEmpty()) {
                        ItemStack copy = fbe.getItem(1).copy();
                        player.setItemInHand(hand, copy);
                        fbe.setItem(1, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS;
                    }
                    if (!fbe.getItem(2).isEmpty()) {
                        ItemStack copy = fbe.getItem(2).copy();
                        player.setItemInHand(hand, copy);
                        fbe.setItem(2, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        if (level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DinosaurFeederBlockEntity fbe) {
                if (player.getItemInHand(hand).is(Items.PORKCHOP) || player.getItemInHand(hand).is(Items.BEEF) || player.getItemInHand(hand).is(Items.CHICKEN)) {
                    if (!fbe.shouldDisplayFood) {
                        fbe.setTexture(true);
                    }
                }
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // 26.2: ENTITYBLOCK_ANIMATED is gone; the feeder model is drawn by CarnivoreFeederRenderer
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DinosaurFeederBlockEntity(blockPos,blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.FEEDER_BLOCK_ENTITY.get(),(world,pos,pState,be)-> be.tick(world,pos,pState,be));
    }
}
