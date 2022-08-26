package net.tslat.smartbrainlib.core.sensor.vanilla;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that looks for the nearest item entity in the surrounding area.<br>
 * Defaults:
 * <ul>
 *     <li>32x16x32 radius</li>
 *     <li>Only items that return true for {@link Mob#wantsToPickUp(ItemStack)}</li>
 *     <li>Only items that return true for {@link net.minecraft.world.entity.LivingEntity#hasLineOfSight(Entity)}</li>
 * </ul>
 * @param <E> The entity
 */
public class NearestItemSensor<E extends MobEntity> extends PredicateSensor<ItemEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM});

	protected Vector3d radius = new Vector3d(32, 16, 32);

	public NearestItemSensor() {
		super((item, entity) -> entity.wantsToPickUp(item.getItem()) && entity.canSee(item));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public NearestItemSensor<E> setRadius(double radius) {
		return setRadius(new Vector3d(radius, radius, radius));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public NearestItemSensor<E> setRadius(Vector3d radius) {
		this.radius = radius;

		return this;
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEAREST_ITEM.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, EntityRetrievalUtil.getNearestEntity(level, entity.getBoundingBox().inflate(this.radius.x(), this.radius.y(), this.radius.z()), entity.position(), obj -> obj instanceof ItemEntity && predicate().test((ItemEntity)obj, entity)));
	}
}
