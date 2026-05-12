package net.tslat.smartbrainlib.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.core.ActivityBuilder;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import net.tslat.smartbrainlib.library.interfaces.BrainBehaviourPredicate;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/// Utility class for various brain functions
///
/// Because vanilla's brains rely on explicit registration and declaration of memory types to operate,
/// its existing methods assume the memory will exist if queried.<br/>
/// `SmartBrainLib` allows for dynamic memory assignment, and so it is strongly recommended that brain
/// and/or memory interactions are performed through this class for safety and consistency
public final class BrainUtil {
	/// Get the [Brain] for an [Entity], safely casting it to the entity's type, for convenience
	@SuppressWarnings("unchecked")
    public static <T extends LivingEntity> Brain<T> getBrain(T entity) {
		return (Brain<T>)entity.getBrain();
	}

	/// Get a memory value from an [Entity], or fall back to a provided value if the memory isn't present
	///
	/// @param entity The entity to retrieve the memory for
	/// @param memory The memory to retrieve the value for
	/// @param fallback A supplier for the fallback value to return if the memory isn't present
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	public static <T> @Nullable T memoryOrDefault(LivingEntity entity, MemoryModuleType<T> memory, Supplier<@Nullable T> fallback) {
		return memoryOrDefault(entity.getBrain(), memory, fallback);
	}

	/// Get a memory value from an [Entity] with a fallback value if no memory is present
	///
	/// @param entity The entity to retrieve the memory for
	/// @param memory The memory to retrieve the value for
	/// @param fallback The fallback value to return if the memory isn't present
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	@Contract("_,_,!null->!null")
	public static <T> @Nullable T memoryOrDefault(LivingEntity entity, MemoryModuleType<T> memory, @Nullable T fallback) {
		return memoryOrDefault(entity.getBrain(), memory, fallback);
	}

	/// Get a memory value from a [Brain], or fall back to a provided value if the memory isn't present
	///
	/// @param brain The brain to retrieve the memory from
	/// @param memory The memory to retrieve the value for
	/// @param fallback A supplier for the fallback value to return if the memory isn't present
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	public static <T> @Nullable T memoryOrDefault(Brain<?> brain, MemoryModuleType<T> memory, Supplier<@Nullable T> fallback) {
		return brain.getMemory(memory).orElseGet(fallback);
	}

	/// Get a memory value from a [Brain], or fall back to a provided value if the memory isn't present
	///
	/// @param brain The brain to retrieve the memory from
	/// @param memory The memory to retrieve the value for
	/// @param fallback The fallback value to return if the memory isn't present
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	@Contract("_,_,!null->!null")
	public static <T> @Nullable T memoryOrDefault(Brain<?> brain, MemoryModuleType<T> memory, @Nullable T fallback) {
		return brain.getMemory(memory).orElse(fallback);
	}

	/// Get a memory value from an [Entity] or insert a new value if not already present
	///
	/// @param entity The entity to retrieve the memory for
	/// @param memory The memory to retrieve the value for
	/// @param fallback A supplier for the fallback value to set and return if the memory isn't present
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	public static <T> @Nullable T computeMemoryIfAbsent(LivingEntity entity, MemoryModuleType<T> memory, Supplier<@Nullable T> fallback) {
		return computeMemoryIfAbsent(entity.getBrain(), memory, fallback);
	}

	/// Get a memory value from a [Brain] or insert a new value if not already present
	///
	/// @param brain The brain to retrieve the memory from
	/// @param memory The memory to retrieve the value for
	/// @param fallback The fallback value to set and return if the memory isn't present
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	public static <T> @Nullable T computeMemoryIfAbsent(Brain<?> brain, MemoryModuleType<T> memory, Supplier<@Nullable T> fallback) {
		return brain.getMemory(memory).orElseGet(() -> {
			final T newMemory = fallback.get();

			if (newMemory != null)
				brain.setMemory(memory, newMemory);

			return newMemory;
		});
	}

	/// Get a memory value from an [Entity] or insert a new value if not already present
	///
	/// @param entity The entity to retrieve the memory for
	/// @param memory The memory to retrieve the value for
	/// @param fallback The fallback value to set and return if the memory isn't present
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	@Contract("_,_,null->null;_,_,!null->!null")
	public static <T> @Nullable T computeMemoryIfAbsent(LivingEntity entity, MemoryModuleType<T> memory, @Nullable T fallback) {
		return computeMemoryIfAbsent(entity.getBrain(), memory, fallback);
	}

