package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Group behaviour that runs all child behaviours in order, one after another.<br>
 * Restarts from the first behaviour upon reaching the end of the list or failing to continue the current behaviour
 * @param <E> The entity
 */
public final class SequentialBehaviour<E extends LivingEntity> extends GroupBehaviour<E> {
	private Predicate<ExtendedBehaviour<? super E>> earlyResetPredicate = behaviour -> false;
	private int runningIndex = 0;

	public SequentialBehaviour(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
		super(behaviours);
	}

	public SequentialBehaviour(ExtendedBehaviour<? super E>... behaviours) {
		super(behaviours);
	}

	/**
	 * Adds an early short-circuit predicate to reset back to the start of the child behaviours at any time
	 */
	public SequentialBehaviour<E> resetIf(Predicate<ExtendedBehaviour<? super E>> predicate) {
		this.earlyResetPredicate = predicate;

		return this;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return this.runningBehaviour != null && this.runningBehaviour.getStatus() != Status.STOPPED;
	}

	@Override
	protected void tick(ServerLevel level, E owner, long gameTime) {
		this.runningBehaviour.tickOrStop(level, owner, gameTime);

		if (this.runningBehaviour.getStatus() == Status.STOPPED) {
			if (pickBehaviour(level, owner, gameTime, this.behaviours) != null)
				return;

			doStop(level, owner, gameTime);
		}
	}

	@Nullable
	@Override
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		if (this.runningIndex >= extendedBehaviours.size())
			return null;

		ExtendedBehaviour<? super E> first = extendedBehaviours.get(this.runningIndex);

		if (first != null && first.tryStart(level, entity, gameTime)) {
			this.runningBehaviour = first;
			this.runningIndex++;

			return this.runningBehaviour;
		}

		return null;
	}

	@Override
	protected void stop(ServerLevel level, E entity, long gameTime) {
		super.stop(level, entity, gameTime);

		this.runningIndex = 0;
	}
}
