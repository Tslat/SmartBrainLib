package net.tslat.smartbrainlib.api.core.sensor.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.EntityFilteringSensor;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.backport.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

/**
 * Sets the {@link SBLMemoryTypes#NEAREST_ATTACKABLE} memory based on visible nearby entities. <br>
 * Defaults:
 * <ul>
 *     <li>Only targets that {@link net.minecraft.world.entity.ai.sensing.Sensor#isEntityAttackable(LivingEntity, LivingEntity)} passes.</li>
 * </ul>
 * @param <E>
 */
public class GenericAttackTargetSensor<E extends LivingEntity> extends EntityFilteringSensor<LivingEntity, E> {
	@Override
	protected MemoryModuleType<LivingEntity> getMemory() {
		return SBLMemoryTypes.NEAREST_ATTACKABLE.get();
	}

	@Override
	protected BiPredicate<LivingEntity, E> predicate() {
		return (target, entity) -> isEntityAttackable(entity, target);
	}

	@Nullable
	@Override
	protected LivingEntity findMatches(E entity, NearestVisibleLivingEntities matcher) {
		return matcher.findClosest(target -> predicate().test(target, entity)).orElse(null);
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.GENERIC_ATTACK_TARGET.get();
	}
}
