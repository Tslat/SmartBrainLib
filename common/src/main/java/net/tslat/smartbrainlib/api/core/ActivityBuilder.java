package net.tslat.smartbrainlib.api.core;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/// Container class for [SmartBrain] [Activity]-[Behavior] groups
///
/// A single `BrainActivityGroup` represents an `Activity`, its associated `behaviours`, and any additional conditions
/// related to that activity, such as [MemoryModuleType] conditions, prioritisation values, etc.
@SuppressWarnings("UnusedReturnValue")
public class ActivityBuilder<T extends LivingEntity> {
	protected final Activity activity;
	protected final List<BehaviorControl<? super T>> behaviours = new ObjectArrayList<>(10);
	protected int priorityStart = 0;
	protected @Nullable Set<Pair<MemoryModuleType<?>, MemoryStatus>> memoryConditions;
	protected @Nullable Set<MemoryModuleType<?>> wipedMemoriesOnFinish;

	protected ActivityBuilder(Activity activity) {
		this.activity = activity;
	}

	/// Create a new [ActivityBuilder] instanceof the given [Activity]
	public static <T extends LivingEntity> ActivityBuilder<T> create(Activity activity) {
		return new ActivityBuilder<>(activity);
	}

	/// Assign a base priority to all [behaviours][BehaviorControl] in this builder
	///
	/// This acts as an ordering mechanism for starting and ticking behaviours. Behaviours with a lower priority value will tick
	/// before behaviours with a higher priority value, even across different [activities][Activity]
	///
	/// All behaviours in this builder will automatically be given a sequential priority based on their order of entry
	///
	/// For example, if this builder has a priority of `10`, the first behaviour added to this builder will have a priority of `10`, the second will have a priority of `11`, and so on.<br/>
	/// Then, all behaviours registered on this [brain][SmartBrain] (regardless of `Activity`) with a priority of `10` will run before all behaviours with a priority of `11`
	public ActivityBuilder<T> behaviourPriorityBase(int priorityStart) {
		this.priorityStart = priorityStart;

		return this;
	}

	/// Add a collection of [behaviours][BehaviorControl] to this builder
	///
	/// The order of the provided behaviours is important as it determines the order that the behaviours will tick in the [brain][SmartBrain]
	@SafeVarargs
    public final ActivityBuilder<T> behaviours(BehaviorControl<? super T>... behaviours) {
		return behaviours(ObjectArrayList.wrap(behaviours));
	}

	/// Add a collection of [behaviours][BehaviorControl] to this builder
	///
	/// The order of the provided behaviours is important as it determines the order that the behaviours will tick in the [brain][SmartBrain]
	public ActivityBuilder<T> behaviours(List<BehaviorControl<? super T>> behaviours) {
		this.behaviours.addAll(behaviours);

		return this;
	}

	/// Add [memory][MemoryModuleType] conditions to this builder
	///
	/// When added to the [brain][SmartBrain], this will prevent this activity from being automatically started unless the provided memories have a value set in the brain
	public ActivityBuilder<T> addMemoryRequirements(MemoryModuleType<?>... memoryTypes) {
		if (this.memoryConditions == null)
			this.memoryConditions = new ObjectArraySet<>(memoryTypes.length);

		for (MemoryModuleType<?> memoryType : memoryTypes) {
			addMemoryRequirement(memoryType, MemoryStatus.VALUE_PRESENT);
		}

		return this;
	}

	/// Add a [memory][MemoryModuleType] condition to this builder
	///
	/// When added to the [brain][SmartBrain], this will prevent this activity from being automatically started unless the provided memory meets the [MemoryStatus] condition provided
	public ActivityBuilder<T> addMemoryRequirement(MemoryModuleType<?> memoryType, MemoryStatus status) {
		if (this.memoryConditions == null)
			this.memoryConditions = new ObjectArraySet<>();

		this.memoryConditions.add(Pair.of(memoryType, status));

		return this;
	}

	/// Add some [memory types][MemoryModuleType] to be cleared when the [Activity] this builder represents is stopped or replaced by another activity
	public ActivityBuilder<T> clearMemoriesWhenFinished(MemoryModuleType<?>... memories) {
		if (this.wipedMemoriesOnFinish == null) {
			this.wipedMemoriesOnFinish = new ObjectOpenHashSet<>(memories);
		}
		else {
			this.wipedMemoriesOnFinish.addAll(new ObjectOpenHashSet<>(memories));
		}

		return this;
	}

	/// Add a [memory][MemoryModuleType] condition to this builder and clear the memory when the [Activity] this builder represents is stopped or replaced by another activity
	///
	/// When added to the [brain][SmartBrain], this will prevent this activity from being automatically started unless the provided memory meets the [MemoryStatus] condition provided
	public ActivityBuilder<T> requireAndClearMemoriesOnUse(MemoryModuleType<?>... memories) {
		for (MemoryModuleType<?> memory : memories) {
			addMemoryRequirement(memory, MemoryStatus.VALUE_PRESENT);
		}

		clearMemoriesWhenFinished(memories);

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Internal Methods>">

	/// @return The [Activity] this builder is for
	public Activity getActivity() {
		return this.activity;
	}

	/// @return Whether this builder is empty (has no added [behaviours][BehaviorControl])
	public boolean isEmpty() {
		return this.behaviours.isEmpty();
	}

	/// @return The [behaviours][BehaviorControl] this builder contains
	public List<BehaviorControl<? super T>> getBehaviours() {
		return this.behaviours;
	}

	/// @return The base priority value for this builder, upon which all [behaviours][BehaviorControl] increment
	public int getBasePriority() {
		return this.priorityStart;
	}

	/// @return The [memory][MemoryModuleType] conditions for the [Activity] this builder represents to start
	public Set<Pair<MemoryModuleType<?>, MemoryStatus>> getStartConditions() {
		return this.memoryConditions == null ? Set.of() : this.memoryConditions;
	}

	/// @return The [memories][MemoryModuleType] that are cleared when the [Activity] this builder represents stops or is replaced
	public Set<MemoryModuleType<?>> getClearedMemoriesOnFinish() {
		return this.wipedMemoriesOnFinish != null ? this.wipedMemoriesOnFinish : Set.of();
	}

	/// @return The list of [behaviours][BehaviorControl] this builder contains, paired with its [ActivityBuilder#priorityStart] value, automatically incremented
	public List<IntObjectPair<BehaviorControl<? super T>>> getPriorityBehaviourPairs() {
		final List<IntObjectPair<BehaviorControl<? super T>>> pairs = new ObjectArrayList<>(this.behaviours.size());
		int priority = this.priorityStart;

		for (BehaviorControl<? super T> behaviour : this.behaviours) {
			pairs.add(IntObjectPair.of(priority++, behaviour));
		}

		return pairs;
	}
	//</editor-fold>
}
