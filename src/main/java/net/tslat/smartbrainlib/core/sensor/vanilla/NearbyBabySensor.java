package net.tslat.smartbrainlib.core.sensor.vanilla;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.core.sensor.EntityFilteringSensor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * A sensor that sets the {@link MemoryModuleType#VISIBLE_VILLAGER_BABIES} memory by checking the existing visible entities for nearby babies of the same entity type. <br/>
 * @see net.minecraft.world.entity.ai.sensing.VillagerBabiesSensor
 * @param <E> The entity
 */
public class NearbyBabySensor<E extends LivingEntity> extends EntityFilteringSensor<List<LivingEntity>, E> {
	@Override
	public MemoryModuleType<List<LivingEntity>> getMemory() {
		return MemoryModuleType.VISIBLE_VILLAGER_BABIES;
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return List.of(getMemory(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
	}

	@Override
	protected BiPredicate<LivingEntity, E> predicate() {
		return (target, entity) -> target.getType() == entity.getType() && target.isBaby();
	}

	@Nullable
	@Override
	protected List<LivingEntity> findMatches(E entity, NearestVisibleLivingEntities matcher) {
		return ImmutableList.copyOf(matcher.findAll(target -> predicate().test(target, entity)));
	}
}
