package net.tslat.smartbrainlib.core;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SmartBrain<E extends LivingEntity & SmartBrainOwner<E>> extends Brain<E> {
	private final List<MemoryModuleType<?>> expirableMemories = new ObjectArrayList<>();

	public SmartBrain(List<MemoryModuleType<?>> memories, List<? extends ExtendedSensor<E>> sensors, boolean saveMemories) {
		super(memories, ImmutableList.of(), ImmutableList.of(), saveMemories ? () -> Brain.codec(memories, convertSensorsToTypes(sensors)) : SmartBrain::emptyBrainCodec);

		for (ExtendedSensor<E> sensor : sensors) {
			this.sensors.put((SensorType<? extends Sensor<E>>)sensor.type(), sensor);
		}
	}

	@Override
	public void tick(ServerLevel level, E entity) {
		entity.level.getProfiler().push("SmartBrain");

		super.tick(level, entity);

		setActiveActivityToFirstValid(entity.getActivityPriorities());

		entity.level.getProfiler().pop();

		if (entity instanceof Mob mob)
			mob.setAggressive(BrainUtils.hasMemory(mob, MemoryModuleType.ATTACK_TARGET));
	}

	@Override
	public void forgetOutdatedMemories() {
		Iterator<MemoryModuleType<?>> expirable = this.expirableMemories.iterator();

		while (expirable.hasNext()) {
			MemoryModuleType<?> memoryType = expirable.next();
			Optional<? extends ExpirableValue<?>> memory = memories.get(memoryType);

			if (memory.isEmpty()) {
				expirable.remove();
			}
			else {
				ExpirableValue<?> value = memory.get();

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
		return (Optional<U>)this.memories.computeIfAbsent(type, key -> Optional.empty()).map(ExpirableValue::getValue);
	}

	@Override
	public <U> void setMemoryInternal(MemoryModuleType<U> memoryType, Optional<? extends ExpirableValue<?>> memory) {
		if (memory.isPresent() && memory.get().getValue() instanceof Collection<?> collection && collection.isEmpty())
			memory = Optional.empty();

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
