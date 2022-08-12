package net.tslat.smartbrainlib.api.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
		return memoryOrDefault(brain, memory, null);
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
	 * Set an entity's memory value for the given memory type. <br/>
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
	 * Set a brain's memory value for the given memory type. <br/>
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
	 * Set a brain's memory value for the given memory type, with the memory expiring after a certain time.<br/>
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
	 * Set an entity's memory value for the given memory type, with the memory expiring after a certain time.<br/>
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
	 * Gets the last entity to attack the given entity, if present. <br/>
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
	 * Sets the attack target of the given entity, and safely sets the non-brain attack target for compatibility purposes. <br/>
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
}