	/// Get a memory value from a [Brain] or insert a new value if not already present
	///
	/// @param brain The brain to retrieve the memory from
	/// @param memory The memory to retrieve the value for
	/// @param fallback The fallback value to set and return if the memory isn't present. If the fallback value is null, the memory is not set
	/// @return The stored memory, or fallback value if no memory was stored
	/// @param <T> The value type of the memory
	@Contract("_,_,null->null;_,_,!null->!null")
	public static <T> @Nullable T computeMemoryIfAbsent(Brain<?> brain, MemoryModuleType<T> memory, @Nullable T fallback) {
		return brain.getMemory(memory).orElseGet(() -> {
			if (fallback != null)
				brain.setMemory(memory, fallback);

			return fallback;
		});
	}

	/// Get a memory value from an [Entity] or null if no memory is present
	///
	/// @param entity The entity to retrieve the memory for
	/// @param memory The memory to retrieve the value for
	/// @return The stored memory, or null if no memory was stored
	/// @param <T> The value type of the memory
	public static <T> @Nullable T getMemory(LivingEntity entity, MemoryModuleType<T> memory) {
		return getMemory(entity.getBrain(), memory);
	}

	/// Get a memory value from a [Brain] or null if no memory is present
	///
	/// @param brain The brain to retrieve the memory from
	/// @param memory The memory to retrieve the value for
	/// @return The stored memory, or null if no memory was stored
	/// @param <T> The value type of the memory
	public static <T> @Nullable T getMemory(Brain<?> brain, MemoryModuleType<T> memory) {
        return memoryOrDefault(brain, memory, (T)null);
	}

	/// Perform an operation using an [Entity]'s memory value, if present
	///
	/// @param entity The entity to retrieve the memory for
	/// @param memory The memory to retrieve the value for
	/// @param consumer The operation to run if the memory is present
	/// @param <T> The value type of the memory
	public static <T> void withMemory(LivingEntity entity, MemoryModuleType<T> memory, Consumer<T> consumer) {
		withMemory(entity.getBrain(), memory, consumer);
	}

	/// Perform an operation using a [Brain]'s memory value, if present
	///
	/// @param brain The brain to retrieve the memory from
	/// @param memory The memory to retrieve the value for
	/// @param consumer The operation to run if the memory is present
	/// @param <T> The value type of the memory
	public static <T> void withMemory(Brain<?> brain, MemoryModuleType<T> memory, Consumer<T> consumer) {
		brain.getMemory(memory).ifPresent(consumer);
	}

	/// Check whether an [Entity] has a memory value set
	///
	/// @param entity The entity to check the memory of
	/// @param memory The memory to check
	/// @return True if the memory value is present, or false if the memory value is absent or unregistered
	public static boolean hasMemory(LivingEntity entity, MemoryModuleType<?> memory) {
		return hasMemory(entity.getBrain(), memory);
	}

	/// Check whether a [Brain] has a memory value set
	///
	/// @param brain The brain to check the memory of
	/// @param memory The memory to check
	/// @return True if the memory value is present, or false if the memory value is absent or unregistered
	public static boolean hasMemory(Brain<?> brain, MemoryModuleType<?> memory) {
		return brain.hasMemoryValue(memory);
	}

	/// Gets the ticks remaining until a memory expires
	///
	/// @param entity The entity to check the memory of
	/// @param memory The memory to check the expiry of
	/// @return The ticks until the memory expires, 0 if the memory doesn't exist or [Long#MAX_VALUE] if present but with no expiration
	public static long getTimeUntilMemoryExpires(LivingEntity entity, MemoryModuleType<?> memory) {
		return getTimeUntilMemoryExpires(entity.getBrain(), memory);
	}

	/// Gets the ticks remaining until a memory expires
	///
	/// @param brain The brain to check the memory of
	/// @param memory The memory to check the expiry of
	/// @return The ticks until the memory expires, 0 if the memory doesn't exist or [Long#MAX_VALUE] if present but with no expiration
	public static long getTimeUntilMemoryExpires(Brain<?> brain, MemoryModuleType<?> memory) {
		return brain.getTimeUntilExpiry(memory);
	}

