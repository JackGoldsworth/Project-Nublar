package net.dumbcode.projectnublar.block;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.block.api.BlockConnectableBase;
import net.dumbcode.projectnublar.block.api.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.Connection;
import net.dumbcode.projectnublar.block.api.ConnectionType;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class ElectricFencePostBlock extends BlockConnectableBase implements EntityBlock {

    public final ConnectionType type;
    public final IntegerProperty indexProperty;
    public static final BooleanProperty POWERED_PROPERTY = BooleanProperty.create("powered");

    private static boolean destroying = false;

    public static final int LIMIT = 15;

    public ElectricFencePostBlock(Properties properties, ConnectionType type, IntegerProperty indexProperty) {
        super(properties);
        this.type = type;
        this.indexProperty = indexProperty;
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED_PROPERTY, false).setValue(indexProperty,0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED_PROPERTY);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        // 26.2: ENTITYBLOCK_ANIMATED is gone; the pole is drawn by ElectricFenceRenderer
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getDefaultShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof BlockEntityElectricFencePole) {
            return ((BlockEntityElectricFencePole) entity).getCachedShape();
        }
        return Shapes.block();
    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        boolean flag = true;
        for (int i = 0; i < this.type.getHeight(); i++) {
            flag &= world.getBlockState(pos.above(i)).canBeReplaced(Fluids.EMPTY);
        }
        return flag;
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(this.indexProperty, 0);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState old, boolean p_220082_5_) {
        if (state.getValue(indexProperty) == 0) {
            for (int i = 1; i < this.type.getHeight(); i++) {
                world.setBlock(pos.above(i), this.defaultBlockState().setValue(indexProperty, i), 3);
            }

        }
        super.onPlace(state, world, pos, old, p_220082_5_);
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult ray) {
        int index = state.getValue(indexProperty);
        if (index == 0) {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof BlockEntityElectricFencePole fencePole) {
                    fencePole.setFlippedAround(!fencePole.isFlippedAround());
                    fencePole.setChanged();
                    for (int y = 0; y < this.type.getHeight(); y++) {
                        BlockEntity t = world.getBlockEntity(pos.above(y));
                        if (t != null) {
                            fencePole.triggerModelUpdate();
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            } else if (stack.getItem() == ItemInit.WIRE_SPOOL.get()) { //Move to item class ?
                // 26.2: stack NBT is gone — the target fence position is stored on the
                // wire spool via the CUSTOM_DATA data component as a packed long
                CompoundTag nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                if (nbt.contains("fence_position")) {
                    BlockPos other = BlockPos.of(nbt.getLongOr("fence_position", 0L));
                    double dist = Math.sqrt(other.distSqr(pos));
                    if (dist > LIMIT) {
                        if (!world.isClientSide()) {
                            sendActionBarMessage(player, Component.translatable("projectnublar.fences.length.toolong", Math.round(dist), LIMIT));
                        }
                        nbt.putLong("fence_position", pos.asLong());
                    } else if (world.getBlockState(other).getBlock() == this && !other.equals(pos)) {
                        int itemMax;
                        int itemAmount = itemMax = Mth.ceil(dist / ElectricFenceBlock.ITEM_FOLD * this.type.getHeight());
                        int total = 0;
                        boolean full = false;
                        List<Pair<ItemStack, Integer>> stacksFound = Lists.newArrayList();

                        if (itemAmount <= stack.getCount()) {
                            total += itemAmount;
                            stacksFound.add(Pair.of(stack, itemAmount));
                            full = true;
                        } else {
                            total += stack.getCount();
                            stacksFound.add(Pair.of(stack, stack.getCount()));
                        }
                        itemAmount -= stack.getCount();

                        for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
                            if (itemStack != stack && itemStack.getItem() == ItemInit.WIRE_SPOOL.get()) {
                                if (itemAmount <= itemStack.getCount()) {
                                    total += itemAmount;
                                    stacksFound.add(Pair.of(itemStack, itemAmount));
                                    full = true;
                                    break;
                                } else {
                                    total += itemStack.getCount();
                                    stacksFound.add(Pair.of(itemStack, itemStack.getCount()));
                                }
                                itemAmount -= itemStack.getCount();
                            }
                        }
                        if (!full) {
                            if (!world.isClientSide()) {
                                sendActionBarMessage(player, Component.translatable("projectnublar.fences.length.notenough", itemMax, total));
                            }
                        } else {
                            if (!player.isCreative()) {
                                stacksFound.forEach(p -> p.getLeft().shrink(p.getRight()));
                            }
                            for (double offset : this.type.getOffsets()) {
                                List<BlockPos> positions = LineUtils.getBlocksInbetween(pos, other, offset);
                                for (int i = 0; i < this.type.getHeight(); i++) {
                                    BlockPos pos1 = pos.above(i);
                                    BlockPos other1 = other.above(i);
                                    for (int i1 = 0; i1 < positions.size(); i1++) {
                                        BlockPos position = positions.get(i1).above(i);
                                        if ((world.getBlockState(position).isAir() || world.getBlockState(position).canBeReplaced(Fluids.EMPTY)) && !(world.getBlockState(position).getBlock() instanceof ElectricFencePostBlock)) {
                                            world.setBlock(position, BlockInit.ELECTRIC_FENCE.get().defaultBlockState(), 3);
                                        }
                                        BlockEntity fencete = world.getBlockEntity(position);
                                        if (fencete instanceof ConnectableBlockEntity) {
                                            ((ConnectableBlockEntity) fencete).addConnection(new Connection(fencete, this.type, offset, pos1, other1, positions.get(Math.min(i1 + 1, positions.size() - 1)).above(i), positions.get(Math.max(i1 - 1, 0)).above(i), position));
                                        }
                                    }
                                }
                            }
                        }
                        nbt.putLong("fence_position", pos.asLong());
                    } else {
                        nbt.remove("fence_position");
                    }
                } else {
                    nbt.putLong("fence_position", pos.asLong());
                }
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
                return InteractionResult.SUCCESS;
            }
        } else if (world.getBlockState(pos.below(index)).getBlock() == this) {
            return this.useWithoutItem(world.getBlockState(pos.below(index)), world, pos.below(index), player, ray);
        }
        return super.useWithoutItem(state, world, pos, player, ray);
    }

    // 26.2: displayClientMessage is gone; action-bar text is sent as a packet
    private static void sendActionBarMessage(Player player, Component message) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(message));
        }
    }

    // 26.2: onRemove is gone. Wire connection teardown happens in
    // BlockEntityElectricFencePole.preRemoveSideEffects while the pole's block entity is
    // still alive; this hook only clears the pole's remaining column segments.
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!destroying) {
            destroying = true;
            int index = state.getValue(indexProperty);
            for (int i = 1; i < index + 1; i++) {
                level.setBlock(pos.below(i), Blocks.AIR.defaultBlockState(), 3); //TODO: verify if our block?
            }
            for (int i = 1; i < this.type.getHeight() - index; i++) {
                level.setBlock(pos.above(i), Blocks.AIR.defaultBlockState(), 3);
            }
            destroying = false;
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }


    // 26.2: getLightBlock(BlockState, BlockGetter, BlockPos) is now getLightDampening(BlockState)
    @Override
    protected int getLightDampening(BlockState state) {
        return state.getValue(POWERED_PROPERTY) && state.getValue(indexProperty) == this.type.getHeight() - 1 ? this.type.getLightLevel() : 0;
    }


    public ConnectionType getType() {
        return type;
    }

    public IntegerProperty getIndexProperty() {
        return indexProperty;
    }

    public static boolean isDestroying() {
        return destroying;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityElectricFencePole(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, pBlockEntityType, (level, pos, state, be) -> ((BlockEntityElectricFencePole)be).tick(level, pos, state, (BlockEntityElectricFencePole)be));
    }
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
    }
}
