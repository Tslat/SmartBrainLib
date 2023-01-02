package net.tslat.smartbrainlib.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.Timeline;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrain;
import net.tslat.smartbrainlib.api.core.behaviour.GroupBehaviour;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.BrainBehaviourConsumer;
import net.tslat.smartbrainlib.object.BrainBehaviourPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utility class for various brain functions. Try to utilise this where possible to ensure consistency and safety.
 */
public final class BrainUtils {
	/**
	 * Get a memory value from an entity, with a fallback value if no memory is present
	 *
	 * @param entity The entity
	 * @param memory Memory type to get the value for
	 * @param fallback Fallback value if no memory value is present
	 * @return The stored memory, or fallback value if no memory was stored
	 * @param <T> The type of object the memory uses
	 */
	public static <T> T memoryOrDefault(LivingEntity entity, MemoryModuleType<T> memory, Supplier<T> fallback) {
		return memoryOrDefault(entity.getBrain(), memory, fallback);
	}

	/**
	 * Get a memory value from a brain, with a fallback value if no memory is present
	 *
	 * @param brain The brain
	 * @param memory Memory type to get the value for
	 * @param fallback Fallback value if no memory value is present
	 * @return The stored memory, or fallback value if no memory was stored
	 * @param <T> The type of object the memory uses
	 */
	public static <T> T memoryOrDefault(Brain<?> brain, MemoryModuleType<T> memory, Supplier<T> fallback) {
		return brain.getMemory(memory).orElseGet(fallback);
	}

	/**
	 * Get a memory value from an entity, or null if no memory is present
	 *
	 * @param entity The entity
	 * @param memory Memory type to get the value for
	 * @return The stored memory, or null if no memory was stored
	 * @param <T> The type of object the memory uses
	 */
	@Nullable
	public static <T> T getMemory(LivingEntity entity, MemoryModuleType<T> memory) {
		return getMemory(entity.getBrain(), memory);
	}

	/**
	 * Get a memory value from a brain, or null if no memory is present
	 *
	 * @param brain The brain
	 * @param memory Memory type to get the value for
	 * @return The stored memory, or null if no memory was stored
	 * @param <T> The type of object the memory uses
	 */
	@Nullable
	public static <T> T getMemory(Brain<?> brain, MemoryModuleType<T> memory) {
		return memoryOrDefault(brain, memory, () -> null);
	}

	/**
	 * Perform an operation on a given memory value, if present. If no memory value set, operation is not run
	 *
	 * @param entity The entity
	 * @param memory Memory type to get the value for
	 * @param consumer The operation to run if the memory is present
	 * @param <T> The type of object the memory uses
	 */
	public static <T> void withMemory(LivingEntity entity, MemoryModuleType<T> memory, Consumer<T> consumer) {
		withMemory(entity.getBrain(), memory, consumer);
	}

	/**
	 * Perform an operation on a given memory value, if present. If no memory value set, operation is not run
	 *
	 * @param brain The brain
	 * @param memory Memory type to get the value for
	 * @param consumer The operation to run if the memory is present
	 * @param <T> The type of object the memory uses
	 */
	public static <T> void withMemory(Brain<?> brain, MemoryModuleType<T> memory, Consumer<T> consumer) {
		brain.getMemory(memory).ifPresent(consumer);
	}

	/**
	 * Check whether an entity has a memory value set.
	 *
	 * @param entity The entity
	 * @param memory Memory type to get the value for
	 * @return True if the memory value is present, or false if the memory value is absent or unregistered
	 */
	public static boolean hasMemory(LivingEntity entity, MemoryModuleType<?> memory) {
		return hasMemory(entity.getBrain(), memory);
	}
	/**
	 * Check whether a brain has a memory value set.
	 *
	 * @param brain The brain
	 * @param memory Memory type to get the value for
	 * @return True if the memory value is present, or false if the memory value is absent or unregistered
	 */
	public static boolean hasMemory(Brain<?> brain, MemoryModuleType<?> memory) {
		return brain.hasMemoryValue(memory);
	}

