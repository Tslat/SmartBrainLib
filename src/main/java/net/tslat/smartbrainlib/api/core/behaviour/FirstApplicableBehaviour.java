package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.object.SBLShufflingList;

import javax.annotation.Nullable;

/**
 * Group behaviour that attempts to run all sub-behaviours in order, until the first successful one.
 * @param <E> The entity
 */
public final class FirstApplicableBehaviour<E extends LivingEntity> extends GroupBehaviour<E> {
	public FirstApplicableBehaviour(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
		super(behaviours);
	}

	public FirstApplicableBehaviour(ExtendedBehaviour<? super E>... behaviours) {
		super(behaviours);
	}

	@Nullable
	@Override
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerWorld level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				return behaviour;
		}

		return null;
	}
}
