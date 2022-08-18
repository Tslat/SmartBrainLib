package net.tslat.smartbrainlib.core.behaviour;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTask;
import net.minecraft.entity.ai.brain.task.Task;

/**
 * Group behaviour that attempts to run all sub-behaviours in order, until the first successful one.
 * @param <E>
 */
public final class FirstSuccessfulBehaviour<E extends LivingEntity> extends MultiTask<E> {
	public FirstSuccessfulBehaviour(Task<? super E>... taskList) {
		this(ImmutableMap.of(), taskList);
	}

	public FirstSuccessfulBehaviour(Map<MemoryModuleType<?>, MemoryModuleStatus> memoryRequirements, Task<? super E>... taskList) {
		this(memoryRequirements, behavioursToWeightedList(taskList));
	}

	public FirstSuccessfulBehaviour(Map<MemoryModuleType<?>, MemoryModuleStatus> memoryRequirements, List<Pair<Task<? super E>, Integer>> taskList) {
		super(memoryRequirements, ImmutableSet.of(), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE, taskList);
	}

	private static <E extends LivingEntity> List<Pair<Task<? super E>, Integer>> behavioursToWeightedList(Task<? super E>... tasks) {
		List<Pair<Task<? super E>, Integer>> list = new ObjectArrayList<>();

		for (Task<? super E> behaviour : tasks) {
			list.add(Pair.of(behaviour, 1));
		}

		return list;
	}
}
