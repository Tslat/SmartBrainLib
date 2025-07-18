package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * Custom behaviour for conditionally invalidating/resetting existing memories.<br>
 * This allows for custom handling of stored memories, and clearing them at will.<br>
 * <br>
 * Invalidates the memory unconditionally once the behaviour runs. Use {@link InvalidateMemory#invalidateIf} and {@link ExtendedBehaviour#startCondition} to quantify its operating conditions
 * @param <E> The brain owner
 * @param <M> The data type of the memory
 */
public class InvalidateMemory<E extends LivingEntity, M> extends ExtendedBehaviour<E> {
	private final List<Pair<MemoryModuleType<?>, MemoryStatus>> memoryRequirements;

	protected final MemoryModuleType<M> memory;
	protected BiPredicate<E, M> customPredicate = (entity, target) -> true;

	public InvalidateMemory(MemoryModuleType<M> memory) {
		super();

		this.memory = memory;
		this.memoryRequirements = MemoryTest.builder(1).hasMemory(this.memory);
	}

	/**
	 * Sets a custom predicate to invalidate the memory if none of the previous checks invalidate it first.
	 */
	public InvalidateMemory<E, M> invalidateIf(BiPredicate<E, M> predicate) {
		this.customPredicate = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return this.memoryRequirements == null ? List.of() : this.memoryRequirements;
	}

	@Override
	protected void start(E entity) {
		M memory = BrainUtil.getMemory(entity, this.memory);

		if (memory != null && this.customPredicate.test(entity, memory))
			BrainUtil.clearMemory(entity, this.memory);
	}
}
