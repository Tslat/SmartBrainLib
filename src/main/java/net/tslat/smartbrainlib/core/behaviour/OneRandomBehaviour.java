package net.tslat.smartbrainlib.core.behaviour;

import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.Task;

/**
 * Wrapper class for {@link RunOne} for easier instantation.
 * @param <E> The entity
 */
public final class OneRandomBehaviour<E extends LivingEntity> extends FirstShuffledTask<E> {
	public OneRandomBehaviour(Task<? super E>... behaviours) {
		this(behavioursToWeightedList(behaviours));
	}

	public OneRandomBehaviour(List<Pair<Task<? super E>, Integer>> behaviours) {
		super(behaviours);
	}

	public OneRandomBehaviour(Map<MemoryModuleType<?>, MemoryModuleStatus> memoryConditions, List<Pair<Task<? super E>, Integer>> behaviours) {
		super(memoryConditions, behaviours);
	}

	private static <E extends LivingEntity> List<Pair<Task<? super E>, Integer>> behavioursToWeightedList(Task<? super E>... tasks) {
		List<Pair<Task<? super E>, Integer>> list = new ObjectArrayList<>();

		for (Task<? super E> behaviour : tasks) {
			list.add(Pair.of(behaviour, 1));
		}

		return list;
	}
}
