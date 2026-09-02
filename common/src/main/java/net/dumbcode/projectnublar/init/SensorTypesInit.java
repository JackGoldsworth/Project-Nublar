package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.ai.sensors.NearbyDinosaurSensor;
import net.dumbcode.projectnublar.entity.ai.sensors.NearestFeederSensor;
import net.dumbcode.projectnublar.entity.ai.sensors.NearestWaterSourceSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;

import java.util.function.Supplier;

public class SensorTypesInit {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, Constants.MODID);

    public static final DeferredHolder<SensorType<?>, SensorType<NearestWaterSourceSensor<?>>> NEAREST_WATER_SOURCE = SENSOR_TYPES.register("nearest_drinkable_source_block", () -> new SensorType<>(NearestWaterSourceSensor::new));
    public static final DeferredHolder<SensorType<?>, SensorType<NearestFeederSensor<?>>> NEAREST_FEEDER_SENSOR = SENSOR_TYPES.register("nearest_feeder_block", () -> new SensorType<>(NearestFeederSensor::new));
    public static final DeferredHolder<SensorType<?>, SensorType<NearbyDinosaurSensor<?>>> NEARBY_DINOSAURS_SENSOR = SENSOR_TYPES.register("nearest_dinosaurs", () -> new SensorType<>(NearbyDinosaurSensor::new));

    public static void registerTo() {
        SENSOR_TYPES.register();
    }
}
