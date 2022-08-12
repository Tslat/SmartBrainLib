package net.tslat.smartbrainlib.core.sensor.vanilla;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.tslat.smartbrainlib.api.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.EntityFilteringSensor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor}. Not really useful, but included for completeness' sake and legibility. <br/>
 * Handles the Axolotl's hostility and targets
 * @param <E> The entity
 */
public class AxolotlSpecificSensor<E extends LivingEntity> extends EntityFilteringSensor<LivingEntity, E> {
	@Override
	public MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return List.of(getMemory(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
	}

	@Override
	protected BiPredicate<LivingEntity, E> predicate() {
		return (target, entity) -> {
			if (target.distanceToSqr(entity) > 64)
				return false;

			if (!target.isInWaterOrBubble())
				return false;

			if (!target.getType().is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES) && (BrainUtils.hasMemory(target, MemoryModuleType.HAS_HUNTING_COOLDOWN) || !target.getType().is(EntityTypeTags.AXOLOTL_HUNT_TARGETS)))
				return false;

			return Sensor.isEntityAttackable(entity, target);
		};
	}

	@Nullable
	@Override
	protected LivingEntity findMatches(E entity, NearestVisibleLivingEntities matcher) {
		return matcher.findClosest(target -> predicate().test(target, entity)).orElse(null);
	}
}