	/**
	 * Gets the ticks remaining until a memory expires.
	 * @param entity The entity
	 * @param memory Memory type to get the expiry time for
	 * @return The ticks until the memory expires, or 0 if the memory doesn't exist or doesn't expire
	 */
	public static long getTimeUntilMemoryExpires(LivingEntity entity, MemoryModuleType<?> memory) {
		return getTimeUntilMemoryExpires(entity.getBrain(), memory);
	}

	/**
	 * Gets the ticks remaining until a memory expires.
	 * @param brain The brain
	 * @param memory Memory type to get the expiry time for
	 * @return The ticks until the memory expires, or 0 if the memory doesn't exist or doesn't expire
	 */
	public static long getTimeUntilMemoryExpires(Brain<?> brain, MemoryModuleType<?> memory) {
		return brain.getTimeUntilExpiry(memory);
	}

	/**
	 * Set an entity's memory value for the given memory type. <br>
	 * Use {@link BrainUtils#clearMemory(LivingEntity, MemoryModuleType)} if intending to set a memory to nothing.
	 *
	 * @param entity The entity
	 * @param memoryType Memory type to set the value for
	 * @param memory The memory value to set
	 * @param <T> The type of object the memory uses
	 */
	public static <T> void setMemory(LivingEntity entity, MemoryModuleType<T> memoryType, T memory) {
		setMemory(entity.getBrain(), memoryType, memory);
	}

	/**
	 * Set a brain's memory value for the given memory type. <br>
	 * Use {@link BrainUtils#clearMemory(Brain, MemoryModuleType)} if intending to set a memory to nothing.
	 *
	 * @param brain The brain
	 * @param memoryType Memory type to set the value for
	 * @param memory The memory value to set
	 * @param <T> The type of object the memory uses
	 */
	public static <T> void setMemory(Brain<?> brain, MemoryModuleType<T> memoryType, T memory) {
		brain.setMemory(memoryType, memory);
	}

	/**
	 * Set a brain's memory value for the given memory type, with the memory expiring after a certain time.<br>
	 * Use {@link BrainUtils#clearMemory(LivingEntity, MemoryModuleType)} if intending to set a memory to nothing.
	 * @param entity The entity
	 * @param memoryType Memory type to set the value for
	 * @param memory The memory value to set
	 * @param expirationTicks How many ticks until the memory expires
	 * @param <T> The type of object the memory uses
	 */
	public static <T> void setForgettableMemory(LivingEntity entity, MemoryModuleType<T> memoryType, T memory, int expirationTicks) {
		setForgettableMemory(entity.getBrain(), memoryType, memory, expirationTicks);
	}

	/**
	 * Set an entity's memory value for the given memory type, with the memory expiring after a certain time.<br>
	 * Use {@link BrainUtils#clearMemory(Brain, MemoryModuleType)} if intending to set a memory to nothing.
	 * @param brain The brain
	 * @param memoryType Memory type to set the value for
	 * @param memory The memory value to set
	 * @param expirationTicks How many ticks until the memory expires
	 * @param <T> The type of object the memory uses
	 */
	public static <T> void setForgettableMemory(Brain<?> brain, MemoryModuleType<T> memoryType, T memory, int expirationTicks) {
		brain.setMemoryWithExpiry(memoryType, memory, expirationTicks);
	}

	/**
	 * Wipe an entity's memory value for the given memory type. This safely unsets a memory, returning it to empty.
	 *
	 * @param entity The entity
	 * @param memory Memory type to erase the value for
	 */
	public static void clearMemory(LivingEntity entity, MemoryModuleType<?> memory) {
		clearMemory(entity.getBrain(), memory);
	}

	/**
	 * Wipe a brain's memory value for the given memory type. This safely unsets a memory, returning it to empty.
	 *
	 * @param brain The brain
	 * @param memory Memory type to erase the value for
	 */
	public static void clearMemory(Brain<?> brain, MemoryModuleType<?> memory) {
		brain.eraseMemory(memory);
	}

	/**
	 * Wipe multiple memories for a given entity. This safely unsets each memory, returning them to empty.
	 *
	 * @param entity The entity
	 * @param memories The list of memory types to erase the values for
	 */
	public static void clearMemories(LivingEntity entity, MemoryModuleType<?>... memories) {
		clearMemories(entity.getBrain(), memories);
	}

