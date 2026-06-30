package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.NearestVisibleEntityFilteredSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A sensor that sets the [MemoryModuleType#VISIBLE_VILLAGER_BABIES] memory by checking the existing visible entities for nearby babies of the same entity type
///
/// @see net.minecraft.world.entity.ai.sensing.VillagerBabiesSensor
/// @param <BO> The brain owner entity
public class NearbyBabySensor<BO extends LivingEntity> extends NearestVisibleEntityFilteredSensor<BO, List<LivingEntity>> {
	protected BiPredicate<BO, LivingEntity> predicate = (entity, target) -> target.getType() == entity.getType() && target.isBaby();

	/// Override the default predicate for testing for nearby babies
	@ApiStatus.NonExtendable
	public NearbyBabySensor<BO> setBabyPredicate(BiPredicate<BO, LivingEntity> predicate) {
		this.predicate = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@ApiStatus.NonExtendable
	@Override
	public NearbyBabySensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (NearbyBabySensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	@ApiStatus.NonExtendable
	@Override
	public NearbyBabySensor<BO> scanRate(int scanRate) {
		return (NearbyBabySensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyBabySensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyBabySensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyBabySensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyBabySensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@ApiStatus.NonExtendable
	@Override
	public NearbyBabySensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyBabySensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_BABY.get();
	}

	/// @return Which memory the sensor should set if an entity meets the given criteria
	@Override
	public MemoryModuleType<List<LivingEntity>> getMemory() {
		return MemoryModuleType.VISIBLE_VILLAGER_BABIES;
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
	@Nullable
	@Override
	protected List<LivingEntity> findMatches(BO entity, NearestVisibleLivingEntities matcher) {
		return ImmutableList.copyOf(matcher.findAll(target -> predicate().test(entity, target)));
	}
	//</editor-fold>
}
