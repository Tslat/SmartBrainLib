package net.tslat.smartbrainlib.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;

import java.util.Comparator;
import java.util.List;

/**
 * A sensor that looks for nearby living entities in the surrounding area, sorted by proximity to the brain owner.<br/>
 * Defaults:
 * <ul>
 *     <li>16x16x16 radius</li>
 *     <li>Only alive entities</li>
 * </ul>
 * @param <E> The entity
 */
public class NearbyLivingEntitySensor<E extends LivingEntity> extends PredicateSensor<LivingEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

	protected Vec3 radius = new Vec3(16, 16, 16);

	public NearbyLivingEntitySensor() {
		super((target, entity) -> target != entity && target.isAlive());
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public NearbyLivingEntitySensor<E> setRadius(double radius) {
		return setRadius(new Vec3(radius, radius, radius));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public NearbyLivingEntitySensor<E> setRadius(Vec3 radius) {
		this.radius = radius;

		return this;
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		List<LivingEntity> entities = EntityRetrievalUtil.getEntities(level, entity.getBoundingBox().inflate(this.radius.x(), this.radius.y(), this.radius.z()), obj -> obj instanceof LivingEntity livingEntity && predicate().test(livingEntity, entity));

		entities.sort(Comparator.comparingDouble(entity::distanceToSqr));

		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities);
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities(entity, entities));
	}
}
