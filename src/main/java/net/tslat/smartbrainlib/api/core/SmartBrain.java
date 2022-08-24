package net.tslat.smartbrainlib.api.core;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Supercedes vanilla's {@link Brain}. One of the core components of the SBL library. <br>
 * Any entity that returns a {@link SmartBrainProvider} from {@link LivingEntity#brainProvider()} will have one of these.
 * @param <E> The entity
 */
public class SmartBrain<E extends LivingEntity & SmartBrainOwner<E>> extends Brain<E> {
	private final List<MemoryModuleType<?>> expirableMemories = new ObjectArrayList<>();
	private final List<ActivityBehaviours<E>> behaviours = new ObjectArrayList<>();
	private final List<Pair<SensorType<ExtendedSensor<? super E>>, ExtendedSensor<? super E>>> sensors = new ObjectArrayList<>();

	private boolean sortBehaviours = false;

	public SmartBrain(List<MemoryModuleType<?>> memories, List<? extends ExtendedSensor<E>> sensors, @Nullable List<BrainActivityGroup<E>> taskList, boolean saveMemories) {
		super(memories, ImmutableList.of(), ImmutableList.of(), saveMemories ? () -> Brain.codec(memories, convertSensorsToTypes(sensors)) : SmartBrain::emptyBrainCodec);

		for (ExtendedSensor<E> sensor : sensors) {
			this.sensors.add(Pair.of((SensorType)sensor.type(), sensor));
		}

		if (taskList != null) {
			for (BrainActivityGroup<E> group : taskList) {
				int priority = group.getPriorityStart();

				for (Behavior<? super E> behaviour : group.getBehaviours()) {
					addBehaviour(priority++, group.getActivity(), behaviour);
				}
			}
		}
	}

	@Override
	public void tick(ServerLevel level, E entity) {
		entity.level.getProfiler().push("SmartBrain");

		if (this.sortBehaviours)
			this.behaviours.sort(Comparator.comparingInt(ActivityBehaviours::priority));

		forgetOutdatedMemories();
		tickSensors(level, entity);
		checkForNewBehaviours(level, entity);
		tickRunningBehaviours(level, entity);

		setActiveActivityToFirstValid(entity.getActivityPriorities());

		entity.level.getProfiler().pop();

		if (entity instanceof Mob mob)
			mob.setAggressive(BrainUtils.hasMemory(mob, MemoryModuleType.ATTACK_TARGET));
	}

	private void tickSensors(ServerLevel level, E entity) {
		for (Pair<SensorType<ExtendedSensor<? super E>>, ExtendedSensor<? super E>> sensor : this.sensors) {
			sensor.getSecond().tick(level, entity);
		}
	}

	private void checkForNewBehaviours(ServerLevel level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Behavior<? super E>>> pair : behaviourGroup.behaviours) {
				if (this.activeActivities.contains(pair.getFirst())) {
					for (Behavior<? super E> behaviour : pair.getSecond()) {
						if (behaviour.getStatus() == Behavior.Status.STOPPED)
							behaviour.tryStart(level, entity, gameTime);
					}
				}
			}
		}
	}

	private void tickRunningBehaviours(ServerLevel level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Behavior<? super E>>> pair : behaviourGroup.behaviours) {
				for (Behavior<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Behavior.Status.RUNNING)
						behaviour.tickOrStop(level, entity, gameTime);
				}
			}
		}
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

	@Override
	public void stopAll(ServerLevel level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Behavior<? super E>>> pair : behaviourGroup.behaviours) {
				for (Behavior<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Behavior.Status.RUNNING)
						behaviour.doStop(level, entity, gameTime);
				}
			}
		}
	}

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

	@Override
	public Brain<E> copyWithoutBehaviors() {
		SmartBrain<E> brain = new SmartBrain<>(this.memories.keySet().stream().toList(), this.sensors.stream().map(pair -> (ExtendedSensor<E>)pair.getSecond()).toList(), null, false);

		for(Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
			MemoryModuleType<?> memoryType = entry.getKey();

			if (entry.getValue().isPresent())
				brain.memories.put(memoryType, entry.getValue());
		}

		return brain;
	}

	@Override
	public List<Behavior<? super E>> getRunningBehaviors() {
		List<Behavior<? super E>> runningBehaviours = new ObjectArrayList<>();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Behavior<? super E>>> pair : behaviourGroup.behaviours) {
				for (Behavior<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Behavior.Status.RUNNING)
						runningBehaviours.add(behaviour);
				}
			}
		}

		return runningBehaviours;
	}

	@Override
	public void removeAllBehaviors() {
		this.behaviours.clear();
	}

	@Override
	public void addActivityAndRemoveMemoriesWhenStopped(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> tasks, Set<Pair<MemoryModuleType<?>, MemoryStatus>> memorieStatuses, Set<MemoryModuleType<?>> memoryTypes) {
		this.activityRequirements.put(activity, memorieStatuses);

		if (!memoryTypes.isEmpty())
			this.activityMemoriesToEraseWhenStopped.put(activity, memoryTypes);

		for(Pair<Integer, ? extends Behavior<? super E>> pair : tasks) {
			addBehaviour(pair.getFirst(), activity, pair.getSecond());
		}
	}

	/**
	 * Add a behaviour to the behaviours list of this brain.
	 * @param priority The behaviour's priority value
	 * @param activity The behaviour's activity category
	 * @param behaviour The behaviour instance
	 */
	public void addBehaviour(int priority, Activity activity, Behavior<? super E> behaviour) {
		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			if (behaviourGroup.priority == priority) {
				for (Pair<Activity, List<Behavior<? super E>>> pair : behaviourGroup.behaviours) {
					if (pair.getFirst() == activity) {
						pair.getSecond().add(behaviour);

						return;
					}
				}

				behaviourGroup.behaviours.add(Pair.of(activity, ObjectArrayList.of(behaviour)));

				return;
			}
		}

		this.behaviours.add(new ActivityBehaviours<>(priority, ObjectArrayList.of(Pair.of(activity, ObjectArrayList.<Behavior<? super E>>of(behaviour)))));
		this.sortBehaviours = true;
	}

	/**
	 * Remove a cached behaviour from the behaviours list of this brain. <br>
	 * Matching uses <b>reference parity</b>
	 * @param priority The behaviour's priority value
	 * @param activity The behaviour's activity category
	 * @param behaviour The behaviour instance
	 */
	public void removeBehaviour(int priority, Activity activity, Behavior<? super E> behaviour) {
		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			if (behaviourGroup.priority == priority) {
				for (Pair<Activity, List<Behavior<? super E>>> pair : behaviourGroup.behaviours) {
					if (pair.getFirst() == activity) {
						for (Iterator<Behavior<? super E>> iterator = pair.getSecond().iterator(); iterator.hasNext();) {
							if (iterator.next() == behaviour) {
								iterator.remove();

								return;
							}
						}

						return;
					}
				}

				return;
			}
		}
	}

	private record ActivityBehaviours<E extends LivingEntity & SmartBrainOwner<E>>(int priority, List<Pair<Activity, List<Behavior<? super E>>>> behaviours) {}
}
