package net.tslat.smartbrainlib.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;

import java.util.List;

/**
 * A sensor that looks for the nearest item entity in the surrounding area.<br/>
 * Defaults:
 * <ul>
 *     <li>32x16x32 radius</li>
 *     <li>Only items that return true for {@link Mob#wantsToPickUp(ItemStack)}</li>
 *     <li>Only items that return true for {@link net.minecraft.world.entity.LivingEntity#hasLineOfSight(Entity)}</li>
 * </ul>
 * @param <E> The entity
 */
public class NearestItemSensor<E extends Mob> extends PredicateSensor<ItemEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);

	protected Vec3 radius = new Vec3(32, 16, 32);

	public NearestItemSensor() {
		super((item, entity) -> entity.wantsToPickUp(item.getItem()) && entity.hasLineOfSight(item));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public NearestItemSensor<E> setRadius(double radius) {
		return setRadius(new Vec3(radius, radius, radius));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public NearestItemSensor<E> setRadius(Vec3 radius) {
		this.radius = radius;

		return this;
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, EntityRetrievalUtil.getNearestEntity(level, entity.getBoundingBox().inflate(this.radius.x(), this.radius.y(), this.radius.z()), entity.position(), obj -> obj instanceof ItemEntity item && predicate().test(item, entity)));
	}
}
