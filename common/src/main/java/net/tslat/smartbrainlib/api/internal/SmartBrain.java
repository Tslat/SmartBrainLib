
package net.tslat.smartbrainlib.api.internal;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryMap;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemorySlot;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.SmartBrainBuilder;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.ActivityBuilder;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.library.interfaces.BrainBehaviourPredicate;
import net.tslat.smartbrainlib.util.BehaviourUtil;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/// The core class for `SmartBrainLib`
///
/// This is the brain object that all entities that utilise `SmartBrainLib` will use.<br/>
/// Entities that implement [SmartBrainOwner] will construct and utilise an instance of this [Brain]
/// for AI
///
/// @param <BO> The brain owner entity
public class SmartBrain<BO extends LivingEntity & SmartBrainOwner<BO>> extends Brain<BO> {
	protected final Sensors sensors;
	protected final Behaviours behaviours;
	protected final @Nullable SmartBrainSchedule<BO, ?> schedule;
	protected final List<MemorySlot<?>> expirableMemories = new ObjectArrayList<>();

	public SmartBrain(Collection<MemoryModuleType<?>> memories, Collection<? extends ExtendedSensor<BO>> sensors, List<ActivityBuilder<BO>> activities, @Nullable SmartBrainSchedule<BO, ?> schedule, RandomSource random) {
		super(memories, List.of(), List.of(), MemoryMap.EMPTY, random);

		this.sensors = new Sensors(sensors, random);
		this.behaviours = new Behaviours(activities);
		this.availableBehaviorsByPriority = this.behaviours.asMapView();
        //noinspection rawtypes
        ((Brain)this).sensors = this.sensors.sensors;
		this.schedule = schedule;
	}

	/// Get the [Sensors] collection for this [SmartBrain] instance
	///
	/// Direct modifications to the collection should be avoided here; use one of the helper methods here or in [BrainUtil]
	public Sensors getSensors() {
		return this.sensors;
	}

	/// Get the [Behaviours] collection for this [SmartBrain] instance
	///
	/// Direct modifications to the collection should be avoided here; use one of the helper methods here or in [BrainUtil] instead
	public Behaviours getBehaviours() {
		return this.behaviours;
	}

	/// Get the time-based schedule for this [SmartBrain] instance if one is set
	public @Nullable SmartBrainSchedule<BO, ?> getActivitySchedule() {
		return this.schedule;
	}

	//<editor-fold defaultstate="collapsed" desc="<Sensors>">
	/// Container object holding all of a [SmartBrain]'s [Sensor]s, sorted by insertion order
	public class Sensors {
		protected final Map<SensorType<?>, ExtendedSensor<BO>> sensors;

		@ApiStatus.Internal
		protected Sensors(Collection<? extends ExtendedSensor<BO>> sensors, RandomSource random) {
			this.sensors = new Reference2ObjectArrayMap<>(sensors.size());

			for (ExtendedSensor<BO> sensor : sensors) {
				addSensor(sensor, random, false);
			}
		}

		/// Get a read-only view of all the [SensorType]s active on this [SmartBrain]
		public Collection<SensorType<?>> sensorTypes() {
			return new ObjectArrayList<>(this.sensors.keySet());
		}

		/// Add an [ExtendedSensor] to this [SmartBrain] instance, optionally automatically registering the required memories
		///
		/// You generally shouldn't be using this, and instead should be setting your brain up properly via your [SmartBrainBuilder]
		public void addSensor(ExtendedSensor<BO> sensor, RandomSource random, boolean registerMemories) {
			this.sensors.put(sensor.type(), sensor);
			sensor.randomlyDelayStart(random);

			if (registerMemories) {
				for (MemoryModuleType<?> memoryType : sensor.memoriesUsed()) {
					registerMemory(memoryType);
				}
			}
		}

		/// Remove a [Sensor] from this [Brain] matching the given [Predicate]
		///
		/// @return true if a [Sensor] was removed
		public boolean removeSensor(SensorType<?> sensorType) {
			return this.sensors.remove(sensorType) != null;
		}

