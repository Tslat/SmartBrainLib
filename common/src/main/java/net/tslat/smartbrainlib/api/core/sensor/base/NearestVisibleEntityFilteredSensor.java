package net.tslat.smartbrainlib.api.core.sensor.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An abstract class that is used to pick out certain entities from the existing [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory<br/>
/// This requires that another sensor has pre-filled that memory
///
/// @param <T> The visible entity
/// @param <BO> The brain owner entity
/// @see net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor
public abstract class NearestVisibleEntityFilteredSensor<BO extends LivingEntity, T> extends PredicateSensor<BO, LivingEntity> {
	/// @return Which memory the sensor should set if an entity meets the given criteria
	protected abstract MemoryModuleType<T> getMemory();

	/// @return The predicate to determine which entities are valid from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	@Override
	protected abstract BiPredicate<BO, LivingEntity> predicate();

	/// Find and return matches based on the provided list of entities<br/>
	/// The returned value is saved as the memory for this sensor
	///
	/// @param entity The brain owner entity
	/// @param matcher The nearby entities list retrieved from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	/// @return The match(es) to save in memory
	protected abstract @Nullable T findMatches(BO entity, NearestVisibleLivingEntities matcher);

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage.
	@Override
	public NearestVisibleEntityFilteredSensor<BO, T> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (NearestVisibleEntityFilteredSensor<BO, T>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public NearestVisibleEntityFilteredSensor<BO, T> scanRate(int scanRate) {
		return (NearestVisibleEntityFilteredSensor<BO, T>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public NearestVisibleEntityFilteredSensor<BO, T> scanRate(ToIntFunction<BO> function) {
		return (NearestVisibleEntityFilteredSensor<BO, T>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public NearestVisibleEntityFilteredSensor<BO, T> afterScanning(Consumer<BO> callback) {
		return (NearestVisibleEntityFilteredSensor<BO, T>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public NearestVisibleEntityFilteredSensor<BO, T> onlyScanIf(Predicate<BO> predicate) {
		return (NearestVisibleEntityFilteredSensor<BO, T>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return List.of(getMemory(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		final T matches = testForEntity(entity);

		if (matches == null) {
			BrainUtil.clearMemory(entity, getMemory());
		}
		else {
			BrainUtil.setMemory(entity, getMemory(), matches);
		}
	}

	/// Test the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] list, [filtering][#predicate()] return the match(es) to store in [another memory][#getMemory()]
	protected @Nullable T testForEntity(BO entity) {
		NearestVisibleLivingEntities matcher = BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

		if (matcher == null)
			return null;

		return findMatches(entity, matcher);
	}
	//</editor-fold>
}
