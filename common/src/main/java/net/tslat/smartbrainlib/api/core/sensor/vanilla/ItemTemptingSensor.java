package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.object.TriPredicate;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Find the nearest player that is holding out a tempting item for the entity.
 * Defaults:
 * <ul>
 * <li>10x10x10 Radius</li>
 * <li>No spectators</li>
 * </ul>
 * 
 * @see net.minecraft.world.entity.ai.sensing.TemptingSensor
 * @param <E> The entity
 */
public class ItemTemptingSensor<E extends LivingEntity> extends ExtendedSensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.TEMPTING_PLAYER);

	protected TriPredicate<E, ItemStack, Player> temptPredicate = (entity, stack, player) -> false;
	protected SquareRadius radius = new SquareRadius(10, 10);

	public ItemTemptingSensor() {}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.ITEM_TEMPTING.get();
	}

	/**
	 * Set the items to temptable items for the entity.<br>
	 * Automatically handles boilerplate player checks as part of the predicate
	 *
	 * @param predicate The predicate to test for valid items for tempting
	 * @return this
	 * @see #temptPredicate(TriPredicate)
	 */
	public ItemTemptingSensor<E> temptedWith(final TriPredicate<E, ItemStack, Player> predicate) {
		return temptPredicate((entity, stack, player) -> {
			if (player.isSpectator() || !player.isAlive())
				return false;

			return predicate.test(entity, stack, player);
		});
	}

	/**
	 * Set the predicate to determine whether the entity should be tempted
	 *
	 * @param predicate The predicate to test for successful temptation
	 * @return this
	 */
	public ItemTemptingSensor<E> temptPredicate(final TriPredicate<E, ItemStack, Player> predicate) {
		this.temptPredicate = predicate;

		return this;
	}

	/**
	 * Set the radius for the player sensor to scan
	 * 
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public ItemTemptingSensor<E> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius for the player sensor to scan.
	 * 
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y  The Y coordinate radius, in blocks
	 * @return this
	 */
	public ItemTemptingSensor<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		Optional<Player> player;
		final List<Player> nearbyPlayers = BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_PLAYERS);
		final Predicate<Player> predicate = pl -> this.temptPredicate.test(entity, pl.getMainHandItem(), pl) || this.temptPredicate.test(entity, pl.getOffhandItem(), pl);

		if (nearbyPlayers != null) {
			Player nearestPlayer = null;
			double nearestDistance = Double.MAX_VALUE;

			for (Player pl : nearbyPlayers) {
				if (predicate.test(pl)) {
					double dist = pl.distanceToSqr(entity);

					if (dist < nearestDistance) {
						nearestDistance = dist;
						nearestPlayer = pl;
					}
				}
			}

			player = Optional.ofNullable(nearestPlayer);
		}
		else {
			player = EntityRetrievalUtil.getNearestPlayer(entity, this.radius.xzRadius(), this.radius.yRadius(), this.radius.xzRadius(), predicate);
		}

		player.ifPresentOrElse(pl ->
				BrainUtil.setMemory(entity, MemoryModuleType.TEMPTING_PLAYER, pl),
				() -> BrainUtil.clearMemory(entity, MemoryModuleType.TEMPTING_PLAYER));
	}
}
