package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.NearestVisibleEntityFilteredSensor;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A replication of vanilla's [net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor]<br/>
/// Not really useful, but included for completeness' sake and legibility
///
/// Handles the [Axolotl]'s hostility and targets
///
/// @param <BO> The entity
public class AxolotlSpecificSensor<BO extends LivingEntity> extends NearestVisibleEntityFilteredSensor<BO, LivingEntity> {
	protected ToFloatBiFunction<BO, LivingEntity> detectionRange = (_, _) -> 8f;
	protected BiPredicate<BO, LivingEntity> validTargetCondition = (entity, target) -> target.isInWater() && (target.is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES) || (!BrainUtil.hasMemory(entity, MemoryModuleType.HAS_HUNTING_COOLDOWN) && target.is(EntityTypeTags.AXOLOTL_HUNT_TARGETS)));

	/// Set the block range at which the entity can identify targets
	public AxolotlSpecificSensor<BO> detectionRange(ToFloatBiFunction<BO, LivingEntity> range) {
		this.detectionRange = range;

		return this;
	}

	/// Set a targeting condition for valid targets
	public AxolotlSpecificSensor<BO> onlyTargetIf(BiPredicate<BO, LivingEntity> predicate) {
		this.validTargetCondition = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public AxolotlSpecificSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (AxolotlSpecificSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public AxolotlSpecificSensor<BO> scanRate(int scanRate) {
		return (AxolotlSpecificSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public AxolotlSpecificSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (AxolotlSpecificSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public AxolotlSpecificSensor<BO> afterScanning(Consumer<BO> callback) {
		return (AxolotlSpecificSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public AxolotlSpecificSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (AxolotlSpecificSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.AXOLOTL_SPECIFIC.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return List.of(getMemory(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.HAS_HUNTING_COOLDOWN);
	}

	/// @return Which memory the sensor should set if an entity meets the given criteria.
	@Override
	public MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}

	/// @return The predicate to determine which entities are valid from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	@Override
	protected BiPredicate<BO, LivingEntity> predicate() {
		return (entity, target) -> {
			if (!entity.closerThan(target, Mth.sqrt(this.detectionRange.applyAsFloat(entity, target))))
				return false;

			if (!this.validTargetCondition.test(entity, target))
				return false;

			return Sensor.isEntityAttackable((ServerLevel)target.level(), entity, target);
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
