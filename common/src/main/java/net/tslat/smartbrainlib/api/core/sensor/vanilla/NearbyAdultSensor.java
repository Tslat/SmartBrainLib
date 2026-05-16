package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.AdultSensorAnyType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.NearestVisibleEntityFilteredSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A sensor that sets the [MemoryModuleType#NEAREST_VISIBLE_ADULT] memory by checking the existing visible entities for nearby adults of the same entity type
///
/// @see net.minecraft.world.entity.ai.sensing.AdultSensor
/// @param <BO> The brain owner entity
public class NearbyAdultSensor<BO extends LivingEntity> extends NearestVisibleEntityFilteredSensor<BO, LivingEntity> {
	protected BiPredicate<BO, LivingEntity> predicate = (entity, target) -> target.getType() == entity.getType() && !target.isBaby();

	/// Override the default predicate for testing for nearby adults
	public NearbyAdultSensor<BO> setAdultPredicate(BiPredicate<BO, LivingEntity> predicate) {
		this.predicate = predicate;

		return this;
	}

	/// Modify the detection [#setAdultPredicate] to support any friendly adult entity, rather than the same entity type as [BO]
	///
	/// @see AdultSensorAnyType
	public NearbyAdultSensor<BO> supportAnyFriendlyAdult() {
		return setAdultPredicate((entity, target) -> target.is(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS) && !target.isBaby());
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public NearbyAdultSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (NearbyAdultSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public NearbyAdultSensor<BO> scanRate(int scanRate) {
		return (NearbyAdultSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public NearbyAdultSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyAdultSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public NearbyAdultSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyAdultSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public NearbyAdultSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyAdultSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return Which memory the sensor should set if an entity meets the given criteria.
	@Override
	public MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_VISIBLE_ADULT;
	}

	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_ADULT.get();
	}

	/// @return The predicate to determine which entities are valid from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	@Override
	protected BiPredicate<BO, LivingEntity> predicate() {
		return this.predicate;
	}

	/// Find and return matches based on the provided list of entities.<br/>
	/// The returned value is saved as the memory for this sensor
	///
	/// @param entity The brain owner entity
	/// @param matcher The nearby entities list retrieved from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	/// @return The match(es) to save in memory
	@Override
	protected @Nullable AgeableMob findMatches(BO entity, NearestVisibleLivingEntities matcher) {
		return (AgeableMob)matcher.findClosest(target -> predicate().test(entity, target)).orElse(null);
	}
	//</editor-fold>
}