	/**
	 * Wipe multiple memories for a given brain. This safely unsets each memory, returning them to empty.
	 *
	 * @param brain The brain
	 * @param memories The list of memory types to erase the values for
	 */
	public static void clearMemories(Brain<?> brain, MemoryModuleType<?>... memories) {
		for (MemoryModuleType<?> memory : memories) {
			brain.eraseMemory(memory);
		}
	}

	/**
	 * Gets the current attack target of an entity, if present.
	 *
	 * @param entity The entity
	 * @return The current attack target of the entity, or null if none present
	 */
	@Nullable
	public static LivingEntity getTargetOfEntity(LivingEntity entity) {
		return getTargetOfEntity(entity, null);
	}

	/**
	 * Gets the current attack target of an entity, if present, or an optional fallback entity if none present
	 *
	 * @param entity The entity
	 * @param fallback Optional fallback entity to return if no attack target is set.
	 * @return The current attack target of the entity, the fallback entity if provided, or null otherwise
	 */
	@Nullable
	public static LivingEntity getTargetOfEntity(LivingEntity entity, @Nullable LivingEntity fallback) {
		return memoryOrDefault(entity.getBrain(), MemoryModuleType.ATTACK_TARGET, () -> fallback);
	}

	/**
	 * Gets the last entity to attack the given entity, if present. <br>
	 * Requires that the entity uses the {@link MemoryModuleType#HURT_BY_ENTITY} memory type, and a sensor that sets it
	 *
	 * @param entity The entity
	 * @return The last entity to attack the given entity, or null if none present
	 */
	@Nullable
	public static LivingEntity getLastAttacker(LivingEntity entity) {
		return memoryOrDefault(entity, MemoryModuleType.HURT_BY_ENTITY, null);
	}

	/**
	 * Sets the attack target of the given entity, and safely sets the non-brain attack target for compatibility purposes. <br>
	 * Provided target can be null to effectively remove an entity's attack target.
	 *
	 * @param entity The entity
	 * @param target The entity to target
	 */
	public static void setTargetOfEntity(LivingEntity entity, @Nullable LivingEntity target) {
		if (entity instanceof Mob mob)
			mob.setTarget(target);

		if (target == null) {
			clearMemory(entity, MemoryModuleType.ATTACK_TARGET);
		}
		else {
			setMemory(entity, MemoryModuleType.ATTACK_TARGET, target);
		}
	}
	
	/**
	 * Replacement of {@link net.minecraft.world.entity.ai.behavior.BehaviorUtils#canSee}, falling back to a raytrace check in the event the target entity isn't in the {@link MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES} memory
	 * @param entity The entity to check the brain of
	 * @param target The target entity
	 * @return Whether the target entity is known to be visible or not
	 */
	public static boolean canSee(LivingEntity entity, LivingEntity target) {
		Brain<?> brain = entity.getBrain();

		if (BehaviorUtils.entityIsVisible(brain, target))
			return true;

		return entity.hasLineOfSight(target);
	}

	/**
	 * Returns a stream of all {@link Behavior Behaviours} registered to this brain
	 */
	public static Stream<Behavior<?>> getAllBehaviours(Brain<?> brain) {
		if (brain instanceof SmartBrain smartBrain)
			return smartBrain.getBehaviours();

		return brain.availableBehaviorsByPriority.values().stream()
				.map(Map::values)
				.flatMap(set -> set.stream().map(value -> value.stream().toList()).flatMap(List::stream));
	}

	/**
	 * Loops over all {@link Behavior Behaviours} registered to this brain, calling the consumer for each
	 * @param brain The brain to scrape the behaviours of
	 * @param consumer The consumer called for each
	 */
	public static void forEachBehaviour(Brain<?> brain, BrainBehaviourConsumer consumer) {
		if (brain instanceof SmartBrain smartBrain) {
			smartBrain.forEachBehaviour(consumer);

			return;
		}

		Set<Map.Entry<Integer, Map<Activity, Set<Behavior<?>>>>> behaviours = (Set)brain.availableBehaviorsByPriority.entrySet();

		for (Map.Entry<Integer, Map<Activity, Set<Behavior<?>>>> priorityEntry : behaviours) {
			Integer priority = priorityEntry.getKey();

			for (Map.Entry<Activity, Set<Behavior<?>>> activityEntry : priorityEntry.getValue().entrySet()) {
				Activity activity = activityEntry.getKey();

				for (Behavior<?> behaviour : activityEntry.getValue()) {
					consumeBehaviour(priority, activity, behaviour, null, consumer);
				}
			}
		}
	}

