package net.tslat.smartbrainlib.core.sensor.vanilla;

import java.util.Map;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.tslat.smartbrainlib.core.sensor.EntityFilteringSensor;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that sets the {@link MemoryModuleType#NEAREST_HOSTILE} memory by checking the existing visible entities for nearby hostiles. <br>
 * By default, this is used for villager hostile detection, but it can be configured at instantiation for any types.
 * @see net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor
 * @param <E> The entity
 */
@SuppressWarnings("unchecked")
public class NearbyHostileSensor<E extends LivingEntity> extends EntityFilteringSensor<LivingEntity, E> {
	private final Map<EntityType<?>, Float> hostileDistanceMap = new Object2FloatOpenHashMap<>(11);

	public NearbyHostileSensor() {
		setHostiles(
				Pair.of(EntityType.DROWNED, 8f),
				Pair.of(EntityType.HUSK, 8f),
				Pair.of(EntityType.VEX, 8f),
				Pair.of(EntityType.ZOMBIE, 8f),
				Pair.of(EntityType.ZOMBIE_VILLAGER, 8f),
				Pair.of(EntityType.VINDICATOR, 10f),
				Pair.of(EntityType.ZOGLIN, 10f),
				Pair.of(EntityType.EVOKER, 12f),
				Pair.of(EntityType.ILLUSIONER, 12f),
				Pair.of(EntityType.RAVAGER, 12f),
				Pair.of(EntityType.PILLAGER, 15f));
	}

	/**
	 * Clear the hostile types map, and add all of the given entries.
	 * @param entries The collection of entity types and distances to set the hostile types map to
	 * @return this
	 */
	public NearbyHostileSensor<E> setHostiles(Pair<EntityType<?>, Float>... entries) {
		this.hostileDistanceMap.clear();

		for (Pair<EntityType<?>, Float> entry : entries) {
			this.hostileDistanceMap.put(entry.getFirst(), entry.getSecond());
		}

		return this;
	}

	/**
	 * Add an entity type to the hostile types map.
	 *
	 * @param entry The entity type and distance to which it should be considered.
	 * @return this
	 */
	public NearbyHostileSensor<E> addHostile(Pair<EntityType<?>, Float> entry) {
		this.hostileDistanceMap.put(entry.getFirst(), entry.getSecond());

		return this;
	}

	@Override
	public MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_HOSTILE;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_HOSTILE.get();
	}

	@Override
	protected BiPredicate<LivingEntity, E> predicate() {
		return (target, entity) -> {
			Float distance = this.hostileDistanceMap.get(target.getType());

			return distance != null && target.distanceToSqr(entity) <= distance * distance;
		};
	}

	@Nullable
	@Override
	protected LivingEntity findMatches(E entity, NearestVisibleLivingEntities matcher) {
		return matcher.findFirstMatchingEntry(target -> predicate().test(target, entity)).orElse(null);
	}
}
