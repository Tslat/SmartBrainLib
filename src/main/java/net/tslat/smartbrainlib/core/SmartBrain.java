package net.tslat.smartbrainlib.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;

public class SmartBrain<E extends LivingEntity & SmartBrainOwner<E>> extends Brain<E> {
	private final List<MemoryModuleType<?>> expirableMemories = new ObjectArrayList<>();

	public SmartBrain(List<MemoryModuleType<?>> memories, List<? extends ExtendedSensor<E>> sensors, boolean saveMemories) {
		super(memories, ImmutableList.of(), ImmutableList.of(), saveMemories ? () -> Brain.codec(memories, convertSensorsToTypes(sensors)) : SmartBrain::emptyBrainCodec);

		for (ExtendedSensor<E> sensor : sensors) {
			this.sensors.put((SensorType<? extends Sensor<E>>)sensor.type(), sensor);
		}
	}

	@Override
	public void tick(ServerWorld level, E entity) {
		entity.level.getProfiler().push("SmartBrain");

		super.tick(level, entity);

		setActiveActivityToFirstValid(entity.getActivityPriorities());

		entity.level.getProfiler().pop();

		if (entity instanceof MobEntity)
			((MobEntity)entity).setAggressive(BrainUtils.hasMemory(entity, MemoryModuleType.ATTACK_TARGET));
	}

	@Override
	public void forgetOutdatedMemories() {
		Iterator<MemoryModuleType<?>> expirable = this.expirableMemories.iterator();

		while (expirable.hasNext()) {
			MemoryModuleType<?> memoryType = expirable.next();
			Optional<? extends Memory<?>> memory = memories.get(memoryType);

			if (!memory.isPresent()) {
				expirable.remove();
			}
			else {
				Memory<?> value = memory.get();

				if (!value.canExpire()) {
					expirable.remove();
				}
				else if (value.hasExpired()) {
					expirable.remove();
					eraseMemory(memoryType);
				}
				else {
					value.tick();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> getMemory(MemoryModuleType<U> type) {
		return (Optional<U>)this.memories.computeIfAbsent(type, key -> Optional.empty()).map(Memory::getValue);
	}
	
	@Override
	public <U> void setMemoryInternal(MemoryModuleType<U> memoryType, Optional<? extends Memory<?>> memory) {
		if (memory.isPresent() && memory.get().getValue() instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) memory.get().getValue();
			if(collection.isEmpty()) {
				memory = Optional.empty();
			}
		}

		this.memories.put(memoryType, memory);

		if (memory.isPresent() && memory.get().canExpire())
			this.expirableMemories.add(memoryType);
	}

	@Override
	public <U> boolean isMemoryValue(MemoryModuleType<U> memoryType, U memory) {
		Optional<U> value = getMemory(memoryType);

		return value.isPresent() && value.get().equals(memory);
	}

	private static <E extends LivingEntity & SmartBrainOwner<E>> Codec<Brain<E>> emptyBrainCodec() {
		MutableObject<Codec<Brain<E>>> brainCodec = new MutableObject<>();

		brainCodec.setValue(Codec.unit(() -> new Brain<>(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), brainCodec::getValue)));

		return brainCodec.getValue();
	}

	private static <E extends LivingEntity & SmartBrainOwner<E>> List<? extends SensorType<? extends Sensor<? super E>>> convertSensorsToTypes(List<? extends ExtendedSensor<E>> sensors) {
		List<SensorType<? extends Sensor<? super E>>> types = new ObjectArrayList<>(sensors.size());

		for (ExtendedSensor<?> sensor : sensors) {
			types.add((SensorType<? extends Sensor<? super E>>)sensor.type());
		}

		return types;
	}
}