	private static <E extends LivingEntity> void consumeBehaviour(int priority, Activity activity, Behavior<E> behaviour, @Nullable Behavior<E> parentBehaviour, BrainBehaviourConsumer consumer) {
		consumer.consume(priority, activity, behaviour, parentBehaviour);

		if (behaviour instanceof GateBehavior<E> groupBehaviour) {
			groupBehaviour.behaviors.stream().forEach(childBehaviour -> consumeBehaviour(priority, activity, (Behavior)childBehaviour, groupBehaviour, consumer));
		}
		else if (behaviour instanceof GroupBehaviour<E> groupBehaviour) {
			groupBehaviour.getBehaviours().forEachRemaining(childBehaviour -> consumeBehaviour(priority, activity, (Behavior)childBehaviour, groupBehaviour, consumer));
		}
	}

	/**
	 * Removes any behaviours matching the given predicate from the provided brain.<br>
	 * Removed behaviours are stopped prior to removal
	 * @param entity The owner of the brain
	 * @param predicate The predicate checked for each
	 */
	public static <E extends LivingEntity> void removeBehaviour(E entity, BrainBehaviourPredicate predicate) {
		if (entity.getBrain() instanceof SmartBrain smartBrain) {
			smartBrain.removeBehaviour(entity, predicate);

			return;
		}

		Set<Map.Entry<Integer, Map<Activity, Set<Behavior<E>>>>> behaviours = (Set)entity.getBrain().availableBehaviorsByPriority.entrySet();

		for (Map.Entry<Integer, Map<Activity, Set<Behavior<E>>>> priorityEntry : behaviours) {
			Integer priority = priorityEntry.getKey();

			for (Map.Entry<Activity, Set<Behavior<E>>> activityEntry : priorityEntry.getValue().entrySet()) {
				Activity activity = activityEntry.getKey();

				for (Iterator<Behavior<E>> iterator = activityEntry.getValue().iterator(); iterator.hasNext();) {
					Behavior<E> behaviour = iterator.next();

					checkBehaviour(priority, activity, behaviour, null, predicate, () -> {
						if (behaviour.getStatus() == Behavior.Status.RUNNING)
							behaviour.doStop((ServerLevel)entity.getLevel(), entity, entity.level.getGameTime());

						iterator.remove();
					});
				}
			}
		}
	}

	private static <E extends LivingEntity> void checkBehaviour(int priority, Activity activity, Behavior<E> behaviour, @Nullable Behavior<E> parentBehaviour, BrainBehaviourPredicate predicate, Runnable callback) {
		if (predicate.isBehaviour(priority, activity, behaviour, parentBehaviour)) {
			callback.run();
		}
		else if (behaviour instanceof GateBehavior groupBehaviour) {
			for (Iterator<Behavior<E>> childBehaviourIterator = groupBehaviour.behaviors.stream().iterator(); childBehaviourIterator.hasNext();) {
				checkBehaviour(priority, activity, childBehaviourIterator.next(), groupBehaviour, predicate, childBehaviourIterator::remove);
			}

			if (!groupBehaviour.behaviors.stream().iterator().hasNext())
				callback.run();
		}
		else if (behaviour instanceof GroupBehaviour groupBehaviour) {
			for (Iterator<Behavior<E>> childBehaviourIterator = groupBehaviour.getBehaviours(); childBehaviourIterator.hasNext();) {
				checkBehaviour(priority, activity, childBehaviourIterator.next(), groupBehaviour, predicate, childBehaviourIterator::remove);
			}

			if (!groupBehaviour.getBehaviours().hasNext())
				callback.run();
		}
	}

