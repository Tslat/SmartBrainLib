package net.tslat.smartbrainlib.core.sensor.vanilla;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.core.sensor.EntityFilteringSensor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * A sensor that sets the {@link MemoryModuleType#NEAREST_VISIBLE_ADULT} memory by checking the existing visible entities for nearby adults of the same entity type. <br/>
 * @see net.minecraft.world.entity.ai.sensing.AdultSensor
 * @param <E> The entity
 */
public class NearbyAdultSensor<E extends AgeableMob> extends EntityFilteringSensor<AgeableMob, E> {
	@Override
	public MemoryModuleType<AgeableMob> getMemory() {
		return MemoryModuleType.NEAREST_VISIBLE_ADULT;
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return List.of(getMemory(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
	}

	@Override
	protected BiPredicate<LivingEntity, E> predicate() {
		return (target, entity) -> target.getType() == entity.getType() && !target.isBaby();
	}

	@Nullable
	@Override
	protected AgeableMob findMatches(E entity, NearestVisibleLivingEntities matcher) {
		return (AgeableMob)matcher.findClosest(target -> predicate().test(target, entity)).orElse(null);
	}
}
