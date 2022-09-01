package net.tslat.smartbrainlib.api.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;

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
				addActivity(group);
			}
		}
	}

	@Override
	public void tick(ServerWorld level, E entity) {
		entity.level.getProfiler().push("SmartBrain");

		if (this.sortBehaviours)
			this.behaviours.sort(Comparator.comparingInt(ActivityBehaviours::priority));

		forgetOutdatedMemories();
		tickSensors(level, entity);
		checkForNewBehaviours(level, entity);
		tickRunningBehaviours(level, entity);

		setActiveActivityToFirstValid(entity.getActivityPriorities());

		entity.level.getProfiler().pop();

		if (entity instanceof MobEntity)
			((MobEntity)entity).setAggressive(BrainUtils.hasMemory(entity, MemoryModuleType.ATTACK_TARGET));
	}

	private void tickSensors(ServerWorld level, E entity) {
		for (Pair<SensorType<ExtendedSensor<? super E>>, ExtendedSensor<? super E>> sensor : this.sensors) {
			sensor.getSecond().tick(level, entity);
		}
	}

	private void checkForNewBehaviours(ServerWorld level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Task<? super E>>> pair : behaviourGroup.behaviours) {
				if (this.activeActivities.contains(pair.getFirst())) {
					for (Task<? super E> behaviour : pair.getSecond()) {
						if (behaviour.getStatus() == Task.Status.STOPPED)
							behaviour.tryStart(level, entity, gameTime);
					}
				}
			}
		}
	}

	private void tickRunningBehaviours(ServerWorld level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Task<? super E>>> pair : behaviourGroup.behaviours) {
				for (Task<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Task.Status.RUNNING)
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

	@Override
	public void stopAll(ServerWorld level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Task<? super E>>> pair : behaviourGroup.behaviours) {
				for (Task<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Task.Status.RUNNING)
						behaviour.doStop(level, entity, gameTime);
				}
			}
		}
	}

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
			types.add((SensorType<? extends Sensor<? super E>>)(Object)sensor.type());
		}

		return types;
	}

	@Override
	public Brain<E> copyWithoutBehaviors() {
		SmartBrain<E> brain = new SmartBrain<>(this.memories.keySet().stream().collect(Collectors.toList()), this.sensors.stream().map(pair -> (ExtendedSensor<E>)pair.getSecond()).collect(Collectors.toList()), null, false);

		for(Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>> entry : this.memories.entrySet()) {
			MemoryModuleType<?> memoryType = entry.getKey();

			if (entry.getValue().isPresent())
				brain.memories.put(memoryType, entry.getValue());
		}

		return brain;
	}

	@Override
	public List<Task<? super E>> getRunningBehaviors() {
		List<Task<? super E>> runningBehaviours = new ObjectArrayList<>();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<Task<? super E>>> pair : behaviourGroup.behaviours) {
				for (Task<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Task.Status.RUNNING)
						runningBehaviours.add(behaviour);
				}
			}
		}

		return runningBehaviours;
	}

	//@Override
	public void removeAllBehaviors() {
		this.behaviours.clear();
	}
	
	@Override
	public void addActivityAndRemoveMemoriesWhenStopped(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> tasks, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> memorieStatuses, Set<MemoryModuleType<?>> memoryTypes) {
		this.activityRequirements.put(activity, memorieStatuses);

		if (!memoryTypes.isEmpty())
			this.activityMemoriesToEraseWhenStopped.put(activity, memoryTypes);

		for(Pair<Integer, ? extends Task<? super E>> pair : tasks) {
			addBehaviour(pair.getFirst(), activity, pair.getSecond());
		}
	}

	public void addActivity(BrainActivityGroup<E> activityGroup) {
		addActivityAndRemoveMemoriesWhenStopped(activityGroup.getActivity(), activityGroup.pairBehaviourPriorities(), activityGroup.getActivityStartMemoryConditions(), activityGroup.getWipedMemoriesOnFinish());
	}

	/**
	 * Add a behaviour to the behaviours list of this brain.
	 * @param priority The behaviour's priority value
	 * @param activity The behaviour's activity category
	 * @param behaviour The behaviour instance
	 */
	public void addBehaviour(int priority, Activity activity, Task<? super E> behaviour) {
		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			if (behaviourGroup.priority == priority) {
				for (Pair<Activity, List<Task<? super E>>> pair : behaviourGroup.behaviours) {
					if (pair.getFirst() == activity) {
						pair.getSecond().add(behaviour);

						return;
					}
				}

				behaviourGroup.behaviours.add(Pair.of(activity, ObjectArrayList.wrap(new Task[] {behaviour})));

				return;
			}
		}

		this.behaviours.add(new ActivityBehaviours(
				priority, ObjectArrayList.wrap(
						new Pair[] {
								Pair.of(
										activity, ObjectArrayList.<Task<? super E>>wrap(new Task[] {behaviour})
										)
						})));
		this.sortBehaviours = true;
	}

	/**
	 * Remove a cached behaviour from the behaviours list of this brain. <br>
	 * Matching uses <b>reference parity</b>
	 * @param priority The behaviour's priority value
	 * @param activity The behaviour's activity category
	 * @param behaviour The behaviour instance
	 */
	public void removeBehaviour(int priority, Activity activity, Task<? super E> behaviour) {
		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			if (behaviourGroup.priority == priority) {
				for (Pair<Activity, List<Task<? super E>>> pair : behaviourGroup.behaviours) {
					if (pair.getFirst() == activity) {
						for (Iterator<Task<? super E>> iterator = pair.getSecond().iterator(); iterator.hasNext();) {
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

	private class ActivityBehaviours<T extends LivingEntity & SmartBrainOwner<T>> {
	
		private int priority;
		public int priority() {
			return priority;
		}

		private List<Pair<Activity, List<Task<? super T>>>> behaviours;
		
		public List<Pair<Activity, List<Task<? super T>>>> behaviours() {
			return behaviours;
		}

		public ActivityBehaviours(int priority, List<Pair<Activity, List<Task<? super T>>>> behaviours) {
			this.priority = priority;
			this.behaviours = behaviours;
		}
		
	}
}
