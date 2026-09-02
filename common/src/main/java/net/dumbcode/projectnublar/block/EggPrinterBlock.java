package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.block.api.MultiBlock;
import net.dumbcode.projectnublar.block.api.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.client.ModShapes;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;


public class EggPrinterBlock extends MultiEntityBlock {

    public EggPrinterBlock(Properties properties, int rows, int columns, int depth) {
        super(properties, rows, columns, depth);
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(MultiBlock.getCorePos(pState, pPos));
            if (blockEntity instanceof EggPrinterBlockEntity sbe) {
                if (pPlayer.getMainHandItem().is(ItemInit.LEVELING_SENSOR.get())) {
                    sbe.setSensor(pPlayer.getMainHandItem().copy());
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.CONSUME;
                }
                if (pPlayer.getMainHandItem().getItem() instanceof ComputerChipItem) {
                    sbe.setChip(pPlayer.getMainHandItem().copy());
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.CONSUME;
                }
            }
            return super.useWithoutItem(pState, pLevel, pPos, pPlayer, pHit);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShapeForDirection(Direction direction) {
        return switch (direction) {
            case SOUTH -> ModShapes.EGG_PRINTER_SOUTH;
            case EAST -> ModShapes.EGG_PRINTER_EAST;
            case WEST -> ModShapes.EGG_PRINTER_WEST;
            default -> ModShapes.EGG_PRINTER_NORTH;
        };
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EggPrinterBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), (world, pos, pState, be) -> be.tick(world, pos, pState, be));
    }
}
