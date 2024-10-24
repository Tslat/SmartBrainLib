package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

/**
 * Calls a callback when the entity has been obstructed for a given period of time.
 * @param <E> The entity
 */
public class ReactToUnreachableTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemories(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, SBLMemoryTypes.TARGET_UNREACHABLE.get());

	protected ToIntFunction<E> ticksToReact = entity -> 100;
	protected BiConsumer<E, Boolean> callback = (entity, towering) -> {};

	protected long reactAtTime = 0;

	/**
	 * Set the amount of ticks that the target should be unreachable before reacting.
	 * @param ticksToReact The function to provide the time to wait before reacting
	 * @return this
	 */
	public ReactToUnreachableTarget<E> timeBeforeReacting(ToIntFunction<E> ticksToReact) {
		this.ticksToReact = ticksToReact;

		return this;
	}

	/**
	 * Set the function to run when the given time has elapsed and the target is still unreachable.
	 * @param callback The function to run
	 * @return this
	 */
	public ReactToUnreachableTarget<E> reaction(BiConsumer<E, Boolean> callback) {
		this.callback = callback;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return this.reactAtTime == 0 || this.reactAtTime < gameTime;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return hasRequiredMemories(entity);
	}

	@Override
	protected void start(E entity) {
		this.reactAtTime = entity.level().getGameTime() + this.ticksToReact.applyAsInt(entity);
	}

	@Override
	protected void stop(E entity) {
		this.reactAtTime = 0;
	}

	@Override
	protected void tick(E entity) {
		if (entity.level().getGameTime() == this.reactAtTime)
			this.callback.accept(entity, BrainUtil.getMemory(entity, SBLMemoryTypes.TARGET_UNREACHABLE.get()));
	}
}
