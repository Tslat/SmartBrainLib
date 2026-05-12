package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A sensor that sets or clears the [MemoryModuleType#IS_IN_WATER] memory depending on certain criteria
///
/// @param <BO> The brain owner entity
public class InWaterSensor<BO extends LivingEntity> extends PredicateSensor<BO, Void> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.IS_IN_WATER);

	public InWaterSensor() {
		super((entity, _) -> entity.isInWater());
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public InWaterSensor<BO> setPredicate(BiPredicate<BO, Void> predicate) {
		return (InWaterSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public InWaterSensor<BO> scanRate(int scanRate) {
		return (InWaterSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public InWaterSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (InWaterSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public InWaterSensor<BO> afterScanning(Consumer<BO> callback) {
		return (InWaterSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public InWaterSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (InWaterSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.IN_WATER.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
        //noinspection DataFlowIssue
        if (predicate().test(entity, null)) {
			BrainUtil.setMemory(entity, MemoryModuleType.IS_IN_WATER, Unit.INSTANCE);
		}
		else {
			BrainUtil.clearMemory(entity, MemoryModuleType.IS_IN_WATER);
		}
	}
	//</editor-fold>
}
