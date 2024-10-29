package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

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

	@Nullable
	@Override
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		ExtendedBehaviour<? super E> lastSuccessfulBehaviour = null;

		for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				lastSuccessfulBehaviour = behaviour;
		}

		return lastSuccessfulBehaviour;
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
		this.cooldownFinishedAt = gameTime + this.cooldownProvider.applyAsInt(entity);

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

	@Override
	public String toString() {
		final Set<? extends BehaviorControl<? super E>> activeBehaviours = this.behaviours.stream()
				.filter(behaviorControl -> behaviorControl.getStatus() == Status.RUNNING)
				.collect(Collectors.toSet());

		return "(" + getClass().getSimpleName() + "): " + activeBehaviours;
	}
}
