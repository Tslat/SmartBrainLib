package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.object.backport.Collections;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * A sensor that sets or clears the {@link SBLMemoryTypes#IS_IN_WATER} memory
 * depending on certain criteria. <br>
 * Defaults:
 * <ul>
 * <li>{@link LivingEntity#isInWater()}</li>
 * </ul>
 * 
 * @param <E> The entity
 */
public class InWaterSensor<E extends LivingEntity> extends PredicateSensor<E, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = Collections.list(SBLMemoryTypes.IS_IN_WATER.get());

	public InWaterSensor() {
		super((entity2, entity) -> entity.isInWater());
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.IN_WATER.get();
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		if (predicate().test(entity, entity)) {
			BrainUtils.setMemory(entity, SBLMemoryTypes.IS_IN_WATER.get(), Unit.INSTANCE);
		}
		else {
			BrainUtils.clearMemory(entity, SBLMemoryTypes.IS_IN_WATER.get());
		}
	}
}
