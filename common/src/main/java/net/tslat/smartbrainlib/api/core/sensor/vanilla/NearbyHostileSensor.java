package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectFloatPair;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.NearestVisibleEntityFilteredSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A sensor that sets the [MemoryModuleType#NEAREST_HOSTILE] memory by checking the existing visible entities for nearby hostiles<br/>
/// By default, this is used for villager hostile detection, but it can be configured at instantiation for any types
///
/// @see net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor
/// @param <BO> The brain owner entity
public class NearbyHostileSensor<BO extends LivingEntity> extends NearestVisibleEntityFilteredSensor<BO, LivingEntity> {
	protected final Reference2FloatOpenHashMap<EntityType<?>> hostileDistanceMap = Util.make(new Reference2FloatOpenHashMap<>(11), map -> {
		map.put(EntityType.DROWNED, 8f);
		map.put(EntityType.HUSK, 8f);
		map.put(EntityType.VEX, 8f);
		map.put(EntityType.ZOMBIE, 8f);
		map.put(EntityType.ZOMBIE_VILLAGER, 8f);
		map.put(EntityType.VINDICATOR, 10f);
		map.put(EntityType.ZOGLIN, 10f);
		map.put(EntityType.EVOKER, 12f);
		map.put(EntityType.ILLUSIONER, 12f);
		map.put(EntityType.RAVAGER, 12f);
		map.put(EntityType.PILLAGER, 15f);
	});
	protected BiPredicate<BO, LivingEntity> hostilePredicate = (target, entity) -> {
		final float distance = this.hostileDistanceMap.getOrDefault(target.getType(), -1);

		return distance >= 0 && target.distanceToSqr(entity) <= distance * distance;
	};

	/// Set the predicate that determines a valid hostile entity
	@ApiStatus.NonExtendable
	public NearbyHostileSensor<BO> setHostilePredicate(BiPredicate<BO, LivingEntity> predicate) {
		this.hostilePredicate = predicate;

		return this;
	}

	/// Clear the hostile types map and replace it with all the provided entries
	///
	/// @param entries The collection of entity types and distances to set the hostile types map to
	@ApiStatus.NonExtendable
	@SuppressWarnings("unchecked")
    public NearbyHostileSensor<BO> setHostiles(ObjectFloatPair<EntityType<?>>... entries) {
		this.hostileDistanceMap.clear();

		for (ObjectFloatPair<EntityType<?>> entry : entries) {
			this.hostileDistanceMap.put(entry.key(), entry.valueFloat());
		}

		return this;
	}

	/// Add an entity type to the hostile types map
	@ApiStatus.NonExtendable
	public NearbyHostileSensor<BO> addHostile(EntityType<?> entityType, float distance) {
		this.hostileDistanceMap.put(entityType, distance);

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public NearbyHostileSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (NearbyHostileSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public NearbyHostileSensor<BO> scanRate(int scanRate) {
		return (NearbyHostileSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public NearbyHostileSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyHostileSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public NearbyHostileSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyHostileSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public NearbyHostileSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyHostileSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_HOSTILE.get();
	}

	/// @return Which memory the sensor should set if an entity meets the given criteria
	@Override
	public MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_HOSTILE;
	}

	/// @return The predicate to determine which entities are valid from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	@Override
	protected BiPredicate<BO, LivingEntity> predicate() {
		return this.hostilePredicate;
	}

	/// Find and return matches based on the provided list of entities<br/>
	/// The returned value is saved as the memory for this sensor
	///
	/// @param entity The brain owner entity
	/// @param matcher The nearby entities list retrieved from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	/// @return The match(es) to save in memory
	@Nullable
	@Override
	protected LivingEntity findMatches(BO entity, NearestVisibleLivingEntities matcher) {
		return matcher.findClosest(target -> predicate().test(entity, target)).orElse(null);
	}
	//</editor-fold>
}
