package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.object.SBLShufflingList;

import javax.annotation.Nullable;

/**
 * Group behaviour that attempts to run sub-behaviours in a
 * @param <E> The entity
 */
public final class OneRandomBehaviour<E extends LivingEntity> extends GroupBehaviour<E> {
	public OneRandomBehaviour(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
		super(behaviours);
	}

	public OneRandomBehaviour(ExtendedBehaviour<? super E>... behaviours) {
		super(behaviours);
	}

	@Nullable
	@Override
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerWorld level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		extendedBehaviours.shuffle();

		for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				return behaviour;
		}

		return null;
	}
}