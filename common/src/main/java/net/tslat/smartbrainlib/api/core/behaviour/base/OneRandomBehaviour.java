package net.tslat.smartbrainlib.api.core.behaviour.base;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.library.object.collection.WeightedShuffleableList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// [GroupBehaviour] that randomly selects one of its sub-behaviours to run
///
/// If a behaviour fails to start, then another random behaviour will be checked, continuing until either no behaviours are applicable, or one starts
///
/// Behaviours may be weighted, allowing for prioritisation of random selection of behaviours
///
/// @param <BO> The brain owner entity
public class OneRandomBehaviour<BO extends LivingEntity> extends GroupBehaviour<BO> {
	@SafeVarargs
	public OneRandomBehaviour(ObjectIntPair<ExtendedBehaviour<? super BO>>... behaviours) {
		super(behaviours);
	}

	@SafeVarargs
	public OneRandomBehaviour(ExtendedBehaviour<? super BO>... behaviours) {
		super(behaviours);
	}

	public OneRandomBehaviour(Collection<ObjectIntPair<ExtendedBehaviour<? super BO>>> behaviours) {
		super(behaviours);
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Add an unweighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> add(ExtendedBehaviour<? super BO> behaviour) {
		return (OneRandomBehaviour<BO>)super.add(behaviour);
	}

	/// Add a weighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> add(ExtendedBehaviour<? super BO> behaviour, int weight) {
		return (OneRandomBehaviour<BO>)super.add(behaviour, weight);
	}

	/// A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task
	///
	/// @param callback The function to call when starting the behaviour, immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (OneRandomBehaviour<BO>)super.whenStarting(callback);
	}

	/// A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task
	///
	/// Note that the task stopping does not necessarily mean it was successful
	///
	/// @param callback The function to call when stopping the behaviour, immediately prior to [#stop(LivingEntity)]
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (OneRandomBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param ticks The number of ticks to run for
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> runFor(int ticks) {
		return (OneRandomBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param minTicks The minimum number of ticks to run for
	/// @param maxTicks The maximum number of ticks to run for
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (OneRandomBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should run for once activated
	///
	/// @param timeProvider A function for the tick value
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (OneRandomBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Prevent a tick-based timeout for this behaviour; and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> noTimeout() {
		return (OneRandomBehaviour<BO>)super.noTimeout();
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param ticks The number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> cooldownFor(int ticks) {
		return (OneRandomBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param minTicks The minimum number of ticks to cooldown for
	/// @param maxTicks The maximum number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (OneRandomBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param timeProvider A function for the tick value to cooldown for
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (OneRandomBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (OneRandomBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set an automatic condition for the behaviour to stop<br/>
	/// Has no effect on one-shot behaviours that don't have a runtime
	///
	/// Stops the behaviour if it is active and this predicate returns true
	///
	/// @param predicate The condition to cause an early stop of the behaviour
	@ApiStatus.NonExtendable
	public OneRandomBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (OneRandomBehaviour<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Select the next behaviour to act as the state-keeper out of the behaviours in this group
	///
	/// This may be called at any time, but there is no expectation of state-based handling of this method<br/>
	/// The behaviour returned may not necessarily be the only behaviour run when this method is called, but does act as the state-keeper for this behaviour, to determine whether
	/// this group behaviour is running or not
	///
	/// @return The next behaviour to use as the state-keeper of this group, or `null` if this group should stop running
	@Override
	protected @Nullable ExtendedBehaviour<? super BO> pickBehaviour(ServerLevel level, BO entity, long gameTime, WeightedShuffleableList<ExtendedBehaviour<? super BO>> behaviours) {
		behaviours.shuffle();

		for (ExtendedBehaviour<? super BO> behaviour : behaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				return behaviour;
		}

		return null;
	}
	//</editor-fold>
}