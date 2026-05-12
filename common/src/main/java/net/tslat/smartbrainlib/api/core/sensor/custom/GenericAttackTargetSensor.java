package net.tslat.smartbrainlib.api.core.sensor.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.NearestVisibleEntityFilteredSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.SensoryUtil;
import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Sets the [MemoryModuleType#NEAREST_ATTACKABLE] memory based on visible nearby entities
///
/// @param <BO> The brain owner entity
public class GenericAttackTargetSensor<BO extends LivingEntity> extends NearestVisibleEntityFilteredSensor<BO, LivingEntity> {
	protected BiPredicate<BO, LivingEntity> targetPredicate = SensoryUtil::isEntityAttackable;

	/// Set a custom predicate for valid attackable targets
	public GenericAttackTargetSensor<BO> onlyTargetIf(BiPredicate<BO, LivingEntity> predicate) {
		this.targetPredicate = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage.
	@Override
	public GenericAttackTargetSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (GenericAttackTargetSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public GenericAttackTargetSensor<BO> scanRate(int scanRate) {
		return (GenericAttackTargetSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public GenericAttackTargetSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (GenericAttackTargetSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public GenericAttackTargetSensor<BO> afterScanning(Consumer<BO> callback) {
		return (GenericAttackTargetSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public GenericAttackTargetSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (GenericAttackTargetSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.GENERIC_ATTACK_TARGET.get();
	}

	/// @return Which memory the sensor should set if an entity meets the given criteria
	@Override
	protected MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}

	/// @return The predicate to determine which entities are valid from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	@Override
	protected BiPredicate<BO, LivingEntity> predicate() {
		return this.targetPredicate;
	}

	/// Find and return matches based on the provided list of entities<br/>
	/// The returned value is saved as the memory for this sensor
	///
	/// @param entity The brain owner entity
	/// @param matcher The nearby entities list retrieved from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	/// @return The match(es) to save in memory
	@Override
	protected @Nullable LivingEntity findMatches(BO entity, NearestVisibleLivingEntities matcher) {
		return matcher.findClosest(target -> predicate().test(entity, target)).orElse(null);
	}
	//</editor-fold>
}
