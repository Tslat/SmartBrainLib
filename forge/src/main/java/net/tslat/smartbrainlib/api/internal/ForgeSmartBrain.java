package net.tslat.smartbrainlib.api.internal;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.common.util.BrainBuilder;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.ActivityBuilder;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/// Wrapper around [SmartBrain] specifically for Forge to support the [BrainBuilder] API
@SuppressWarnings({"NullableProblems", "UnstableApiUsage"})
public class ForgeSmartBrain<BO extends LivingEntity & SmartBrainOwner<BO>> extends SmartBrain<BO> {
    public ForgeSmartBrain(Collection<MemoryModuleType<?>> memories, Collection<? extends ExtendedSensor<BO>> extendedSensors, List<ActivityBuilder<BO>> activities, @Nullable SmartBrainSchedule<BO, ?> schedule, RandomSource random) {
        super(memories, extendedSensors, activities, schedule, random);
    }

    @SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
    @Override
    public BrainBuilder<BO> createBuilder() {
        final SmartBrain<BO> brain = this;
        final var builder = new net.minecraftforge.common.util.BrainBuilder<>(brain) {
            @SuppressWarnings("unchecked")
            @ApiStatus.Internal
            @Override
            public Brain<BO> makeBrain(BO entity, Packed packedBrain) {
                final var sensorMap = brain.getSensors().sensors;

                for (SensorType<?> sensorType : getSensorTypes()) {
                    if (!sensorMap.containsKey(sensorType)) {
                        Sensor<?> sensor = sensorType.create();

                        if (sensor instanceof ExtendedSensor<?> extendedSensor)
                            brain.getSensors().addSensor((ExtendedSensor<BO>)extendedSensor, entity.getRandom(), true);
                    }
                }

                sensorMap.keySet().removeIf(sensorType -> !getSensorTypes().contains(sensorType));
                brain.copyFromBuilder(this);

                return brain;
            }
        };

        builder.getMemoryTypes().addAll(brain.memories.keySet());
        builder.getSensorTypes().addAll((Collection)brain.getSensors().sensorTypes());
        builder.addAvailableBehaviorsByPriorityFrom(brain.availableBehaviorsByPriority);
        builder.addActivityRequirementsFrom(brain.activityRequirements);
        builder.addActivityMemoriesToEraseWhenStoppedFrom(brain.activityMemoriesToEraseWhenStopped);
        builder.getCoreActivities().addAll(brain.coreActivities);
        builder.setDefaultActivity(brain.defaultActivity);
        builder.setActiveActivites(brain.getActiveActivities());

        return builder;
    }
}
