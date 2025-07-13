package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Group behaviour that attempts to run all sub-behaviours in order, until the first successful one.
 * @param <E> The entity
 */
public final class FirstApplicableBehaviour<E extends LivingEntity> extends GroupBehaviour<E> {
	@SafeVarargs
	public FirstApplicableBehaviour(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
		super(behaviours);
	}

	@SafeVarargs
	public FirstApplicableBehaviour(ExtendedBehaviour<? super E>... behaviours) {
		super(behaviours);
	}

	public FirstApplicableBehaviour(List<Pair<ExtendedBehaviour<? super E>, Integer>> behaviours) {
		super(behaviours);
	}

	@Nullable
	@Override
	protected ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
		for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				return behaviour;
		}

		return null;
	}
}