	/// Set an [Entity]'s memory value for the given memory type
	///
	/// Use [BrainUtil#clearMemory(LivingEntity, MemoryModuleType)] if intending to set a memory to nothing
	///
	/// @param entity The entity to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set
	/// @param <T> The value type of the memory
	public static <T> void setMemory(LivingEntity entity, MemoryModuleType<T> memoryType, T memory) {
		setMemory(entity.getBrain(), memoryType, memory);
	}

	/// Set a [Brain]'s memory value for the given memory type
	///
	/// Use [BrainUtil#clearMemory(LivingEntity, MemoryModuleType)] if intending to set a memory to nothing
	///
	/// @param brain The brain to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set
	/// @param <T> The value type of the memory
	public static <T> void setMemory(Brain<?> brain, MemoryModuleType<T> memoryType, T memory) {
		brain.setMemory(memoryType, memory);
	}

	/// Set an [Entity]'s memory value for the given memory type, or clear it if the passed value is null
	///
	/// @param entity The entity to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set
	/// @param <T> The value type of the memory
	public static <T> void setOrClearMemory(LivingEntity entity, MemoryModuleType<T> memoryType, @Nullable T memory) {
		if (memory == null) {
			clearMemory(entity.getBrain(), memoryType);
		}
		else {
			setMemory(entity.getBrain(), memoryType, memory);
		}
	}

	/// Set a [Brain]'s memory value for the given memory type, or clear it if the passed value is null
	///
	/// @param brain The brain to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set
	/// @param <T> The value type of the memory
	public static <T> void setOrClearMemory(Brain<?> brain, MemoryModuleType<T> memoryType, @Nullable T memory) {
		if (memory == null) {
			clearMemory(brain, memoryType);
		}
		else {
			setMemory(brain, memoryType, memory);
		}
	}

	/// Set or clear a [Brain]'s memory value for the given memory type
	///
	/// @param entity The entity to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set or empty if clearing the memory
	/// @param <T> The value type of the memory
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> void setOrClearMemory(LivingEntity entity, MemoryModuleType<T> memoryType, Optional<T> memory) {
		setOrClearMemory(entity.getBrain(), memoryType, memory);
	}

	/// Set or clear a [Brain]'s memory value for the given memory type
	///
	/// @param brain The brain to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set or empty if clearing the memory
	/// @param <T> The value type of the memory
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> void setOrClearMemory(Brain<?> brain, MemoryModuleType<T> memoryType, Optional<T> memory) {
		setOrClearMemory(brain, memoryType, memory.orElse(null));
	}

	/// Set an [Entity]'s memory value for the given memory type, with the memory expiring after a certain time
	///
	/// Use [BrainUtil#clearMemory(LivingEntity, MemoryModuleType)] if intending to set a memory to nothing
	///
	/// @param entity The entity to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set
	/// @param expirationTicks The length of time (in ticks) that this memory should last for before expiring
	/// @param <T> The value type of the memory
	public static <T> void setForgettableMemory(LivingEntity entity, MemoryModuleType<T> memoryType, T memory, int expirationTicks) {
		setForgettableMemory(entity.getBrain(), memoryType, memory, expirationTicks);
	}

	/// Set a [Brain]'s memory value for the given memory type, with the memory expiring after a certain time
	///
	/// Use [BrainUtil#clearMemory(LivingEntity, MemoryModuleType)] if intending to set a memory to nothing
	///
	/// @param brain The brain to set the memory of
	/// @param memoryType The memory to set the value for
	/// @param memory The memory value to set
	/// @param expirationTicks The length of time (in ticks) that this memory should last for before expiring
	/// @param <T> The value type of the memory
	public static <T> void setForgettableMemory(Brain<?> brain, MemoryModuleType<T> memoryType, T memory, int expirationTicks) {
		brain.setMemoryWithExpiry(memoryType, memory, expirationTicks);
	}

	/// Wipe an [Entity]'s memory value for the given memory type.<br/>
	/// This safely unsets a memory, returning it to empty
	///
	/// @param entity The entity to clear the memory of
	/// @param memory The memory type to erase the value for
	public static void clearMemory(LivingEntity entity, MemoryModuleType<?> memory) {
		clearMemory(entity.getBrain(), memory);
	}

