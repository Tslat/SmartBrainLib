package net.tslat.smartbrainlib.core.sensor.vanilla;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.PiglinBruteSpecificSensor}. Not really useful, but included for completeness' sake and legibility. <br>
 * Keeps track of nearby {@link Piglin piglins} and {@link MemoryModuleType#NEAREST_VISIBLE_NEMESIS nemesis}
 * @param <E> The entity
 */
public class PiglinBruteSpecificSensor<E extends LivingEntity> extends ExtendedSensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.NEARBY_ADULT_PIGLINS});

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.PIGLIN_BRUTE_SPECIFIC.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		Brain<?> brain = entity.getBrain();
		List<AbstractPiglinEntity> nearbyPiglins = new ObjectArrayList<>();

		BrainUtils.withMemory(brain, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get(), entities -> BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, (MobEntity)entities.findClosest(target -> target instanceof WitherSkeletonEntity || target instanceof WitherEntity).orElse(null)));
		BrainUtils.withMemory(brain, MemoryModuleType.LIVING_ENTITIES, entities -> {
			for (LivingEntity target : entities) {
				if (target instanceof AbstractPiglinEntity && ((AbstractPiglinEntity)target).isAdult())
					nearbyPiglins.add((AbstractPiglinEntity) target);
			}
		});

		BrainUtils.setMemory(brain, MemoryModuleType.NEARBY_ADULT_PIGLINS, nearbyPiglins);
	}
}
