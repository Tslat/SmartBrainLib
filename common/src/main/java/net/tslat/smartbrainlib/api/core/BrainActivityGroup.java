package net.tslat.smartbrainlib.api.core;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;

import java.util.List;
import java.util.Set;

public class BrainActivityGroup<T extends LivingEntity & SmartBrainOwner<T>> {
	private final Activity activity;
	private int priorityStart = 0;
	private final List<Behavior<? super T>> behaviours = new ObjectArrayList<>();
	private final Set<Pair<MemoryModuleType<?>, MemoryStatus>> activityStartMemoryConditions = new ObjectOpenHashSet<>();
	private Set<MemoryModuleType<?>> wipedMemoriesOnFinish = null;

	public BrainActivityGroup(Activity activity) {
		this.activity = activity;
	}

	public BrainActivityGroup<T> priority(int priorityStart) {
		this.priorityStart = priorityStart;

		return this;
	}

	public BrainActivityGroup<T> behaviours(Behavior<? super T>... behaviours) {
		this.behaviours.addAll(new ObjectArrayList<>(behaviours));

		return this;
	}

	public BrainActivityGroup<T> onlyStartWithMemoryStatus(MemoryModuleType<?> memory, MemoryStatus status) {
		this.activityStartMemoryConditions.add(Pair.of(memory, status));

		return this;
	}

	public BrainActivityGroup<T> wipeMemoriesWhenFinished(MemoryModuleType<?>... memories) {
		if (this.wipedMemoriesOnFinish == null) {
			this.wipedMemoriesOnFinish = new ObjectOpenHashSet<>(memories);
		}
		else {
			this.wipedMemoriesOnFinish.addAll(new ObjectOpenHashSet<>(memories));
		}

		return this;
	}

	public BrainActivityGroup<T> requireAndWipeMemoriesOnUse(MemoryModuleType<?>... memories) {
		for (MemoryModuleType<?> memory : memories) {
			onlyStartWithMemoryStatus(memory, MemoryStatus.VALUE_PRESENT);
		}

		wipeMemoriesWhenFinished(memories);

		return this;
	}

	public Activity getActivity() {
		return this.activity;
	}

	public List<Behavior<? super T>> getBehaviours() {
		return this.behaviours;
	}

	public int getPriorityStart() {
		return this.priorityStart;
	}

	public Set<Pair<MemoryModuleType<?>, MemoryStatus>> getActivityStartMemoryConditions() {
		return this.activityStartMemoryConditions;
	}

	public Set<MemoryModuleType<?>> getWipedMemoriesOnFinish() {
		return this.wipedMemoriesOnFinish != null ? this.wipedMemoriesOnFinish : Set.of();
	}

	public ImmutableList<Pair<Integer, Behavior<? super T>>> pairBehaviourPriorities() {
		int priority = this.priorityStart;
		ImmutableList.Builder<Pair<Integer, Behavior<? super T>>> pairedBehaviours = ImmutableList.builder();

		for (Behavior<? super T> behaviour : this.behaviours) {
			pairedBehaviours.add(Pair.of(priority++, behaviour));
		}

		return pairedBehaviours.build();
	}

	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> empty() {
		return new BrainActivityGroup<T>(Activity.REST);
	}

	@SafeVarargs
	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> coreTasks(Behavior... behaviours) {
		return new BrainActivityGroup<T>(Activity.CORE).priority(0).behaviours(behaviours);
	}

	@SafeVarargs
	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> idleTasks(Behavior... behaviours) {
		return new BrainActivityGroup<T>(Activity.IDLE).priority(10).behaviours(behaviours);
	}

	@SafeVarargs
	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> fightTasks(Behavior... behaviours) {
		return new BrainActivityGroup<T>(Activity.FIGHT).priority(10).behaviours(behaviours).requireAndWipeMemoriesOnUse(MemoryModuleType.ATTACK_TARGET);
	}
}
