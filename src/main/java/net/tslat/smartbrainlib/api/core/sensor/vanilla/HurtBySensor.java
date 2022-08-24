package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that sets the memory state for the last damage source and attacker.
 *
 * @param <E> The entity
 */
public class HurtBySensor<E extends MobEntity> extends PredicateSensor<DamageSource, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY});

	public HurtBySensor() {
		super((damageSource, entity) -> true);
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.HURT_BY.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		Brain<?> brain = entity.getBrain();
		DamageSource damageSource = entity.getLastDamageSource();

		if (damageSource == null) {
			BrainUtils.clearMemory(brain, MemoryModuleType.HURT_BY);
		}
		else if (predicate().test(damageSource, entity)) {
			BrainUtils.setMemory(brain, MemoryModuleType.HURT_BY, damageSource);

			if (damageSource.getEntity() instanceof LivingEntity && ((LivingEntity)damageSource.getEntity()).isAlive() && ((LivingEntity)damageSource.getEntity()).level == entity.level)
				BrainUtils.setMemory(brain, MemoryModuleType.HURT_BY_ENTITY, ((LivingEntity)damageSource.getEntity()));
		}
		else {
			BrainUtils.withMemory(brain, MemoryModuleType.HURT_BY_ENTITY, attacker -> {
				if (!attacker.isAlive() || attacker.level != entity.level)
					BrainUtils.clearMemory(brain, MemoryModuleType.HURT_BY_ENTITY);
			});
		}
	}
}
