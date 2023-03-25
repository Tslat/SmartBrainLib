package net.tslat.smartbrainlib.api.core.sensor;

import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.object.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * An abstract class that is used to pick out certain entities from the existing {@link net.minecraft.world.entity.ai.memory.MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES} memory. <br>
 * This requires that another sensor has pre-filled that memory.
 * @see net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor
 * @param <P> The target entity
 * @param <E> The entity
 */
public abstract class EntityFilteringSensor<P, E extends LivingEntity> extends PredicateSensor<LivingEntity, E> {
	/**
	 * Which memory the sensor should set if an entity meets the given criteria.
	 *
	 * @return The memory type to use
	 */
	protected abstract MemoryModuleType<P> getMemory();

	@Override
	protected abstract BiPredicate<LivingEntity, E> predicate();

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return Lists.newArrayList(getMemory());
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		BrainUtils.setMemory(entity, getMemory(), testForEntity(entity));
	}

	protected P testForEntity(E entity) {
		NearestVisibleLivingEntities matcher = BrainUtils.getMemory(entity, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get());
		
		if (matcher == null)
			return null;

		return findMatches(entity, matcher);
	}

	/**
	 * Find and return matches based on the provided list of entities. <br>
	 * The returned value is saved as the memory for this sensor.
	 * @param entity The entity
	 * @param matcher The nearby entities list retrieved from the {@link MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES} memory
	 * @return The match(es) to save in memory
	 */
	@Nullable
	protected abstract P findMatches(E entity, NearestVisibleLivingEntities matcher);
}
