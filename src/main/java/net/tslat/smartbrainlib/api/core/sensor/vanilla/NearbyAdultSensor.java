package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.EntityFilteringSensor;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.registry.SBLSensors;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

/**
 * A sensor that sets the {@link MemoryModuleType#NEAREST_VISIBLE_ADULT} memory by checking the existing visible entities for nearby adults of the same entity type. <br>
 * @see net.minecraft.world.entity.ai.sensing.AdultSensor
 * @param <E> The entity
 */
public class NearbyAdultSensor<E extends AgeableEntity> extends EntityFilteringSensor<AgeableEntity, E> {
	@Override
	public MemoryModuleType<AgeableEntity> getMemory() {
		return MemoryModuleType.NEAREST_VISIBLE_ADULT;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_ADULT.get();
	}

	@Override
	protected BiPredicate<LivingEntity, E> predicate() {
		return (target, entity) -> target.getType() == entity.getType() && !target.isBaby();
	}

	@Nullable
	@Override
	protected AgeableEntity findMatches(E entity, NearestVisibleLivingEntities matcher) {
		return (AgeableEntity)matcher.findFirstMatchingEntry(target -> predicate().test(target, entity)).orElse(null);
	}
}
