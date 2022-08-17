package net.tslat.smartbrainlib.core.behaviour;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;
import java.util.Map;

/**
 * Group behaviour that attempts to run all sub-behaviours in order, until the first successful one.
 * @param <E>
 */
@SuppressWarnings("unchecked")
public final class FirstSuccessfulBehaviour<E extends LivingEntity> extends GateBehavior<E> {
	public FirstSuccessfulBehaviour(Behavior<? super E>... taskList) {
		this(ImmutableMap.of(), taskList);
	}

	public FirstSuccessfulBehaviour(Map<MemoryModuleType<?>, MemoryStatus> memoryRequirements, Behavior<? super E>... taskList) {
		this(memoryRequirements, behavioursToWeightedList(taskList));
	}

	public FirstSuccessfulBehaviour(Map<MemoryModuleType<?>, MemoryStatus> memoryRequirements, List<Pair<Behavior<? super E>, Integer>> taskList) {
		super(memoryRequirements, ImmutableSet.of(), OrderPolicy.ORDERED, RunningPolicy.RUN_ONE, taskList);
	}

	private static <E extends LivingEntity> List<Pair<Behavior<? super E>, Integer>> behavioursToWeightedList(Behavior<? super E>... tasks) {
		List<Pair<Behavior<? super E>, Integer>> list = new ObjectArrayList<>();

		for (Behavior<? super E> behaviour : tasks) {
			list.add(Pair.of(behaviour, 1));
		}

		return list;
	}
}
