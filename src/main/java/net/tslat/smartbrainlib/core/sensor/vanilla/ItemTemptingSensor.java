package net.tslat.smartbrainlib.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;

import java.util.List;

/**
 * Find the nearest player that is holding out a tempting item for the entity.
 * Defaults:
 * <ul>
 *     <li>10x10x10 Radius</li>
 *     <li>No spectators</li>
 *     <li></li>
 * </ul>
 * @see net.minecraft.world.entity.ai.sensing.TemptingSensor
 * @param <E> The entity
 */
public class ItemTemptingSensor<E extends LivingEntity> extends PredicateSensor<Player, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.TEMPTING_PLAYER);

	protected Ingredient temptingItems = Ingredient.EMPTY;
	protected Vec3 radius = new Vec3(10, 10, 10);

	public ItemTemptingSensor() {
		setPredicate((target, entity) -> {
			if (target.isSpectator() || !target.isAlive())
				return false;

			return this.temptingItems.test(target.getMainHandItem()) || this.temptingItems.test(target.getOffhandItem());
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
	 * @param temptingItems An ingredient representing the temptations for the entity
	 * @return this
	 */
	public ItemTemptingSensor<E> setTemptingItems(Ingredient temptingItems) {
		this.temptingItems = temptingItems;

		return this;
	}

	/**
	 * Set the radius for the player sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public ItemTemptingSensor<E> setRadius(double radius) {
		return setRadius(new Vec3(radius, radius, radius));
	}

	/**
	 * Set the radius for the player sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public ItemTemptingSensor<E> setRadius(Vec3 radius) {
		this.radius = radius;

		return this;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.TEMPTING_PLAYER, EntityRetrievalUtil.getNearestPlayer(entity, this.radius.x(), this.radius.y(), this.radius.z(), target -> predicate().test(target, entity)));
	}
}
