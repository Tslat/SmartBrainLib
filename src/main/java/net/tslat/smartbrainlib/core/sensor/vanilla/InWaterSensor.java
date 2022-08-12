package net.tslat.smartbrainlib.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;

import java.util.List;

/**
 * A sensor that sets or clears the {@link MemoryModuleType#IS_IN_WATER} memory depending on certain criteria. <br/>
 * Defaults:
 * <ul>
 *     <li>{@link LivingEntity#isInWater()}</li>
 * </ul>
 * @param <E> The entity
 */
public class InWaterSensor<E extends LivingEntity> extends PredicateSensor<E, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.IS_IN_WATER);

	public InWaterSensor() {
		super((entity2, entity) -> entity.isInWater());
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		if (predicate().test(entity, entity)) {
			BrainUtils.setMemory(entity, MemoryModuleType.IS_IN_WATER, Unit.INSTANCE);
		}
		else {
			BrainUtils.clearMemory(entity, MemoryModuleType.IS_IN_WATER);
		}
	}
}