		/// Tick all sensors on this [SmartBrain] instance
		public void tickSensors(ServerLevel level, BO entity) {
			SmartBrain.this.tickSensors(level, entity);
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Behaviours>">
	/// Container object holding all of a [SmartBrain]'s behaviours, sorted by priority and insertion order
	public class Behaviours {
		protected final List<ByPriority> behaviours;
		protected boolean needsSorting = true;

		protected Behaviours(List<ActivityBuilder<BO>> builders) {
			this.behaviours = new ObjectArrayList<>(Math.max(ObjectArrayList.DEFAULT_INITIAL_CAPACITY, builders.size()));

			for (ActivityBuilder<BO> builder : builders) {
				addActivity(builder, false);
			}
		}

		/// Get a [List] of all currently running top-level [BehaviorControl]s for this [SmartBrain]
		public List<BehaviorControl<? super BO>> getRunningBehaviours() {
			final List<BehaviorControl<? super BO>> runningBehaviours = new ObjectArrayList<>();

			for (Behaviours.ByPriority byPriority : this.behaviours) {
				for (BehaviorControl<? super BO> behaviour : byPriority.behaviours()) {
					if (behaviour.getStatus() == Behavior.Status.RUNNING)
						runningBehaviours.add(behaviour);
				}
			}

			return runningBehaviours;
		}

		/// Register an [Activity] with this [Behaviours] collection from the given [ActivityBuilder], optionally automatically registering the required memories
		///
		/// You generally shouldn't be using this, and instead should be setting your brain up properly via your [SmartBrainBuilder]
		public void addActivity(ActivityBuilder<BO> builder, boolean registerMemories) {
			final Activity activity = builder.getActivity();
			int priority = builder.getBasePriority();

			for (BehaviorControl<? super BO> behaviour : builder.getBehaviours()) {
				addBehaviour(activity, priority++, behaviour, registerMemories);
			}

			SmartBrain.this.activityRequirements.put(activity, builder.getStartConditions());
			SmartBrain.this.activityMemoriesToEraseWhenStopped.put(activity, builder.getClearedMemoriesOnFinish());
		}

		/// Add a [BehaviorControl] to this [SmartBrain] instance, optionally automatically registering the required memories
		///
		/// You generally shouldn't be using this, and instead should be setting your brain up properly via your [SmartBrainBuilder]
		public void addBehaviour(Activity activity, int priority, BehaviorControl<? super BO> behaviour, boolean registerMemories) {
			getOrCreatePriority(priority).addBehaviour(activity, behaviour);

			if (registerMemories) {
				for (MemoryModuleType<?> memoryType : SmartBrainBuilder.getMemoriesUsedByBehaviour(behaviour)) {
					SmartBrain.this.registerMemory(memoryType);
				}
			}
		}

		/// Remove all [BehaviorControl]s associated with the given [Activity] in this collection
		///
		/// @return true if any behaviours were removed
		public boolean removeActivity(Activity activity, BO entity) {
			boolean result = false;

			for (ByPriority byPriority : this.behaviours) {
				result |= byPriority.removeActivity(activity, entity);
			}

			return result;
		}

		/// Remove the first [BehaviorControl] associated with the given [Activity] in this collection that matches the provided [predicate][BrainBehaviourPredicate]
		///
		/// @return true if any behaviours were removed
		@SuppressWarnings("UnusedReturnValue")
        public boolean removeBehaviour(Activity activity, BO entity, BrainBehaviourPredicate<BO> predicate) {
			for (ByPriority byPriority : this.behaviours) {
				if (byPriority.removeBehaviour(activity, entity, predicate))
					return true;
			}

			return false;
		}

		/// Create a [Map] view of this behaviour collection, for compatibility with vanilla's [Brain#availableBehaviorsByPriority]
		@ApiStatus.Internal
		ActivityMapRedirect<BO> asMapView() {
			return ActivityMapRedirect.wrap(this);
		}

		/// Sort this instance's [ByPriority] list to order by natural priority order
		protected void sort() {
			if (this.needsSorting) {
				this.behaviours.sort(Comparator.comparing(ByPriority::priority));
				this.needsSorting = false;
			}
		}

		/// Get or create a [ByPriority] instance for the given priority value on this `Behaviours` instance
		protected ByPriority getOrCreatePriority(int priority) {
			for (ByPriority group : this.behaviours) {
				if (group.priority == priority)
					return group;
			}

			this.needsSorting = true;
			final ByPriority byPriority = new ByPriority(priority);

			this.behaviours.add(byPriority);

			return byPriority;
		}

		/// Stop all currently running [BehaviorControl]s
		public void stopAll(ServerLevel level, BO entity) {
			final long gameTime = level.getGameTime();

			for (Behaviours.ByPriority byPriority : this.behaviours) {
				for (BehaviorControl<? super BO> behaviour : byPriority.behaviours()) {
					if (behaviour.getStatus() == Behavior.Status.RUNNING)
						behaviour.doStop(level, entity, gameTime);
				}
			}
		}

		/// Check all currently available non-running behaviours and attempt to start them
		protected void tryStartBehaviours(ServerLevel level, BO entity) {
			final long gameTime = level.getGameTime();

			for (ByPriority byPriority : this.behaviours) {
				for (Map.Entry<Activity, List<BehaviorControl<? super BO>>> entry : byPriority.behaviours.entrySet()) {
                    //noinspection deprecation
                    if (getActiveActivities().contains(entry.getKey())) {
						for (BehaviorControl<? super BO> behaviour : entry.getValue()) {
							if (behaviour.getStatus() == Behavior.Status.STOPPED)
								behaviour.tryStart(level, entity, gameTime);
						}
					}
				}
			}
		}

		/// Check all currently available non-running behaviours and attempt to start them
		protected void tickBehaviours(ServerLevel level, BO entity) {
			final long gameTime = level.getGameTime();

			for (ByPriority byPriority : this.behaviours) {
				for (Map.Entry<Activity, List<BehaviorControl<? super BO>>> entry : byPriority.behaviours.entrySet()) {
					//noinspection deprecation
					final boolean isActiveActivity = getActiveActivities().contains(entry.getKey());

					for (BehaviorControl<? super BO> behaviour : entry.getValue()) {
						if (behaviour.getStatus() == Behavior.Status.RUNNING) {
							if (!isActiveActivity && behaviour instanceof ExtendedBehaviour<? super BO> extendedBehaviour && extendedBehaviour.tryExpire(level, entity, gameTime))
								continue;

							behaviour.tickOrStop(level, entity, gameTime);
						}
					}
				}
			}
		}

		/// Stop all currently running [BehaviorControl]s for the given [Activity]
		protected void stopBehavioursForActivity(ServerLevel level, BO entity, Activity activity) {
			final long gameTime = level.getGameTime();

			for (ByPriority byPriority : this.behaviours) {
				byPriority.behaviours.getOrDefault(activity, List.of())
						.forEach(behaviour -> behaviour.doStop(level, entity, gameTime));
			}
		}

		/// Container object pairing an `int` priority value with an associated list of [behaviours][BehaviorControl]<br/>
		/// This allows for iteration-optimised handling of `Behaviours` with minimal memory footprint and overhead
		@ApiStatus.Internal
		public class ByPriority {
			protected final int priority;
			protected final Reference2ObjectArrayMap<Activity, List<BehaviorControl<? super BO>>> behaviours;

			protected ByPriority(int priority) {
				this.priority = priority;
				this.behaviours = new Reference2ObjectArrayMap<>();
			}

			/// Get the priority ordinal for this [ByPriority] group
			public int priority() {
				return this.priority;
			}

			/// Get a list of all behaviours registered under this priority, in their insertion order
			///
			/// This is a **read-only** view of the behaviour collection
			public Iterable<BehaviorControl<? super BO>> behaviours() {
				return Iterables.unmodifiableIterable(Iterables.concat(this.behaviours.values()));
			}

			/// Add a [BehaviorControl] to this group's behaviours for the given [Activity]
			protected void addBehaviour(Activity activity, BehaviorControl<? super BO> behaviour) {
				this.behaviours.computeIfAbsent(activity, _ -> new ObjectArrayList<>()).add(behaviour);
			}

			/// Remove the first [BehaviorControl] from this group that matches the provided [BrainBehaviourPredicate]
			///
			/// @return true if a behaviour was removed
			protected boolean removeBehaviour(Activity activity, BO entity, BrainBehaviourPredicate<BO> predicate) {
                return removeBehaviourRecursively(this.behaviours.getOrDefault(activity, List.of()), new Stack<>(), activity, entity, predicate);
			}

			/// Recursively the first [BehaviorControl] from the provided iterable that matches the provided [BrainBehaviourPredicate]
			///
			/// @return true if a behaviour was removed
			protected boolean removeBehaviourRecursively(Iterable<? extends BehaviorControl<? super BO>> behaviours, Stack<BehaviorControl<? super BO>> parentBehaviours, Activity activity,
			                                             BO entity, BrainBehaviourPredicate<BO> predicate) {
				for (Iterator<? extends BehaviorControl<? super BO>> iterator = behaviours.iterator(); iterator.hasNext();) {
					final BehaviorControl<? super BO> behaviour = iterator.next();

					if (predicate.isBehaviour(behaviour, activity, this.priority, parentBehaviours)) {
						if (behaviour.getStatus() != Behavior.Status.STOPPED)
							behaviour.doStop((ServerLevel)entity.level(), entity, entity.level().getGameTime());

						iterator.remove();
						return true;
					}
					else {
						parentBehaviours.push(behaviour);

						if (removeBehaviourRecursively(BehaviourUtil.getChildrenOfBehaviour(behaviour), parentBehaviours, activity, entity, predicate)) {
							parentBehaviours.pop();

							return true;
						}

						parentBehaviours.pop();
					}
				}

				return false;
			}

			/// Remove all [BehaviorControl]s from this group that belong to the given [Activity]
			///
			/// @return true if any behaviours were removed
			protected boolean removeActivity(Activity activity, BO entity) {
				final List<BehaviorControl<? super BO>> removedBehaviours = this.behaviours.remove(activity);

				//noinspection ConstantValue
				if (removedBehaviours != null) {
					for (BehaviorControl<? super BO> behaviour : removedBehaviours) {
						behaviour.doStop((ServerLevel)entity.level(), entity, entity.level().getGameTime());
					}

					SmartBrain.this.activityRequirements.remove(activity);
					SmartBrain.this.activityMemoriesToEraseWhenStopped.remove(activity);

					return true;
				}

				return false;
			}
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Methods>">
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public List<BehaviorControl<? super BO>> getRunningBehaviors() {
		return getBehaviours().getRunningBehaviours();
	}

	@Override
	public void tick(ServerLevel level, BO entity) {
		final ProfilerFiller profiler = Profiler.get();
		final Behaviours behaviours = getBehaviours();

		profiler.push("smartBrainPreTick");

		behaviours.sort();
		forgetOutdatedMemories();
		profiler.popPush("smartBrainSensors");
		getSensors().tickSensors(level, entity);
		profiler.popPush("smartBrainBehaviours");
		behaviours.tryStartBehaviours(level, entity);
		behaviours.tickBehaviours(level, entity);
		profiler.popPush("smartBrainActivityUpdate");
		updateCurrentActivity(entity);
		profiler.pop();
	}

	@Override
	public void stopAll(ServerLevel level, BO entity) {
		getBehaviours().stopAll(level, entity);
	}

	@Override
	public <M> MemorySlot<M> getMemorySlotIfPresent(MemoryModuleType<M> memoryType) {
        //noinspection unchecked
        return (MemorySlot<M>)this.memories.computeIfAbsent(memoryType, _ -> MemorySlot.create());
	}

	@Override
	public void forgetOutdatedMemories() {
		this.expirableMemories.forEach(MemorySlot::tick);
		this.expirableMemories.removeIf(memory -> {
			if (!memory.canExpire() || !memory.hasValue() || memory.hasExpired()) {
				memory.clear();

				return true;
			}

			return false;
		});
	}

	@Override
	public void clearMemories() {
		super.clearMemories();
		this.expirableMemories.clear();
	}

	@Override
	public <M> void setMemoryInternal(MemoryModuleType<M> type, M value, long lifespan) {
		super.setMemoryInternal(type, value, lifespan);

		final MemorySlot<M> memory = getMemorySlotIfPresent(type);

		if (memory.hasValue() && memory.canExpire() && !memory.hasExpired())
			this.expirableMemories.add(memory);
	}

	/// Determine the [Activity] that should currently be active based on the [SmartBrain] owner's context and apply it
	protected void updateCurrentActivity(BO entity) {
		final Optional<Activity> nextActivity = entity.getCurrentRelevantActivity(this::activityRequirementsAreMet);

		if (nextActivity.filter(entity.getScheduleIgnoringActivities()::contains).isPresent()) {
			setActiveActivity(nextActivity.get());

			return;
		}

		final SmartBrainSchedule<BO, ?> schedule = getActivitySchedule();

		if (schedule != null) {
			final Activity scheduledActivity = schedule.tick(entity);

			if (scheduledActivity != null && !isActive(scheduledActivity) && activityRequirementsAreMet(scheduledActivity)) {
				setActiveActivity(scheduledActivity);

				return;
			}
		}

		nextActivity.ifPresent(this::setActiveActivity);
	}

	/// SmartBrainLib does not use these schedules
	/// @see SmartBrainBuilder#getSchedule()
	@Deprecated
	@Override
	public void setSchedule(EnvironmentAttribute<Activity> schedule) {
		super.setSchedule(schedule);
	}
	//</editor-fold>
}