	/// Wipe a [Brain]'s memory value for the given memory type.<br/>
	/// This safely unsets a memory, returning it to empty
	///
	/// @param brain The brain to clear the memory of
	/// @param memory The memory type to erase the value for
	public static void clearMemory(Brain<?> brain, MemoryModuleType<?> memory) {
		brain.eraseMemory(memory);
	}

	/// Wipe an [Entity]'s memory value for the given memory type.<br/>
	/// This safely unsets a memory, returning it to empty
	///
	/// @param entity The entity to clear the memories of
	/// @param memories The memory types to erase the value for
	public static void clearMemories(LivingEntity entity, MemoryModuleType<?>... memories) {
		clearMemories(entity.getBrain(), memories);
	}

	/// Wipe a [Brain]'s memory value for the given memory type.<br/>
	/// This safely unsets a memory, returning it to empty
	///
	/// @param brain The brain to clear the memories of
	/// @param memories The memory types to erase the value for
	public static void clearMemories(Brain<?> brain, MemoryModuleType<?>... memories) {
		for (MemoryModuleType<?> memory : memories) {
			brain.eraseMemory(memory);
		}
	}

	/// Gets the current primary attack target of an entity, if present.<br/>
	/// Will additionally attempt to retrieve the non-[Brain] target if the entity is a [Mob]
	///
	/// __**Note:**__ Will not account for additional target memories (E.G. [MemoryModuleType#RAM_TARGET])
	///
	/// @param entity The entity to retrieve the target of
	/// @return The current attack target of the entity, or null if none present
	public static @Nullable LivingEntity getTargetOfEntity(LivingEntity entity) {
		return getTargetOfEntity(entity, entity instanceof Mob mob ? mob.getTarget() : null);
	}

	/// Gets the current attack target of an entity, if present, or an optional fallback entity if none present
	///
	/// Will additionally attempt to retrieve the non-[Brain] target if the entity is a [Mob]
	///
	/// @param entity The entity to retrieve the target of
	/// @param fallback An optional fallback entity to return if no attack target is present
	/// @return The current attack target of the entity, the fallback entity if provided, or null otherwise
	@Contract("_,!null->!null")
	public static @Nullable LivingEntity getTargetOfEntity(LivingEntity entity, @Nullable LivingEntity fallback) {
		return getTargetOfEntity(entity, () -> fallback);
	}

	/// Gets the current attack target of an entity, if present, or an optional fallback entity if none present
	///
	/// Will additionally attempt to retrieve the non-[Brain] target if the entity is a [Mob]
	///
	/// @param entity The entity to retrieve the target of
	/// @param fallback An optional fallback entity to return if no attack target is present
	/// @return The current attack target of the entity, the fallback entity if provided, or null otherwise
	public static @Nullable LivingEntity getTargetOfEntity(LivingEntity entity, Supplier<@Nullable LivingEntity> fallback) {
		return memoryOrDefault(entity.getBrain(), MemoryModuleType.ATTACK_TARGET, (Supplier<LivingEntity>)() -> {
			if (entity instanceof Mob mob && mob.getTarget() != null)
				return mob.getTarget();

			return fallback.get();
		});
	}

	/// Gets the last tracked [LivingEntity] to attack the given entity, if present.<br/>
	/// Requires that the entity uses the [MemoryModuleType#HURT_BY_ENTITY] memory type, and a [Sensor] that sets it
	///
	/// @param entity The entity to retrieve the attacker for
	/// @return The last entity to attack the given entity, or null if none present
	public static @Nullable LivingEntity getLastAttacker(LivingEntity entity) {
		return memoryOrDefault(entity, MemoryModuleType.HURT_BY_ENTITY, (LivingEntity)null);
	}

	/// Sets the attack target of the given [LivingEntity] and safely sets the non-brain attack target for compatibility purposes.<br/>
	/// Provided target can be null to effectively remove an entity's attack target
	///
	/// @param entity The entity to set the target of
	/// @param target The target entity to set, or null to clear the current target
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
	
	/// Replacement of [BehaviorUtils#canSee], falling back to a raytrace check in the event the target entity isn't in the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory
	///
	/// @param entity The entity to check line of sight of
	/// @param target The entity to check visibility of
	/// @return Whether the target entity is known to be visible or not
	public static boolean canSee(LivingEntity entity, LivingEntity target) {
		if (BehaviorUtils.entityIsVisible(entity.getBrain(), target))
			return true;

		return SensoryUtil.hasLineOfSight(entity, target);
	}

