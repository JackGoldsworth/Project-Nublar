package net.dumbcode.projectnublar.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.api.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.Connection;
import net.dumbcode.projectnublar.util.LineUtils;
import net.dumbcode.projectnublar.block.api.MachineEnergyHandler;
import net.dumbcode.projectnublar.block.api.MathUtils;
import net.dumbcode.projectnublar.block.api.NublarEnergyBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;


import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BlockEntityElectricFencePole extends BlockEntityElectricFence implements ConnectableBlockEntity, GeoBlockEntity, NublarEnergyBlock {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public boolean flippedAround;

    public VoxelShape cachedShape = Shapes.block();

    private double cachedRotation = 0;

    public boolean shouldRefreshNextTick = false;

    public boolean isFlippedAround() {
        return flippedAround;
    }

    public VoxelShape getCachedShape() {
        return cachedShape;
    }

    public double getCachedRotation() {
        return cachedRotation;
    }

    public boolean isShouldRefreshNextTick() {
        return shouldRefreshNextTick;
    }

    private MachineEnergyHandler energyHandler;


    public BlockEntityElectricFencePole(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(),pos, state);
    }

    @Override
    protected void saveData(ValueOutput output) {
        output.putBoolean("rotation_flipped", this.flippedAround);
        getEnergyHandler().serialize(output.child("energy"));
        super.saveData(output);
    }


    @Override
    protected void loadData(ValueInput input) {
        this.flippedAround = input.getBooleanOr("rotation_flipped", false);
        getEnergyHandler().deserialize(input.childOrEmpty("energy"));
        super.loadData(input);
    }

    // 26.2: the wire teardown that used to live in ElectricFencePostBlock#onRemove — it needs
    // this block entity's connections, so it runs here while the pole is still alive
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level == null) {
            return;
        }
        for (Connection connection : this.getConnections()) {
            BlockPos fromPos = connection.getFrom();
            if (fromPos.equals(pos)) {
                fromPos = connection.getTo();
            }
            if (this.level.getBlockState(fromPos).getBlock() != state.getBlock() || true) {
                for (BlockPos blockPos : LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset())) {
                    if (blockPos.equals(connection.getTo()) || blockPos.equals(connection.getFrom())) {
                        BlockEntity be = this.level.getBlockEntity(blockPos);

                        if (be instanceof BlockEntityElectricFencePole fencePole1 && fencePole1 != this) {
                            connection.setBroken(true);
                        }

                        continue;
                    }


                    BlockEntity te = this.level.getBlockEntity(blockPos);
                    if (te instanceof ConnectableBlockEntity connectableBlockEntity) {
                        boolean left = false;
                        for (Connection bitcon : connectableBlockEntity.getConnections()) {
                            if (connection.lazyEquals(bitcon)) {
                                bitcon.setBroken(true);
                            }
                            left |= !bitcon.isBroken();
                        }
                        if (!left) {
                            this.level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean removedByFenceRemovers() {
        return false;
    }


    public void setFlippedAround(boolean flippedAround) {
        this.flippedAround = flippedAround;
        this.triggerModelUpdate();
        this.level.sendBlockUpdated(this.getBlockPos(), Blocks.AIR.defaultBlockState(), this.getBlockState(), 3);
    }

    protected static final VoxelShape DEFAULT_SHAPE = Shapes.create(.875 -.03125 * 3,0,.5 - .0625, 1-.03125 * 3,1,.5 + .0625);


    public void tick(Level world, BlockPos blockPos, BlockState pState, BlockEntityElectricFencePole be) {
        if(this.shouldRefreshNextTick) {
            this.shouldRefreshNextTick = false;
            this.triggerModelUpdate();
            this.cachedRotation = this.computeRotation();
        }
        double oldRotation = this.cachedRotation;
        this.cachedRotation = this.computeRotation();
        if (oldRotation != this.cachedRotation) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
        boolean powered = this.getEnergyHandler().getStoredEnergy() > 0;
        if(powered) {
            boolean update = false;
            if (this.level.getBlockState(this.getBlockPos()).getValue(ElectricFencePostBlock.POWERED_PROPERTY) != powered) {
                update = true;
            }
            getEnergyHandler().internalExtract(10, false);
            BlockState state = this.level.getBlockState(this.getBlockPos());
            if (state.getBlock() instanceof ElectricFencePostBlock && state.getValue(((ElectricFencePostBlock) state.getBlock()).getIndexProperty()) == 0) {
                if (update) {
                    for (int y = 0; y < ((ElectricFencePostBlock) state.getBlock()).getType().getHeight(); y++) {
                        BlockPos pos = this.getBlockPos().above(y);
                        BlockState s = this.level.getBlockState(pos);
                        if (s.getBlock() == state.getBlock()) { //When placing the blocks can be air
                            this.level.setBlock(pos, s.setValue(ElectricFencePostBlock.POWERED_PROPERTY, powered), 3);
                        }
                    }
                }
                //Pass power to other poles connected to this.
                if (this.getEnergyHandler().getStoredEnergy() > 300) {
                    Set<MachineEnergyHandler> storages = Sets.newLinkedHashSet();
                    for (Connection connection : this.getConnections()) {
                        BlockEntity te = this.level.getBlockEntity(connection.getPosition().equals(connection.getFrom()) ? connection.getTo() : connection.getFrom());
                        if (te != null) {
                            if (te instanceof BlockEntityElectricFencePole e) {
                                storages.add(e.getEnergyHandler());
                            }
                        }
                    }
                    List<MachineEnergyHandler> list = Lists.newArrayList(storages);
                    list.sort(Comparator.comparing(MachineEnergyHandler::getStoredEnergy));
                    for (MachineEnergyHandler storage : list) {
                        long sendEnergy = storage.internalInsert(this.getEnergyHandler().internalExtract(300 / list.size(), true), true);
                        this.getEnergyHandler().internalExtract((int) sendEnergy, false);
                        storage.internalInsert((int) sendEnergy, false);
                    }
                }
            }
        }
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        VoxelShape shape = super.getOrCreateCollision();
        if (shape.isEmpty()) {
            shape = Shapes.or(shape,DEFAULT_SHAPE);
        }
        return shape;
    }


    public double computeRotation() {
        double rotation = 0;

        if(this.level == null || !this.level.isLoaded(this.getBlockPos())) {
            return this.flippedAround ? 0F : 180F;
        }

        BlockState state = this.level.getBlockState(this.getBlockPos());
        if (state.getBlock() instanceof ElectricFencePostBlock) {
            ElectricFencePostBlock pole = (ElectricFencePostBlock) state.getBlock();
            BlockEntity te = this.level.getBlockEntity(this.getBlockPos().below(state.getValue((pole).getIndexProperty())));
            if (te instanceof BlockEntityElectricFencePole) {
                BlockEntityElectricFencePole ef = (BlockEntityElectricFencePole) te;
                if (!ef.getConnections().isEmpty()) {

                    List<Connection> differingConnections = Lists.newArrayList();
                    for (Connection connection : ef.getConnections()) {
                        boolean has = false;
                        for (Connection dc : differingConnections) {
                            if (connection.getFrom().equals(dc.getFrom()) && connection.getTo().equals(dc.getTo())) {
                                has = true;
                                break;
                            }
                        }
                        if (!has) {
                            differingConnections.add(connection);
                        }
                    }

                    if (differingConnections.size() == 1) {
                        Connection connection = differingConnections.get(0);
                        double[] in = connection.getIn();
                        rotation += (float) Math.toDegrees(Math.atan((in[2] - in[3]) / (in[1] - in[0]))) + 90;
                    } else {
                        Connection connection1 = differingConnections.get(0);
                        Connection connection2 = differingConnections.get(1);

                        double[] in1 = connection1.getIn();
                        double[] in2 = connection2.getIn();

                        double angle1 = MathUtils.horizontalDegree(in1[1] - in1[0], in1[2] - in1[3], connection1.getPosition().equals(connection1.getMin()));
                        double angle2 = MathUtils.horizontalDegree(in2[1] - in2[0], in2[2] - in2[3], connection2.getPosition().equals(connection2.getMin()));

                        rotation += (float) (angle1 + (angle2 - angle1) / 2D);
                    }
                }

                rotation += pole.getType().getRotationOffset();
                if (ef.isFlippedAround()) {
                    rotation += 180;
                }
            }
        }
        return rotation;
    }

    // Formerly BlockEntityElectricFencePoleMixin (1.20.1 forge) — native NeoForge model data in 26.2
    @Override
    public void requestModelDataUpdate() {
        this.cachedRotation = this.computeRotation();

        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof ElectricFencePostBlock) {
            net.dumbcode.projectnublar.block.api.ConnectionType type = ((ElectricFencePostBlock) state.getBlock()).getType();

            float t = type.getHalfSize();
            double x = Math.sin(Math.toRadians(this.cachedRotation + 90F - type.getRotationOffset())) * type.getRadius();
            double z = Math.cos(Math.toRadians(this.cachedRotation + 90F - type.getRotationOffset())) * type.getRadius();
            this.cachedShape = Shapes.box(x-t, 0, z-t, x+t, 1, z+t).move(0.5, 0, 0.5);
        }

        super.requestModelDataUpdate();
    }

    @Override
    public void triggerModelUpdate() {
        this.requestModelDataUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.shouldRefreshNextTick = true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public MachineEnergyHandler getEnergyHandler() {
        return energyHandler == null ? this.energyHandler = new MachineEnergyHandler(350, 350, 0, this::setChanged) : this.energyHandler;
    }
}
