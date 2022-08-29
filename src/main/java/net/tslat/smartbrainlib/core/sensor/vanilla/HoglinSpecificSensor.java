package net.tslat.smartbrainlib.core.sensor.vanilla;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.HoglinSpecificSensor}. Not really useful, but included for completeness' sake and legibility. <br>
 * Handles most of Hoglin's memories at once
 * @param <E> The entity
 */
public class HoglinSpecificSensor<E extends LivingEntity> extends ExtendedSensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT});

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.HOGLIN_SPECIFIC.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		Brain<?> brain = entity.getBrain();

		BrainUtils.withMemory(brain, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get(), entities -> {
			int piglinCount = 0;
			PiglinEntity nearestPiglin = null;
			List<HoglinEntity> hoglins = new ObjectArrayList<>();

			for (LivingEntity target : entities.findAllMatchingEntries(mob -> !mob.isBaby())) {
				if (target instanceof PiglinEntity) {
					piglinCount++;

					if (nearestPiglin == null)
						nearestPiglin = (PiglinEntity)target;
				}
				else if (target instanceof HoglinEntity) {
					hoglins.add((HoglinEntity)target);
				}
			}

			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, nearestPiglin);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, hoglins);
			BrainUtils.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, piglinCount);
			BrainUtils.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, hoglins.size());
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_REPELLENT, BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, pos -> level.getBlockState(pos).is(BlockTags.HOGLIN_REPELLENTS)).orElse(null));
		});
	}
}
