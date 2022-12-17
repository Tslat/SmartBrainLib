package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Group behaviour that runs all child behaviours in order, one after another.<br>
 * Restarts from the first behaviour upon reaching the end of the list
 * @param <E> The entity
 */
public final class SequentialBehaviour<E extends LivingEntity> extends GroupBehaviour<E> {
	protected Predicate<ExtendedBehaviour<? super E>> earlyResetPredicate = behaviour -> false;
	protected ExtendedBehaviour<? super E> lastRun = null;

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

	@Nullable
	@Override
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		boolean pickNext = this.lastRun == null;

		if (this.lastRun != null && this.earlyResetPredicate.test(this.lastRun)) {
			pickNext = true;
			this.lastRun = null;
		}

		for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
			if (pickNext) {
				if (behaviour.tryStart(level, entity, gameTime)) {
					this.lastRun = behaviour;

					return behaviour;
				}

				return null;
			}

			if (behaviour == this.lastRun)
				pickNext = true;
		}

		this.lastRun = null;

		return null;
	}
}
