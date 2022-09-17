package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * Find the nearest player that is holding out a tempting item for the entity.
 * Defaults:
 * <ul>
 *     <li>10x10x10 Radius</li>
 *     <li>No spectators</li>
 * </ul>
 * @see net.minecraft.world.entity.ai.sensing.TemptingSensor
 * @param <E> The entity
 */
public class ItemTemptingSensor<E extends LivingEntity> extends PredicateSensor<PlayerEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {SBLMemoryTypes.TEMPTING_PLAYER.get()});

	protected Ingredient temptingItems = Ingredient.EMPTY;
	protected SquareRadius radius = new SquareRadius(10, 10);

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
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public ItemTemptingSensor<E> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius for the player sensor to scan.
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y The Y coordinate radius, in blocks
	 * @return this
	 */
	public ItemTemptingSensor<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		BrainUtils.setMemory(entity, SBLMemoryTypes.TEMPTING_PLAYER.get(), EntityRetrievalUtil.getNearestPlayer(entity, this.radius.xzRadius(), this.radius.yRadius(), this.radius.xzRadius(), target -> predicate().test(target, entity)));
	}
}
