package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that looks for nearby players in the surrounding area, sorted by
 * proximity to the brain owner.<br>
 * Defaults:
 * <ul>
 * <li>Radius is equivalent to the entity's
 * {@link net.minecraft.world.entity.ai.attributes.Attributes#FOLLOW_RANGE}
 * attribute</li>
 * <li>No spectators</li>
 * </ul>
 * 
 * @param <E> The entity
 */
public class NearbyPlayersSensor<E extends LivingEntity> extends PredicateSensor<Player, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);

	@Nullable
	protected SquareRadius radius = null;

	public NearbyPlayersSensor() {
		super((player, entity) -> !player.isSpectator());
	}

	/**
	 * Set the radius for the sensor to scan.
	 * 
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public NearbyPlayersSensor<E> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius for the sensor to scan.
	 * 
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y  The Y coordinate radius, in blocks
	 * @return this
	 */
	public NearbyPlayersSensor<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_PLAYERS;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		SquareRadius radius = this.radius;

		if (radius == null) {
			double dist = entity.getAttributeValue(Attributes.FOLLOW_RANGE);

			radius = new SquareRadius(dist, dist);
		}

		List<Player> players = EntityRetrievalUtil.getPlayers(level, radius.inflateAABB(entity.getBoundingBox()), player -> predicate().test(player, entity));

		players.sort(Comparator.comparingDouble(entity::distanceToSqr));

		List<Player> targetablePlayers = new ObjectArrayList<>(players);

		targetablePlayers.removeIf(pl -> !isEntityTargetable(entity, pl));

		List<Player> attackablePlayers = new ObjectArrayList<>(targetablePlayers);

		attackablePlayers.removeIf(pl -> !isEntityAttackable(entity, pl));

		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_PLAYERS, players);
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_PLAYER, targetablePlayers.isEmpty() ? null : targetablePlayers.get(0));
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, attackablePlayers.isEmpty() ? null : attackablePlayers.get(0));
	}
}
