package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.frog.Frog;
import net.tslat.smartbrainlib.api.core.sensor.EntityFilteringSensor;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * A replication of vanilla's
 * {@link net.minecraft.world.entity.ai.sensing.FrogAttackablesSensor}. Not
 * really useful, but included for completeness' sake and legibility. <br>
 * Handles the Frog's tongue target.
 * 
 * @param <E> The entity
 */
public class FrogSpecificSensor<E extends LivingEntity> extends EntityFilteringSensor<LivingEntity, E> {
	@Override
	public MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.FROG_SPECIFIC.get();
	}

	@Override
	protected BiPredicate<LivingEntity, E> predicate() {
		return (target, entity) -> {
			if (BrainUtil.hasMemory(entity, MemoryModuleType.HAS_HUNTING_COOLDOWN))
				return false;

			if (!Sensor.isEntityAttackable((ServerLevel)entity.level(), entity, target))
				return false;

			if (!Frog.canEat(target))
				return false;

			if (!target.closerThan(entity, 10))
				return false;

			List<UUID> unreachableTargets = BrainUtil.getMemory(entity, MemoryModuleType.UNREACHABLE_TONGUE_TARGETS);

			return unreachableTargets == null || !unreachableTargets.contains(target.getUUID());
		};
	}

	@Nullable
	@Override
	protected LivingEntity findMatches(E entity, NearestVisibleLivingEntities matcher) {
		return matcher.findClosest(target -> predicate().test(target, entity)).orElse(null);
	}
}
