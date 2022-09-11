package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.util.Unit;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that sets or clears the {@link MemoryModuleType#IS_IN_WATER} memory depending on certain criteria. <br>
 * Defaults:
 * <ul>
 *     <li>{@link LivingEntity#isInWater()}</li>
 * </ul>
 * @param <E> The entity
 */
public class InWaterSensor<E extends LivingEntity> extends PredicateSensor<E, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {SBLMemoryTypes.IS_IN_WATER.get()});

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
	protected void doTick(ServerWorld level, E entity) {
		if (predicate().test(entity, entity)) {
			BrainUtils.setMemory(entity, SBLMemoryTypes.IS_IN_WATER.get(), Unit.INSTANCE);
		}
		else {
			BrainUtils.clearMemory(entity, SBLMemoryTypes.IS_IN_WATER.get());
		}
	}
}
