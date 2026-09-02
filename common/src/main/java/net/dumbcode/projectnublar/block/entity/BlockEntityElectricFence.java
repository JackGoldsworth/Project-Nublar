package net.dumbcode.projectnublar.block.entity;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.block.api.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.Connection;
import net.dumbcode.projectnublar.block.api.SyncingBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.dumbcode.projectnublar.annotation.OnlyIn.Dist;
import net.dumbcode.projectnublar.annotation.OnlyIn;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockEntityElectricFence extends BlockEntityElectricFenceBase implements ConnectableBlockEntity {

    public BlockEntityElectricFence(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get(), pos, state);
    }

    protected BlockEntityElectricFence(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void resetCollidableCache(){
        this.collidableCache = null;
    }

    @Override
    protected void saveData(ValueOutput output) {
        super.saveData(output);
        ValueOutput.ValueOutputList nbt = output.childrenList("connections");
        for (Connection connection : this.fenceConnections) {
            connection.writeData(nbt.addChild());
        }
    }

    @Override
    protected void loadData(ValueInput input) {
        super.loadData(input);
        this.fenceConnections.clear();
        input.childrenListOrEmpty("connections").forEach(tag -> {
            Connection connection = Connection.fromData(tag, this);
            if(connection.isValid()) {
                this.fenceConnections.add(connection);
            }
        });

        if(this.level != null) {
            this.triggerModelUpdate();
        }
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        if(this.collidableCache == null) {
            VoxelShape shape = Shapes.empty();
            for (Connection connection : this.fenceConnections) {
                shape = Shapes.or(shape, connection.getCollisionShape());
            }
            this.collidableCache = shape;
        }

        return this.collidableCache;
    }

    @Override
    public void addConnection(Connection connection) {
        this.fenceConnections.add(connection);
        this.triggerModelUpdate();
        this.setChanged();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Set<Connection.CompiledRenderData> compiledRenderData() {
        return this.getConnections().stream()
            .map(c -> c.compileRenderData(this.level))
            .collect(Collectors.toSet());
    }


    @Override
    public Set<Connection> getConnections() {
        return Collections.unmodifiableSet(this.fenceConnections);
    }

    // Formerly BlockEntityElectricFenceMixin (1.20.1 forge) — NeoForge model data in 26.2.
    // The getModelData()/ModelProperty side was dropped for the fabric port (nothing
    // consumed it: 26.2 renders fences through the BER, not baked models).
    public void requestModelDataUpdate() {
        if(this.level != null) {
          this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
        this.resetCollidableCache();
    }

    @Override
    public void triggerModelUpdate() {
        this.requestModelDataUpdate();
    }

    /**
     * Breaks the surrounding fence. Used for entities
     * who "attack" the fence.
     * @param intensity Intensity at which the fence breaks.
     */
    public void breakFence(int intensity) { // TODO: Add more randomness.
        for (Connection connection : fenceConnections) {
            for (double offset : connection.getType().getOffsets()) {
                List<BlockPos> blocks = LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), offset);
                for (int k = 0; k < blocks.size(); k++) {
                    for (int i = 0; i < connection.getType().getHeight(); i++) {
                        BlockPos position = blocks.get(k).above(i);
                        if ((k == blocks.size() / 2 - 1 || k == blocks.size() / 2 + 1) && i < intensity / 2 + 1) {
                            this.level.destroyBlock(position, true);
                        } else if (k == blocks.size() / 2 && i < intensity) {
                            this.level.destroyBlock(position, true);
                        }
                    }
                }
            }
        }
    }
}
