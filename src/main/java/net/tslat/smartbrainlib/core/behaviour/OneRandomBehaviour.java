package net.tslat.smartbrainlib.core.behaviour;

import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Wrapper class for {@link RunOne} for easier instantation.
 * @param <E> The entity
 */
@SuppressWarnings("unchecked")
public final class OneRandomBehaviour<E extends LivingEntity> extends RunOne<E> {
	public OneRandomBehaviour(Behavior<? super E>... behaviours) {
		this(behavioursToWeightedList(behaviours));
	}

	public OneRandomBehaviour(List<Pair<Behavior<? super E>, Integer>> behaviours) {
		super(behaviours);
	}

	public OneRandomBehaviour(Map<MemoryModuleType<?>, MemoryStatus> memoryConditions, List<Pair<Behavior<? super E>, Integer>> behaviours) {
		super(memoryConditions, behaviours);
	}

	private static <E extends LivingEntity> List<Pair<Behavior<? super E>, Integer>> behavioursToWeightedList(Behavior<? super E>... tasks) {
		List<Pair<Behavior<? super E>, Integer>> list = new ObjectArrayList<>();

		for (Behavior<? super E> behaviour : tasks) {
			list.add(Pair.of(behaviour, 1));
		}

		return list;
	}
}
