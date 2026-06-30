package net.tslat.smartbrainlib.api.core.behaviour.base;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.tslat.smartbrainlib.library.object.collection.WeightedShuffleableList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// [GroupBehaviour] that attempts to run all sub-behaviours in order, running all that apply
///
/// This allows for wrapping entire groups of behaviours in overarching conditions or nesting them in other groups<br/>
/// This will count this behaviour as running if any of the child behaviours are running
///
/// @param <BO> The brain owner entity
public class AllApplicableBehaviours<BO extends LivingEntity> extends GroupBehaviour<BO> {
	@SafeVarargs
	public AllApplicableBehaviours(ObjectIntPair<ExtendedBehaviour<? super BO>>... behaviours) {
		super(behaviours);
	}

	@SafeVarargs
	public AllApplicableBehaviours(ExtendedBehaviour<? super BO>... behaviours) {
		super(behaviours);
	}

	public AllApplicableBehaviours(Collection<ObjectIntPair<ExtendedBehaviour<? super BO>>> behaviours) {
		super(behaviours);
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Add an unweighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> add(ExtendedBehaviour<? super BO> behaviour) {
		return (AllApplicableBehaviours<BO>)super.add(behaviour);
	}

	/// Add a weighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> add(ExtendedBehaviour<? super BO> behaviour, int weight) {
		return (AllApplicableBehaviours<BO>)super.add(behaviour, weight);
	}

	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> whenStarting(Consumer<BO> callback) {
		return (AllApplicableBehaviours<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> whenStopping(Consumer<BO> callback) {
		return (AllApplicableBehaviours<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> runFor(int ticks) {
		return (AllApplicableBehaviours<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> runFor(int minTicks, int maxTicks) {
		return (AllApplicableBehaviours<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (AllApplicableBehaviours<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> noTimeout() {
		return (AllApplicableBehaviours<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> cooldownFor(int ticks) {
		return (AllApplicableBehaviours<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> cooldownFor(int minTicks, int maxTicks) {
		return (AllApplicableBehaviours<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (AllApplicableBehaviours<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> startCondition(Predicate<BO> predicate) {
		return (AllApplicableBehaviours<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public AllApplicableBehaviours<BO> stopIf(Predicate<BO> predicate) {
		return (AllApplicableBehaviours<BO>)super.stopIf(predicate);
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
		ExtendedBehaviour<? super BO> lastSuccessfulBehaviour = null;

		for (ExtendedBehaviour<? super BO> behaviour : behaviours) {
			if (behaviour.tryStart(level, entity, gameTime))
				lastSuccessfulBehaviour = behaviour;
		}

		return lastSuccessfulBehaviour;
	}

	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	/// 
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true 
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		boolean stillValid = false;

		for (ExtendedBehaviour<? super BO> behaviour : this.behaviours) {
			stillValid |= behaviour.getStatus() == Status.RUNNING && behaviour.canStillUse((ServerLevel)entity.level(), entity, entity.level().getGameTime());
		}

		return stillValid;
	}
	
	/// Determine whether this behaviour has run for longer than its [Behavior#endTimestamp] allows it to run for
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	///
	/// @see #runFor
	@ApiStatus.Internal
	@Override
	protected boolean timedOut(long gameTime) {
		boolean timedOut = true;

		for (ExtendedBehaviour<? super BO> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING && !behaviour.timedOut(gameTime))
				timedOut = false;
		}

		return timedOut;
	}
	
	/// Perform any internal per-tick functionality for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	@ApiStatus.Internal
	@Override
	protected void tick(ServerLevel level, BO owner, long gameTime) {
		boolean stillRunning = false;

		for (ExtendedBehaviour<? super BO> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING) {
				behaviour.tickOrStop(level, owner, gameTime);

				if (behaviour.getStatus() != Status.STOPPED)
					stillRunning = true;
			}
		}

		if (!stillRunning)
			doStop(level, owner, gameTime);
	}
	
	/// Perform any internal cleanup functionality on task stop for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	@ApiStatus.Internal
	@Override
	protected void stop(ServerLevel level, BO entity, long gameTime) {
		this.cooldownFinishedAt = gameTime + this.cooldownProvider.applyAsInt(entity);

		this.onStopCallback.accept(entity);
		stop(entity);

		for (ExtendedBehaviour<? super BO> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING)
				behaviour.doStop(level, entity, gameTime);
		}
	}

	@Override
	public Status getStatus() {
		for (ExtendedBehaviour<? super BO> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING)
				return Status.RUNNING;
		}

		return Status.STOPPED;
	}

	@Override
	public String toString() {
		final List<BehaviorControl<? super BO>> activeBehaviours = new ObjectArrayList<>();
		final List<BehaviorControl<? super BO>> inactiveBehaviours = new ObjectArrayList<>();

		for (BehaviorControl<? super BO> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.STOPPED) {
				inactiveBehaviours.add(behaviour);
			}
			else {
				activeBehaviours.add(behaviour);
			}
		}

		return "(" + getClass().getSimpleName() + "): ACTIVE:" + activeBehaviours + ", INACTIVE: " + inactiveBehaviours;
	}
	//</editor-fold>
}
