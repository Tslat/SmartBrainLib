package net.tslat.smartbrainlib.core.sensor.vanilla;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that looks for nearby players in the surrounding area, sorted by proximity to the brain owner.<br>
 * Defaults:
 * <ul>
 *     <li>Radius is equivalent to the entity's {@link net.minecraft.world.entity.ai.attributes.Attributes#FOLLOW_RANGE} attribute</li>
 *     <li>No spectators</li>
 * </ul>
 * @param <E> The entity
 */
public class NearbyPlayersSensor<E extends LivingEntity> extends PredicateSensor<PlayerEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER});

	@Nullable
	protected Vector3d radius = null;

	public NearbyPlayersSensor() {
		super((player, entity) -> !player.isSpectator());
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public NearbyPlayersSensor<E> setRadius(double radius) {
		return setRadius(new Vector3d(radius, radius, radius));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public NearbyPlayersSensor<E> setRadius(Vector3d radius) {
		this.radius = radius;

		return this;
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_PLAYERS.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		Vector3d radius = this.radius;

		if (radius == null) {
			double dist = entity.getAttributeValue(Attributes.FOLLOW_RANGE);

			radius = new Vector3d(dist, dist, dist);
		}

		List<PlayerEntity> players = EntityRetrievalUtil.getPlayers(level, entity.getBoundingBox().inflate(radius.x(), radius.y(), radius.z()), player -> predicate().test(player, entity));

		players.sort(Comparator.comparingDouble(entity::distanceToSqr));

		List<PlayerEntity> targetablePlayers = new ObjectArrayList<>(players);

		targetablePlayers.removeIf(pl -> !isEntityTargetable(entity, pl));

		List<PlayerEntity> attackablePlayers = new ObjectArrayList<>(targetablePlayers);

		attackablePlayers.removeIf(pl -> !isEntityTargetable(entity, pl));

		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_PLAYERS, players);
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_PLAYER, targetablePlayers.isEmpty() ? null : targetablePlayers.get(0));
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, attackablePlayers.isEmpty() ? null : attackablePlayers.get(0));
	}
}
