package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;

/**
 * Calls a callback when the entity has been obstructed for a given period of time.
 * @param <E> The entity
 */
public class ReactToUnreachableTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleStatus.VALUE_PRESENT), Pair.of(SBLMemoryTypes.TARGET_UNREACHABLE.get(), MemoryModuleStatus.VALUE_PRESENT)});

	protected Function<E, Integer> ticksToReact = entity -> 100;
	protected BiConsumer<E, Boolean> callback = (entity, towering) -> {};

	protected long reactAtTime = 0;

	/**
	 * Set the amount of ticks that the target should be unreachable before reacting.
	 * @param ticksToReact The function to provide the time to wait before reacting
	 * @return this
	 */
	public ReactToUnreachableTarget<E> timeBeforeReacting(Function<E, Integer> ticksToReact) {
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
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return this.reactAtTime == 0 || this.reactAtTime < gameTime;
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		return hasRequiredMemories(entity);
	}

	@Override
	protected void start(E entity) {
		this.reactAtTime = entity.level.getGameTime() + this.ticksToReact.apply(entity);
	}

	@Override
	protected void stop(E entity) {
		this.reactAtTime = 0;
	}

	@Override
	protected void tick(E entity) {
		if (entity.level.getGameTime() == this.reactAtTime) {
			this.callback.accept(entity, BrainUtils.getMemory(entity, SBLMemoryTypes.TARGET_UNREACHABLE.get()));
		}
	}
}