	/**
	 * Safely a new {@link Behavior Behaviour} to the given {@link Brain}
	 * @param brain The brain to add the behaviour to
	 * @param priority The priority index the behaviour belongs to (lower runs earlier)
	 * @param activity The activity category the behaviour belongs to
	 * @param behaviourControl The behaviour to add
	 */
	public static void addBehaviour(Brain<?> brain, int priority, Activity activity, Behavior behaviourControl) {
		if (brain instanceof SmartBrain smartBrain) {
			smartBrain.addBehaviour(priority, activity, behaviourControl);

			return;
		}

		brain.availableBehaviorsByPriority.computeIfAbsent(priority, priority2 -> Maps.newHashMap()).computeIfAbsent(activity, activity2 -> Sets.newLinkedHashSet()).add(behaviourControl);

//		if (behaviourControl instanceof Behavior<?> behavior) {
//			for (MemoryModuleType<?> memoryType : behavior.entryCondition.keySet()) {
//				brain.memories.putIfAbsent(memoryType, Optional.empty());
//			}
//		}
	}

	/**
	 * Adds a full {@link BrainActivityGroup} to the brain, inclusive of activities and conditions
	 */
	public static void addActivity(Brain<?> brain, BrainActivityGroup<?> behaviourGroup) {
		if (brain instanceof SmartBrain smartBrain) {
			smartBrain.addActivity(behaviourGroup);

			return;
		}

		brain.addActivityAndRemoveMemoriesWhenStopped(behaviourGroup.getActivity(), (ImmutableList)behaviourGroup.pairBehaviourPriorities(), behaviourGroup.getActivityStartMemoryConditions(), behaviourGroup.getWipedMemoriesOnFinish());
	}

	/**
	 * Adds a sensor to the given brain, additionally allowing for custom instantiation.<br>
	 * Automatically adds detected memories to the brain, but because of the nature of the vanilla brain system,
	 * you may need to {@link BrainUtils#addMemories add additional memories manually} if Mojang didn't set something up properly
	 */
	public static <S extends Sensor<?>> void addSensor(Brain<?> brain, SensorType<S> sensorType, S sensor) {
		if (brain instanceof SmartBrain smartBrain) {
			if (!(sensor instanceof ExtendedSensor extendedSensor))
				throw new IllegalArgumentException("Attempted to provide sensor to SmartBrain, only ExtendedSensor subclasses acceptable. Sensor: " + sensor.getClass());

			smartBrain.addSensor(extendedSensor);

			return;
		}

		brain.sensors.put((SensorType)sensorType, (Sensor)sensor);
		addMemories(brain, sensor.requires().toArray(new MemoryModuleType[0]));
	}

	/**
	 * Adds the given {@link MemoryModuleType} to the provided brain.<br>
	 * Generally only required if modifying vanilla brains and additional memories are needed.
	 */
	public static void addMemories(Brain<?> brain, MemoryModuleType<?>... memories) {
		if (brain instanceof SmartBrain smartBrain) {
			for (MemoryModuleType<?> memoryType : memories) {
				smartBrain.getMemory(memoryType);
			}

			return;
		}

		for (MemoryModuleType<?> memoryType : memories) {
			brain.memories.computeIfAbsent(memoryType, key -> Optional.empty()).map(ExpirableValue::getValue);
		}
	}

	/**
	 * Adds the given scheduled activity transition to the provided brain's {@link net.minecraft.world.entity.schedule.Schedule schedule}, creating a new schedule if required.
	 * @param brain The brain the schedule belongs to
	 * @param activity The activity to transition to
	 * @param tickTime The tick-time the activity transition should happen
	 * @param tickType The type of tick tracking the schedule should use, if a new schedule has to be created.
	 */
	public static void addScheduledActivityTransition(Brain<?> brain, Activity activity, int tickTime, SmartBrainSchedule.Type tickType) {
		if (brain instanceof SmartBrain smartBrain) {
			SmartBrainSchedule schedule;

			if ((schedule = smartBrain.getSchedule()) == null)
				smartBrain.setSchedule((schedule = new SmartBrainSchedule(tickType)));

			schedule.activityAt(tickTime, activity);
		}
		else {
			Schedule schedule;

			if ((schedule = brain.getSchedule()) == Schedule.EMPTY)
				brain.setSchedule(new Schedule());

			Timeline timeline = schedule.timelines.computeIfAbsent(activity, key -> new Timeline());

			timeline.addKeyframe(tickTime, 1);

			for (Map.Entry<Activity, Timeline> entry : schedule.timelines.entrySet()) {
				if (entry.getKey() != activity)
					entry.getValue().addKeyframe(tickTime, 0);
			}
		}
	}
}