	/// Sets a [SBLMemoryTypes#SPECIAL_ATTACK_COOLDOWN] value for a certain length of time.<br/>
	/// This can then be checked via [BrainUtil#isOnSpecialCooldown(LivingEntity)] as needed
	///
	/// @param entity The entity to set the cooldown for
	/// @param ticks The length of time (in ticks) the cooldown should apply for
	public static void setSpecialCooldown(LivingEntity entity, int ticks) {
		setSpecialCooldown(entity.getBrain(), ticks);
	}

	/// Sets a [SBLMemoryTypes#SPECIAL_ATTACK_COOLDOWN] value for a certain length of time.<br/>
	/// This can then be checked via [BrainUtil#isOnSpecialCooldown(Brain)] as needed
	///
	/// @param brain The brain to set the cooldown on
	/// @param ticks The length of time (in ticks) the cooldown should apply for
	public static void setSpecialCooldown(Brain<?> brain, int ticks) {
		setForgettableMemory(brain, SBLMemoryTypes.SPECIAL_ATTACK_COOLDOWN.get(), true, ticks);
	}

	/// Checks whether the entity has had a [SBLMemoryTypes#SPECIAL_ATTACK_COOLDOWN] set, and it hasn't expired.<br/>
	/// This can be used for cross-behaviour cooldowns and interactions
	///
	/// @param entity The entity to check the cooldown for
	/// @return Whether the entity has a cooldown currently active
	public static boolean isOnSpecialCooldown(LivingEntity entity) {
		return isOnSpecialCooldown(entity.getBrain());
	}

	/// Checks whether the entity has had a [SBLMemoryTypes#SPECIAL_ATTACK_COOLDOWN] set, and it hasn't expired.<br/>
	/// This can be used for cross-behaviour cooldowns and interactions
	///
	/// @param brain The brain to check the cooldown for
	/// @return Whether the entity has a cooldown currently active
	public static boolean isOnSpecialCooldown(Brain<?> brain) {
		return hasMemory(brain, SBLMemoryTypes.SPECIAL_ATTACK_COOLDOWN.get());
	}

	/// Returns a **<u>read-only</u>** stream of all [Behaviours][BehaviorControl] registered to this brain
	@SuppressWarnings({"rawtypes", "unchecked"})
    public static <E extends LivingEntity> Stream<BehaviorControl<? super E>> getAllBehaviours(Brain<E> brain) {
		return brain.availableBehaviorsByPriority.values().stream()
				.mapMulti((map, consumer) -> map.values().forEach((Consumer)consumer));
	}

	/// Removes the first behaviour matching the given [BrainBehaviourPredicate] from the provided [Brain].<br/>
	/// Removed behaviours are stopped prior to removal
	///
	/// @param entity The owner of the brain
	/// @param activity The activity category of the behaviour to remove
	/// @param predicate The predicate checked for each
	/// @return true if a behaviour was removed
	@SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends LivingEntity> boolean removeBehaviour(T entity, Activity activity, BrainBehaviourPredicate<T> predicate) {
		if (!(entity.level() instanceof ServerLevel))
			return false;

		if (entity.getBrain() instanceof SmartBrain smartBrain)
            return smartBrain.getBehaviours().removeBehaviour(activity, entity, predicate);

		for (Map.Entry<Integer, Map<Activity, Set<BehaviorControl<? super T>>>> byPriority : ((Brain<T>)entity.getBrain()).availableBehaviorsByPriority.entrySet()) {
			final int priority = byPriority.getKey();
			final Set<BehaviorControl<? super T>> behaviours = byPriority.getValue().getOrDefault(activity, Set.of());

			if (!behaviours.isEmpty() && removeBehaviourRecursively(entity, activity, priority, behaviours, new Stack<>(), predicate))
				return true;
		}

		return false;
	}

