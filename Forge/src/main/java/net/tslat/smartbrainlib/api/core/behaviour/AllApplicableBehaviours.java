package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

/**
 * Group behaviour that attempts to run all sub-behaviours in order, running any that apply.<br>
 * This allows for wrapping entire groups of behaviours in overarching conditions or nesting them in other groups.<br>
 * This will count this behaviour as running if any of the child behaviours are running.
 * @param <E> The entity
 */
public final class AllApplicableBehaviours<E extends LivingEntity> extends GroupBehaviour<E> {
	public AllApplicableBehaviours(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
		super(behaviours);
	}

	public AllApplicableBehaviours(ExtendedBehaviour<? super E>... behaviours) {
		super(behaviours);
	}

	@Override
	protected boolean doStartCheck(ServerLevel level, E entity, long gameTime) {
		if (this.cooldownFinishedAt > gameTime || !hasRequiredMemories(entity) || !this.startCondition.test(entity) || !checkExtraStartConditions(level, entity))
			return false;

		return (this.runningBehaviour = pickBehaviour(level, entity, gameTime, this.behaviours)) != null;
	}

	@Nullable
	@Override
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
			behaviour.tryStart(level, entity, gameTime);
		}

		return null;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		boolean stillOperational = false;

		for (ExtendedBehaviour<? super E> behaviour : this.behaviours) {
			stillOperational |= behaviour.getStatus() == Status.RUNNING && behaviour.canStillUse((ServerLevel)entity.level(), entity, entity.level().getGameTime());
		}

		return stillOperational;
	}

	@Override
	protected boolean timedOut(long gameTime) {
		boolean timedOut = true;

		for (ExtendedBehaviour<? super E> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING && !behaviour.timedOut(gameTime))
				timedOut = false;
		}

		return timedOut;
	}

	@Override
	protected void tick(ServerLevel level, E owner, long gameTime) {
		boolean stillRunning = false;

		for (ExtendedBehaviour<? super E> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING) {
				behaviour.tickOrStop(level, owner, gameTime);

				if (behaviour.getStatus() != Status.STOPPED)
					stillRunning = true;
			}
		}

		if (!stillRunning)
			doStop(level, owner, gameTime);
	}

	@Override
	protected void stop(ServerLevel level, E entity, long gameTime) {
		this.cooldownFinishedAt = gameTime + cooldownProvider.apply(entity);

		this.taskStopCallback.accept(entity);
		stop(entity);

		for (ExtendedBehaviour<? super E> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING)
				behaviour.doStop(level, entity, gameTime);
		}
	}

	@Override
	public Status getStatus() {
		for (ExtendedBehaviour<? super E> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING)
				return Status.RUNNING;
		}

		return Status.STOPPED;
	}
}
