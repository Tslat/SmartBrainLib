package net.tslat.smartbrainlib.api.core.behaviour.base;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.library.object.collection.WeightedShuffleableList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Functional replacement to [net.minecraft.world.entity.ai.behavior.GateBehavior] due to the very poor way it is implemented
///
/// In particular, this allows nesting of group behaviours without breaking behaviour flow entirely<br/>
/// It also allows for utilising the various callbacks and conditions that [ExtendedBehaviour] offers
///
/// **NOTE:** Only supports `ExtendedBehaviour` implementations as sub-behaviours. This is due to access-modifiers on the vanilla behaviours making this prohibitively annoying to work with
///
/// @param <BO> The brain owner entity
public abstract class GroupBehaviour<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected final WeightedShuffleableList<ExtendedBehaviour<? super BO>> behaviours;

	protected @Nullable ExtendedBehaviour<? super BO> runningBehaviour = null;

	@SafeVarargs
    public GroupBehaviour(ObjectIntPair<ExtendedBehaviour<? super BO>>... behaviours) {
		this.behaviours = WeightedShuffleableList.of(behaviours);

		noTimeout();
	}

	@SafeVarargs
	public GroupBehaviour(ExtendedBehaviour<? super BO>... behaviours) {
		this.behaviours = WeightedShuffleableList.of(behaviours);

		noTimeout();
	}

	public GroupBehaviour(Collection<ObjectIntPair<ExtendedBehaviour<? super BO>>> behaviours) {
		this.behaviours = WeightedShuffleableList.wrapWeighted(behaviours);

		noTimeout();
	}

	/// Add an unweighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> add(ExtendedBehaviour<? super BO> behaviour) {
		return add(behaviour, 1);
	}

	/// Add a weighted [ExtendedBehaviour] to the end of this `GroupBehaviour`'s list
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> add(ExtendedBehaviour<? super BO> behaviour, int weight) {
		this.behaviours.add(behaviour, weight);

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (GroupBehaviour<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (GroupBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> runFor(int ticks) {
		return (GroupBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (GroupBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (GroupBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> noTimeout() {
		return (GroupBehaviour<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> cooldownFor(int ticks) {
		return (GroupBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (GroupBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (GroupBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (GroupBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public GroupBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (GroupBehaviour<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Custom Implementation Boilerplate>">
	/// The set of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached set
	///
	/// @return The [Set] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	/// @see MemoryTest
	@Override
	public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
		return Set.of();
	}

	/// Select the next behaviour to act as the state-keeper out of the behaviours in this group
	///
	/// This may be called at any time, but there is no expectation of state-based handling of this method<br/>
	/// The behaviour returned may not necessarily be the only behaviour run when this method is called, but does act as the state-keeper for this behaviour, to determine whether
	/// this group behaviour is running or not
	/// 
	/// @return The next behaviour to use as the state-keeper of this group, or `null` if this group should stop running
	protected abstract @Nullable ExtendedBehaviour<? super BO> pickBehaviour(ServerLevel level, BO entity, long gameTime, WeightedShuffleableList<ExtendedBehaviour<? super BO>> behaviours);
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Get the [ExtendedBehaviour]s this group contains
	public Iterable<ExtendedBehaviour<? super BO>> getBehaviours() {
		return this.behaviours;
	}
	
	/// Check all behaviour start conditions to determine whether the behaviour can start or not
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// @see #startCondition(Predicate)
	/// @see #getMemoryRequirements()
	/// @see #runFor
	/// @see #checkExtraStartConditions(ServerLevel, LivingEntity)
	@ApiStatus.Internal
	@Override
	protected boolean canStart(ServerLevel level, BO entity, long gameTime) {
		if (!super.canStart(level, entity, gameTime))
			return false;

		return (this.runningBehaviour = pickBehaviour(level, entity, gameTime, this.behaviours)) != null;
	}

	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	/// 
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true 
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return this.runningBehaviour != null && this.runningBehaviour.canStillUse((ServerLevel)entity.level(), entity, entity.level().getGameTime());
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
		return this.runningBehaviour == null || this.runningBehaviour.timedOut(gameTime);
	}
	
	/// Perform any internal per-tick functionality for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	@ApiStatus.Internal
	@Override
	protected void tick(ServerLevel level, BO owner, long gameTime) {
		if (this.runningBehaviour == null) {
			doStop(level, owner, gameTime);

			return;
		}

		this.runningBehaviour.tickOrStop(level, owner, gameTime);

		if (this.runningBehaviour.getStatus() == Status.STOPPED) {
			this.runningBehaviour = null;

			doStop(level, owner, gameTime);
		}
	}
	
	/// Perform any internal cleanup functionality on task stop for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	@ApiStatus.Internal
	@Override
	protected void stop(ServerLevel level, BO entity, long gameTime) {
		super.stop(level, entity, gameTime);

		if (this.runningBehaviour != null)
			this.runningBehaviour.doStop(level, entity, gameTime);

		this.runningBehaviour = null;
	}

	@ApiStatus.Internal
	@Override
	public Status getStatus() {
		if (this.runningBehaviour == null)
			return Status.STOPPED;

		return this.runningBehaviour.getStatus();
	}

	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + "): " + (this.runningBehaviour != null ? this.runningBehaviour.toString() : "{}");
	}

	@Override
	public String debugString() {
		return getClass().getSimpleName() + " -> " + (this.runningBehaviour != null ? this.runningBehaviour.debugString() : "{}");
	}
	//</editor-fold>
}
