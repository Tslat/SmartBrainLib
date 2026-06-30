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

/// [GroupBehaviour] that attempts to run all sub-behaviours in order, only running the first applicable behaviour available at any given time
///
/// @param <BO> The brain owner entity
public class FirstApplicableBehaviour<BO extends LivingEntity> extends GroupBehaviour<BO> {
	@SafeVarargs
	public FirstApplicableBehaviour(ObjectIntPair<ExtendedBehaviour<? super BO>>... behaviours) {
		super(behaviours);
	}

	@SafeVarargs
	public FirstApplicableBehaviour(ExtendedBehaviour<? super BO>... behaviours) {
		super(behaviours);
	}

	public FirstApplicableBehaviour(Collection<ObjectIntPair<ExtendedBehaviour<? super BO>>> behaviours) {
		super(behaviours);
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Add an unweighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> add(ExtendedBehaviour<? super BO> behaviour) {
		return (FirstApplicableBehaviour<BO>)super.add(behaviour);
	}

	/// Add a weighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
    @ApiStatus.NonExtendable
    @Override
	public FirstApplicableBehaviour<BO> add(ExtendedBehaviour<? super BO> behaviour, int weight) {
		return (FirstApplicableBehaviour<BO>)super.add(behaviour, weight);
	}

	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (FirstApplicableBehaviour<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (FirstApplicableBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> runFor(int ticks) {
		return (FirstApplicableBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (FirstApplicableBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (FirstApplicableBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> noTimeout() {
		return (FirstApplicableBehaviour<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> cooldownFor(int ticks) {
		return (FirstApplicableBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (FirstApplicableBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (FirstApplicableBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (FirstApplicableBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public FirstApplicableBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (FirstApplicableBehaviour<BO>)super.stopIf(predicate);
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
		for (ExtendedBehaviour<? super BO> behaviour : behaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				return behaviour;
		}

		return null;
	}
	//</editor-fold>
}
