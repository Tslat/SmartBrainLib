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
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.behaviour.GroupBehaviour;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.BrainBehaviourConsumer;
import net.tslat.smartbrainlib.object.BrainBehaviourPredicate;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Supercedes vanilla's {@link Brain}. One of the core components of the SBL
 * library. <br>
 * Any entity that returns a {@link SmartBrainProvider} from
 * {@link LivingEntity#brainProvider()} will have one of these.
 * 
 * @param <E> The entity
 */
public class SmartBrain<E extends LivingEntity & SmartBrainOwner<E>> extends Brain<E> {
	protected final List<MemoryModuleType<?>> expirableMemories = new ObjectArrayList<>();
	protected final List<ActivityBehaviours<E>> behaviours = new ObjectArrayList<>();
	protected final List<Pair<SensorType<ExtendedSensor<? super E>>, ExtendedSensor<? super E>>> sensors = new ObjectArrayList<>();
	protected SmartBrainSchedule schedule = null;

	protected boolean sortBehaviours = false;

	public SmartBrain(List<MemoryModuleType<?>> memories, List<? extends ExtendedSensor<E>> sensors, @Nullable List<BrainActivityGroup<E>> taskList) {
		super(memories, ImmutableList.of(), ImmutableList.of(), SmartBrain::emptyBrainCodec);

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
	public void tick(ServerLevel level, E entity) {
		entity.level().getProfiler().push("SmartBrain");

		if (this.sortBehaviours)
			this.behaviours.sort(Comparator.comparingInt(ActivityBehaviours::priority));

		forgetOutdatedMemories();
		tickSensors(level, entity);
		checkForNewBehaviours(level, entity);
		tickRunningBehaviours(level, entity);
		findAndSetActiveActivity(entity);

		entity.level().getProfiler().pop();

		if (entity instanceof Mob mob)
			mob.setAggressive(BrainUtils.hasMemory(mob, MemoryModuleType.ATTACK_TARGET));
	}

	protected void findAndSetActiveActivity(E entity) {
		Activity nextActivity = getFirstValidActivity(entity.getActivityPriorities());

		if (nextActivity != null && entity.getScheduleIgnoringActivities().contains(nextActivity)) {
			setActiveActivity(nextActivity);

			return;
		}

		if (this.schedule != null) {
			Activity scheduledActivity = this.schedule.tick(entity);

			if (scheduledActivity != null && activityRequirementsAreMet(scheduledActivity)) {
				if (!isActive(scheduledActivity))
					setActiveActivity(scheduledActivity);

				return;
			}
		}

		if (nextActivity != null)
			setActiveActivity(nextActivity);
	}

	@Nullable
	protected Activity getFirstValidActivity(List<Activity> activities) {
		for (Activity activity : activities) {
			if (activityRequirementsAreMet(activity))
				return activity;
		}

		return null;
	}

	protected void tickSensors(ServerLevel level, E entity) {
		for (Pair<SensorType<ExtendedSensor<? super E>>, ExtendedSensor<? super E>> sensor : this.sensors) {
			sensor.getSecond().tick(level, entity);
		}
	}

	protected void checkForNewBehaviours(ServerLevel level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<BehaviorControl<? super E>>> pair : behaviourGroup.behaviours) {
				if (getActiveActivities().contains(pair.getFirst())) {
					for (BehaviorControl<? super E> behaviour : pair.getSecond()) {
						if (behaviour.getStatus() == Behavior.Status.STOPPED)
							behaviour.tryStart(level, entity, gameTime);
					}
				}
			}
		}
	}

	protected void tickRunningBehaviours(ServerLevel level, E entity) {
		long gameTime = level.getGameTime();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<BehaviorControl<? super E>>> pair : behaviourGroup.behaviours) {
				for (BehaviorControl<? super E> behaviour : pair.getSecond()) {
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
			for (Pair<Activity, List<BehaviorControl<? super E>>> pair : behaviourGroup.behaviours) {
				for (BehaviorControl<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Behavior.Status.RUNNING)
						behaviour.doStop(level, entity, gameTime);
				}
			}
		}
	}

	@Override
	public <U> Optional<U> getMemory(MemoryModuleType<U> type) {
		return (Optional<U>) this.memories.computeIfAbsent(type, key -> Optional.empty()).map(ExpirableValue::getValue);
	}

	@Override
	public <U> void setMemoryInternal(MemoryModuleType<U> memoryType, Optional<? extends ExpirableValue<?>> memory) {
		if (memory.isPresent() && memory.get().getValue()instanceof Collection<?> collection && collection.isEmpty())
			memory = Optional.empty();

		this.memories.put(memoryType, memory);

		if (memory.isPresent() && memory.get().canExpire() && !this.expirableMemories.contains(memoryType))
			this.expirableMemories.add(memoryType);
	}

	@Override
	public <U> boolean isMemoryValue(MemoryModuleType<U> memoryType, U memory) {
		Optional<U> value = getMemory(memoryType);

		return value.isPresent() && value.get().equals(memory);
	}

	protected static <E extends LivingEntity & SmartBrainOwner<E>> Codec<Brain<E>> emptyBrainCodec() {
		MutableObject<Codec<Brain<E>>> brainCodec = new MutableObject<>();

		brainCodec.setValue(Codec.unit(() -> new Brain<>(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), brainCodec::getValue)));

		return brainCodec.getValue();
	}

	protected static <E extends LivingEntity & SmartBrainOwner<E>> List<? extends SensorType<? extends Sensor<? super E>>> convertSensorsToTypes(List<? extends ExtendedSensor<E>> sensors) {
		List<SensorType<? extends Sensor<? super E>>> types = new ObjectArrayList<>(sensors.size());

		for (ExtendedSensor<?> sensor : sensors) {
			types.add((SensorType)sensor.type());
		}

		return types;
	}

	@Override
	public Brain<E> copyWithoutBehaviors() {
		SmartBrain<E> brain = new SmartBrain<>(this.memories.keySet().stream().toList(), this.sensors.stream().map(pair -> (ExtendedSensor<E>) pair.getSecond()).toList(), null);

		for (Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
			MemoryModuleType<?> memoryType = entry.getKey();

			if (entry.getValue().isPresent())
				brain.memories.put(memoryType, entry.getValue());
		}

		return brain;
	}

	@Override
	public List<BehaviorControl<? super E>> getRunningBehaviors() {
		List<BehaviorControl<? super E>> runningBehaviours = new ObjectArrayList<>();

		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			for (Pair<Activity, List<BehaviorControl<? super E>>> pair : behaviourGroup.behaviours) {
				for (BehaviorControl<? super E> behaviour : pair.getSecond()) {
					if (behaviour.getStatus() == Behavior.Status.RUNNING)
						runningBehaviours.add(behaviour);
				}
			}
		}

		return runningBehaviours;
	}

	/**
	 * Returns a stream of all {@link BehaviorControl Behaviours} registered to this brain
	 */
	public Stream<BehaviorControl<? super E>> getBehaviours() {
		return this.behaviours.stream().map(ActivityBehaviours::behaviours).flatMap(list -> list.stream().map(Pair::getSecond).flatMap(List::stream));
	}

	@Override
	public void removeAllBehaviors() {
		this.behaviours.clear();
	}

	@Override
	public void addActivityAndRemoveMemoriesWhenStopped(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> tasks, Set<Pair<MemoryModuleType<?>, MemoryStatus>> memorieStatuses, Set<MemoryModuleType<?>> memoryTypes) {
		this.activityRequirements.put(activity, memorieStatuses);

		if (!memoryTypes.isEmpty())
			this.activityMemoriesToEraseWhenStopped.put(activity, memoryTypes);

		for (Pair<Integer, ? extends BehaviorControl<? super E>> pair : tasks) {
			addBehaviour(pair.getFirst(), activity, pair.getSecond());
		}
	}

	/**
	 * Adds a full {@link BrainActivityGroup} to the brain, inclusive of activities and conditions
	 */
	public void addActivity(BrainActivityGroup<E> activityGroup) {
		addActivityAndRemoveMemoriesWhenStopped(activityGroup.getActivity(), activityGroup.pairBehaviourPriorities(), activityGroup.getActivityStartMemoryConditions(), activityGroup.getWipedMemoriesOnFinish());
	}

	/**
	 * Add a behaviour to the behaviours list of this brain.
	 * 
	 * @param priority  The behaviour's priority value
	 * @param activity  The behaviour's activity category
	 * @param behaviour The behaviour instance
	 */
	public void addBehaviour(int priority, Activity activity, BehaviorControl<? super E> behaviour) {
		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			if (behaviourGroup.priority == priority) {
				for (Pair<Activity, List<BehaviorControl<? super E>>> pair : behaviourGroup.behaviours) {
					if (pair.getFirst() == activity) {
						pair.getSecond().add(behaviour);

						return;
					}
				}

				behaviourGroup.behaviours.add(Pair.of(activity, ObjectArrayList.of(behaviour)));

				return;
			}
		}

		this.behaviours.add(new ActivityBehaviours<>(priority, ObjectArrayList.of(Pair.of(activity, ObjectArrayList.<BehaviorControl<? super E>>of(behaviour)))));
		this.sortBehaviours = true;
	}

	/**
	 * Removes any behaviours matching the given predicate from the provided brain.<br>
	 * Removed behaviours are stopped prior to removal
	 * @param entity The owner of the brain
	 * @param predicate The predicate checked for each (priority, activity, behaviour)
	 */
	public void removeBehaviour(E entity, BrainBehaviourPredicate predicate) {
		for (ActivityBehaviours<E> behaviourGroup : this.behaviours) {
			int priority = behaviourGroup.priority;

			for (Pair<Activity, List<BehaviorControl<? super E>>> pair : behaviourGroup.behaviours) {
				Activity activity = pair.getFirst();

				for (Iterator<BehaviorControl<? super E>> iterator = pair.getSecond().iterator(); iterator.hasNext();) {
					BehaviorControl<? super E> behaviour = iterator.next();

					checkBehaviour(priority, activity, behaviour, null, predicate, () -> {
						if (behaviour.getStatus() == Behavior.Status.RUNNING)
							behaviour.doStop((ServerLevel)entity.level(), entity, entity.level().getGameTime());

						iterator.remove();
					});
				}
			}
		}
	}

	/**
	 * Sets a {@link SmartBrainSchedule} for this brain, for scheduled functionality
	 * @param schedule The schedule to set for the brain
	 * @return this
	 */
	public SmartBrain<E> setSchedule(SmartBrainSchedule schedule) {
		this.schedule = schedule;

		return this;
	}

	/**
	 * @return The {@link SmartBrainSchedule schedule} of this brain
	 */
	@Override
	public SmartBrainSchedule getSchedule() {
		return this.schedule;
	}

	/**
	 * Cheekily (and conveniently) uses the {@link SmartBrainSchedule schedule} system to schedule a delayed runnable for this entity/brain.
	 * @param delay The delay (in ticks) before running the task
	 * @param task The task to run at the given tick
	 */
	public void scheduleTask(E brainOwner, int delay, Consumer<E> task) {
		if (this.schedule == null)
			this.schedule = new SmartBrainSchedule();

		this.schedule.scheduleTask(brainOwner, delay, (Consumer)task);
	}

	protected static <E extends LivingEntity> void checkBehaviour(int priority, Activity activity, BehaviorControl<E> behaviour, @Nullable BehaviorControl<E> parentBehaviour, BrainBehaviourPredicate predicate, Runnable callback) {
		if (predicate.isBehaviour(priority, activity, behaviour, parentBehaviour)) {
			callback.run();
		}
		else if (behaviour instanceof GateBehavior groupBehaviour) {
			for (Iterator<BehaviorControl<E>> childBehaviourIterator = groupBehaviour.behaviors.iterator(); childBehaviourIterator.hasNext();) {
				checkBehaviour(priority, activity, childBehaviourIterator.next(), groupBehaviour, predicate, childBehaviourIterator::remove);
			}

			if (!groupBehaviour.behaviors.iterator().hasNext())
				callback.run();
		}
		else if (behaviour instanceof GroupBehaviour groupBehaviour) {
			for (Iterator<BehaviorControl<E>> childBehaviourIterator = groupBehaviour.getBehaviours(); childBehaviourIterator.hasNext();) {
				checkBehaviour(priority, activity, childBehaviourIterator.next(), groupBehaviour, predicate, childBehaviourIterator::remove);
			}

			if (!groupBehaviour.getBehaviours().hasNext())
				callback.run();
		}
	}

	/**
	 * Loops over all {@link BehaviorControl Behaviours} registered to this brain, calling the consumer for each
	 * @param consumer The consumer called for each (priority, activity, behaviour)
	 */
	public void forEachBehaviour(BrainBehaviourConsumer consumer) {
		for (ActivityBehaviours<E> behavioursGroup : this.behaviours) {
			int priority = behavioursGroup.priority();

			for (Pair<Activity, List<BehaviorControl<? super E>>> behaviourList : behavioursGroup.behaviours()) {
				Activity activity = behaviourList.getFirst();

				for (BehaviorControl<? super E> behaviour : behaviourList.getSecond()) {
					consumeBehaviour(priority, activity, behaviour, null, consumer);
				}
			}
		}
	}

	protected static <E extends LivingEntity> void consumeBehaviour(int priority, Activity activity, BehaviorControl<E> behaviour, @Nullable BehaviorControl<E> parentBehaviour, BrainBehaviourConsumer consumer) {
		consumer.consume(priority, activity, behaviour, parentBehaviour);

		if (behaviour instanceof GateBehavior<E> groupBehaviour) {
			groupBehaviour.behaviors.stream().forEach(childBehaviour -> consumeBehaviour(priority, activity, (BehaviorControl)childBehaviour, groupBehaviour, consumer));
		}
		else if (behaviour instanceof GroupBehaviour<E> groupBehaviour) {
			groupBehaviour.getBehaviours().forEachRemaining(childBehaviour -> consumeBehaviour(priority, activity, (BehaviorControl)childBehaviour, groupBehaviour, consumer));
		}
	}

	/**
	 * Adds an {@link ExtendedSensor} to this brain
	 */
	public void addSensor(ExtendedSensor<E> sensor) {
		SensorType<ExtendedSensor<? super E>> sensorType = (SensorType)sensor.type();

		this.sensors.add(Pair.of(sensorType, sensor));
	}

	/**
	 * Not supported, use {@link SmartBrain#setSchedule(SmartBrainSchedule)} instead
	 */
	@Deprecated(forRemoval = true)
	@Override
	public final void setSchedule(Schedule schedule) {}

	protected record ActivityBehaviours<E extends LivingEntity & SmartBrainOwner<E>> (int priority, List<Pair<Activity, List<BehaviorControl<? super E>>>> behaviours) {}
}
