package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.golem.IronGolem;
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

/// A sensor that sets the [MemoryModuleType#GOLEM_DETECTED_RECENTLY] memory by checking if any of the detected nearby entities are [Iron Golems][IronGolem]
///
/// @see net.minecraft.world.entity.ai.sensing.GolemSensor
/// @param <BO> The brain owner entity
public class NearbyGolemSensor<BO extends LivingEntity> extends PredicateSensor<BO, LivingEntity> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.GOLEM_DETECTED_RECENTLY);

	protected int timeToRemember = 600;

	public NearbyGolemSensor() {
		scanRate(200);
		setPredicate((_, target) -> target instanceof IronGolem && target.isAlive());
	}

	/// Set the number of ticks the entity should remember that the golem is there
	public NearbyGolemSensor<BO> setMemoryTime(int ticks) {
		this.timeToRemember = ticks;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public NearbyGolemSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (NearbyGolemSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public NearbyGolemSensor<BO> scanRate(int scanRate) {
		return (NearbyGolemSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public NearbyGolemSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyGolemSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public NearbyGolemSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyGolemSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public NearbyGolemSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyGolemSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_GOLEM.get();
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
		BrainUtil.withMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entityList -> {
			if (entityList.isEmpty())
				return;

			for (LivingEntity target : entityList) {
				if (predicate().test(entity, target)) {
					BrainUtil.setForgettableMemory(entity, MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, this.timeToRemember);

					return;
				}
			}
		});
	}
	//</editor-fold>
}
