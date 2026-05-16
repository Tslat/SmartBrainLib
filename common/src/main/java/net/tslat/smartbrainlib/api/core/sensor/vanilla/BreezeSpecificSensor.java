package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.SensoryUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A replication of vanilla's [net.minecraft.world.entity.ai.sensing.BreezeAttackEntitySensor]<br/>
/// Not really useful, but included for completeness' sake and legibility
///
/// Handles the [Breeze]'s attack target sensing
///
/// @param <BO> The brain owner entity
public class BreezeSpecificSensor<BO extends LivingEntity> extends PredicateSensor<BO, LivingEntity> {
    protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_ATTACKABLE);

    public BreezeSpecificSensor() {
        setPredicate((entity, target) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target) && SensoryUtil.isEntityAttackable(entity, target));
    }

    //<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
    /// Set the predicate for the sensor. The subclass of this class determines its usage
    @Override
    public BreezeSpecificSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
        return (BreezeSpecificSensor<BO>)super.setPredicate(predicate);
    }

    /// Set the scan rate for this sensor
    public BreezeSpecificSensor<BO> scanRate(int scanRate) {
        return scanRate(_ -> scanRate);
    }

    /// Set the scan rate provider for this sensor
    ///
    /// The provider will be sampled every time the sensor does a scan
    public BreezeSpecificSensor<BO> scanRate(ToIntFunction<BO> function) {
        return (BreezeSpecificSensor<BO>)super.scanRate(function);
    }

    /// Set a callback function for when the sensor completes a scan
    public BreezeSpecificSensor<BO> afterScanning(Consumer<BO> callback) {
        return (BreezeSpecificSensor<BO>)super.afterScanning(callback);
    }

    /// Set a condition that must be met in order to perform a scan
    ///
    /// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
    public BreezeSpecificSensor<BO> onlyScanIf(Predicate<BO> predicate) {
        return (BreezeSpecificSensor<BO>)super.onlyScanIf(predicate);
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
    /// @return The [SensorType] of the sensor, used for reverse lookups.
    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.BREEZE_SPECIFIC.get();
    }

    /// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
    /// Bonus points if it's a statically cached list
    ///
    /// @return The list of memory types saves by this sensor
    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return List.of();
    }

    /// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
    ///
    /// @param level The level the entity is in
    /// @param entity The owner of the brain
    @Override
    protected void doTick(ServerLevel level, BO entity) {
        final List<LivingEntity> entities = BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, List.of());

        BrainUtil.setOrClearMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, entities.isEmpty() ? null : entities.getFirst());
    }
    //</editor-fold>
}