	/// Recursively search for a matching [BehaviorControl] to remove from the provided collection of behaviours
	private static <T extends LivingEntity> boolean removeBehaviourRecursively(T entity, Activity activity, int priority, Iterable<? extends BehaviorControl<? super T>> behaviours, Stack<BehaviorControl<? super T>> parentBehaviours, BrainBehaviourPredicate<T> predicate) {
		for (Iterator<? extends BehaviorControl<? super T>> iterator = behaviours.iterator(); iterator.hasNext();) {
			final BehaviorControl<? super T> behaviour = iterator.next();

			if (predicate.isBehaviour(behaviour, activity, priority, parentBehaviours)) {
				if (behaviour.getStatus() == Behavior.Status.RUNNING)
					behaviour.doStop((ServerLevel)entity.level(), entity, entity.level().getGameTime());

				iterator.remove();

				return true;
			}
			else {
				parentBehaviours.push(behaviour);

				if (removeBehaviourRecursively(entity, activity, priority, BehaviourUtil.getChildrenOfBehaviour(behaviour), parentBehaviours, predicate)) {
					parentBehaviours.pop();

					return true;
				}

				parentBehaviours.pop();
			}
		}

		return false;
	}

	/// Safely a new [Behaviour][BehaviorControl] to the given [Brain]
	/// @param brain The brain to add the behaviour to
	/// @param activity The activity category the behaviour belongs to
	/// @param priority The priority index the behaviour belongs to (lower runs earlier)
	/// @param behaviourControl The behaviour to add
	@SuppressWarnings({"rawtypes", "unchecked"})
    public static <E extends LivingEntity> void addBehaviour(Brain<E> brain, Activity activity, int priority, BehaviorControl<? super E> behaviourControl) {
		if (brain instanceof SmartBrain<?> smartBrain) {
			((SmartBrain)smartBrain).getBehaviours().addBehaviour(activity, priority, behaviourControl, true);

			return;
		}

		brain.availableBehaviorsByPriority.computeIfAbsent(priority, _ -> Maps.newHashMap()).computeIfAbsent(activity, _ -> Sets.newLinkedHashSet()).add(behaviourControl);

		if (behaviourControl instanceof Behavior<?> behavior) {
			for (MemoryModuleType<?> memoryType : behavior.entryCondition.keySet()) {
				brain.registerMemory(memoryType);
			}
		}
	}

	/// Adds a full [ActivityBuilder] to the brain, inclusive of activities and conditions
	@SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends LivingEntity> void addActivity(Brain<E> brain, ActivityBuilder<?> activityBuilder) {
		if (brain instanceof SmartBrain<?> smartBrain) {
			smartBrain.getBehaviours().addActivity((ActivityBuilder)activityBuilder, true);

			return;
		}

		brain.addActivity(activityBuilder.getActivity(),
						  (ImmutableList)ImmutableList.copyOf(activityBuilder.getPriorityBehaviourPairs().stream().map(pair -> Pair.of(pair.leftInt(), pair.right())).toList()),
						  activityBuilder.getStartConditions(),
						  activityBuilder.getClearedMemoriesOnFinish());
	}

	/// Adds a sensor to the given brain, additionally allowing for custom instantiation
	///
	/// Automatically adds detected memories to the brain, but because of the nature of the vanilla brain system,
	/// you may need to [add additional memories manually][BrainUtil#addMemories] if Mojang didn't set something up properly
	///
	/// @param brain The brain to add the sensor to
	/// @param sensorType The registered [SensorType] of the sensor to add
	/// @param sensor The sensor instance to add
	/// @param random A [RandomSource] instance to use to intialize the sensor
	public static <E extends LivingEntity, S extends Sensor<E>> void addSensor(Brain<E> brain, SensorType<S> sensorType, S sensor, RandomSource random) {
		if (brain instanceof SmartBrain<?> smartBrain) {
			if (!(sensor instanceof ExtendedSensor<?> extendedSensor))
				throw new IllegalArgumentException("Attempted to provide sensor to SmartBrain, only ExtendedSensor subclasses acceptable. Sensor: " + sensor.getClass());

            //noinspection rawtypes,unchecked
            smartBrain.getSensors().addSensor((ExtendedSensor)extendedSensor, random, true);

			return;
		}

		brain.sensors.put(sensorType, sensor);
		sensor.randomlyDelayStart(random);
		addMemories(brain, sensor.requires().toArray(new MemoryModuleType[0]));
	}

	/// Adds the given [MemoryModuleType] to the provided brain
	///
	/// Generally only required if modifying vanilla brains and additional memories are needed
	public static void addMemories(Brain<?> brain, MemoryModuleType<?>... memories) {
		for (MemoryModuleType<?> memoryType : memories) {
			brain.registerMemory(memoryType);
		}
	}
}
