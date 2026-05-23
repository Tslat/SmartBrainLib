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
	public AllApplicableBehaviours<BO> add(ExtendedBehaviour<? super BO> behaviour) {
		return (AllApplicableBehaviours<BO>)super.add(behaviour);
	}

	/// Add a weighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> add(ExtendedBehaviour<? super BO> behaviour, int weight) {
		return (AllApplicableBehaviours<BO>)super.add(behaviour, weight);
	}

	/// A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task
	///
	/// @param callback The function to call when starting the behaviour, immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> whenStarting(Consumer<BO> callback) {
		return (AllApplicableBehaviours<BO>)super.whenStarting(callback);
	}

	/// A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task
	///
	/// Note that the task stopping does not necessarily mean it was successful
	///
	/// @param callback The function to call when stopping the behaviour, immediately prior to [#stop(LivingEntity)]
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> whenStopping(Consumer<BO> callback) {
		return (AllApplicableBehaviours<BO>)super.whenStopping(callback);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param ticks The number of ticks to run for
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> runFor(int ticks) {
		return (AllApplicableBehaviours<BO>)super.runFor(ticks);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param minTicks The minimum number of ticks to run for
	/// @param maxTicks The maximum number of ticks to run for
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> runFor(int minTicks, int maxTicks) {
		return (AllApplicableBehaviours<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should run for once activated
	///
	/// @param timeProvider A function for the tick value
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (AllApplicableBehaviours<BO>)super.runFor(timeProvider);
	}

	/// Prevent a tick-based timeout for this behaviour; and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> noTimeout() {
		return (AllApplicableBehaviours<BO>)super.noTimeout();
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param ticks The number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> cooldownFor(int ticks) {
		return (AllApplicableBehaviours<BO>)super.cooldownFor(ticks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param minTicks The minimum number of ticks to cooldown for
	/// @param maxTicks The maximum number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> cooldownFor(int minTicks, int maxTicks) {
		return (AllApplicableBehaviours<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param timeProvider A function for the tick value to cooldown for
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (AllApplicableBehaviours<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	public AllApplicableBehaviours<BO> startCondition(Predicate<BO> predicate) {
		return (AllApplicableBehaviours<BO>)super.startCondition(predicate);
	}

	/// Set an automatic condition for the behaviour to stop<br/>
	/// Has no effect on one-shot behaviours that don't have a runtime
	///
	/// Stops the behaviour if it is active and this predicate returns true
	///
	/// @param predicate The condition to cause an early stop of the behaviour
	@ApiStatus.NonExtendable
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

	/// Check whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(BO)]
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	///
	/// @param entity The brain owner entity
	/// @return Whether the behaviour should continue ticking
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
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	@Override
	protected boolean timedOut(long gameTime) {
		boolean timedOut = true;

		for (ExtendedBehaviour<? super BO> behaviour : this.behaviours) {
			if (behaviour.getStatus() == Status.RUNNING && !behaviour.timedOut(gameTime))
				timedOut = false;
		}

		return timedOut;
	}

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

	@Override
	protected void stop(ServerLevel level, BO entity, long gameTime) {
		this.cooldownFinishedAt = gameTime + this.cooldownProvider.applyAsInt(entity);

		this.taskStopCallback.accept(entity);
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
