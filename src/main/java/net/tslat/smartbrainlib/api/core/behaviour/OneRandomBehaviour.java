package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

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
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		extendedBehaviours.shuffle();

		for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				return behaviour;
		}

		return null;
	}
}