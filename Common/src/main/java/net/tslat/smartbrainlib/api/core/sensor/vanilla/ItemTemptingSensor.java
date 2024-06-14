package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtils;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

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
public class ItemTemptingSensor<E extends LivingEntity> extends PredicateSensor<Player, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.TEMPTING_PLAYER);

	protected BiPredicate<E, ItemStack> temptPredicate = (entity, stack) -> false;
	protected SquareRadius radius = new SquareRadius(10, 10);

	public ItemTemptingSensor() {
		setPredicate((target, entity) -> {
			if (target.isSpectator() || !target.isAlive())
				return false;

			return this.temptPredicate.test(entity, target.getMainHandItem()) || this.temptPredicate.test(entity, target.getOffhandItem());
		});
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.ITEM_TEMPTING.get();
	}

	/**
	 * Set the items to temptable items for the entity.
	 *
	 * @param temptingItems An ingredient representing the temptations for the
	 *                      entity
	 * @deprecated Use {@link ItemTemptingSensor#temptedWith}
	 * @return this
	 */
	@Deprecated(forRemoval = true)
	public ItemTemptingSensor<E> setTemptingItems(Ingredient temptingItems) {
		return temptedWith((entity, stack) -> temptingItems.test(stack));
	}

	/**
	 * Set the items to temptable items for the entity.
	 *
	 * @param predicate An ingredient representing the temptations for the
	 *                      entity
	 * @return this
	 */
	public ItemTemptingSensor<E> temptedWith(final BiPredicate<E, ItemStack> predicate) {
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
		Player player;
		final List<Player> nearbyPlayers = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_PLAYERS);

		if (nearbyPlayers != null) {
			player = nearbyPlayers.stream().filter(pl -> predicate().test(pl, entity)).min(Comparator.comparing(pl -> pl.distanceToSqr(entity))).orElse(null);
		}
		else {
			player = EntityRetrievalUtil.getNearestPlayer(entity, this.radius.xzRadius(), this.radius.yRadius(), this.radius.xzRadius(), target -> predicate().test(target, entity));
		}

		if (player == null) {
			BrainUtils.clearMemory(entity, MemoryModuleType.TEMPTING_PLAYER);
		}
		else {
			BrainUtils.setMemory(entity, MemoryModuleType.TEMPTING_PLAYER, player);
		}
	}
}
