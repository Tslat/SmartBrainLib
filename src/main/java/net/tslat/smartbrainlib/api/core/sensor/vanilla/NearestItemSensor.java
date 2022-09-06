package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;

import java.util.List;

/**
 * A sensor that looks for the nearest item entity in the surrounding area.<br>
 * Defaults:
 * <ul>
 * <li>32x16x32 radius</li>
 * <li>Only items that return true for {@link Mob#wantsToPickUp(ItemStack)}</li>
 * <li>Only items that return true for
 * {@link net.minecraft.world.entity.LivingEntity#hasLineOfSight(Entity)}</li>
 * </ul>
 * 
 * @param <E> The entity
 */
public class NearestItemSensor<E extends Mob> extends PredicateSensor<ItemEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);

	protected SquareRadius radius = new SquareRadius(32, 16);

	public NearestItemSensor() {
		super((item, entity) -> entity.wantsToPickUp(item.getItem()) && entity.hasLineOfSight(item));
	}

	/**
	 * Set the radius for the item sensor to scan.
	 * 
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public NearestItemSensor<E> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius for the item sensor to scan.
	 * 
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y  The Y coordinate radius, in blocks
	 * @return this
	 */
	public NearestItemSensor<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEAREST_ITEM;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, EntityRetrievalUtil.getNearestEntity(level, this.radius.inflateAABB(entity.getBoundingBox()), entity.position(), obj -> obj instanceof ItemEntity item && predicate().test(item, entity)));
	}
}
