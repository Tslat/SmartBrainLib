package net.tslat.smartbrainlib.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * A sensor that looks for nearby living entities in the surrounding area, sorted by proximity to the brain owner.<br>
 * Defaults:
 * <ul>
 *     <li>Radius is equivalent to the entity's {@link net.minecraft.world.entity.ai.attributes.Attributes#FOLLOW_RANGE} attribute</li>
 *     <li>Only alive entities</li>
 * </ul>
 * @param <E> The entity
 */
public class NearbyLivingEntitySensor<E extends LivingEntity> extends PredicateSensor<LivingEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

	@Nullable
	protected Vec3 radius = null;

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
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_LIVING_ENTITY.get();
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		Vec3 radius = this.radius;

		if (radius == null) {
			double dist = entity.getAttributeValue(Attributes.FOLLOW_RANGE);

			radius = new Vec3(dist, dist, dist);
		}

		List<LivingEntity> entities = EntityRetrievalUtil.getEntities(level, entity.getBoundingBox().inflate(radius.x(), radius.y(), radius.z()), obj -> obj instanceof LivingEntity livingEntity && predicate().test(livingEntity, entity));

		entities.sort(Comparator.comparingDouble(entity::distanceToSqr));

		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities);
		BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities(entity, entities));
	}
}
