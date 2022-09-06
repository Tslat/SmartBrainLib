package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.registry.SBLSensors;

import java.util.List;

/**
 * A sensor that sets or clears the {@link MemoryModuleType#IS_IN_WATER} memory
 * depending on certain criteria. <br>
 * Defaults:
 * <ul>
 * <li>{@link LivingEntity#isInWater()}</li>
 * </ul>
 * 
 * @param <E> The entity
 */
public class InWaterSensor<E extends LivingEntity> extends PredicateSensor<E, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.IS_IN_WATER);

	public InWaterSensor() {
		super((entity2, entity) -> entity.isInWater());
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.IN_WATER;
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
