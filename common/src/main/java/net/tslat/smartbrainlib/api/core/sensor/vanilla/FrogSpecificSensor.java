package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.frog.Frog;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.NearestVisibleEntityFilteredSensor;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.SensoryUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A replication of vanilla's [net.minecraft.world.entity.ai.sensing.FrogAttackablesSensor]<br/>
/// Not really useful, but included for completeness' sake and legibility
///
/// Handles the Frog's tongue target
///
/// @param <BO> The brain owner entity
public class FrogSpecificSensor<BO extends LivingEntity> extends NearestVisibleEntityFilteredSensor<BO, LivingEntity> {
	protected ToFloatBiFunction<BO, LivingEntity> detectionRange = (_, _) -> 10f;
	protected BiPredicate<BO, LivingEntity> validTargetCondition = (entity, target) -> Frog.canEat(target) && !BrainUtil.memoryOrDefault(entity, MemoryModuleType.UNREACHABLE_TONGUE_TARGETS, List.of()).contains(target.getUUID());

	/// Set the block range at which the entity can identify targets
	public FrogSpecificSensor<BO> detectionRange(ToFloatBiFunction<BO, LivingEntity> range) {
		this.detectionRange = range;

		return this;
	}

	/// Set a targeting condition for valid targets
	public FrogSpecificSensor<BO> onlyTargetIf(BiPredicate<BO, LivingEntity> predicate) {
		this.validTargetCondition = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public FrogSpecificSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (FrogSpecificSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public FrogSpecificSensor<BO> scanRate(int scanRate) {
		return (FrogSpecificSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor.
	/// The provider will be sampled every time the sensor does a scan.
	@Override
	public FrogSpecificSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (FrogSpecificSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan.
	@Override
	public FrogSpecificSensor<BO> afterScanning(Consumer<BO> callback) {
		return (FrogSpecificSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public FrogSpecificSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (FrogSpecificSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.FROG_SPECIFIC.get();
	}

	/// @return Which memory the sensor should set if an entity meets the given criteria.
	@Override
	public MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain
	/// Bonus points if it's a statically cached list.
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return List.of(getMemory(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.UNREACHABLE_TONGUE_TARGETS);
	}

	/// @return The predicate to determine which entities are valid from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	@Override
	protected BiPredicate<BO, LivingEntity> predicate() {
		return (entity, target) -> {
			if (!entity.closerThan(target, this.detectionRange.applyAsFloat(entity, target)))
				return false;

			if (!this.validTargetCondition.test(entity, target))
				return false;

			return SensoryUtil.isEntityAttackable(entity, target);
		};
	}

	/// Find and return matches based on the provided list of entities.
	/// The returned value is saved as the memory for this sensor.
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
