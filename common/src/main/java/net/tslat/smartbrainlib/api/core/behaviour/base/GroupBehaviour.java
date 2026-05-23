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
import java.util.List;
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
	/// A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task
	///
	/// @param callback The function to call when starting the behaviour, immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (GroupBehaviour<BO>)super.whenStarting(callback);
	}

	/// A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task
	///
	/// Note that the task stopping does not necessarily mean it was successful
	///
	/// @param callback The function to call when stopping the behaviour, immediately prior to [#stop(LivingEntity)]
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (GroupBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param ticks The number of ticks to run for
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> runFor(int ticks) {
		return (GroupBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param minTicks The minimum number of ticks to run for
	/// @param maxTicks The maximum number of ticks to run for
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (GroupBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should run for once activated
	///
	/// @param timeProvider A function for the tick value
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (GroupBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Prevent a tick-based timeout for this behaviour; and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> noTimeout() {
		return (GroupBehaviour<BO>)super.noTimeout();
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param ticks The number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> cooldownFor(int ticks) {
		return (GroupBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param minTicks The minimum number of ticks to cooldown for
	/// @param maxTicks The maximum number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (GroupBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param timeProvider A function for the tick value to cooldown for
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (GroupBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (GroupBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set an automatic condition for the behaviour to stop<br/>
	/// Has no effect on one-shot behaviours that don't have a runtime
	///
	/// Stops the behaviour if it is active and this predicate returns true
	///
	/// @param predicate The condition to cause an early stop of the behaviour
	@ApiStatus.NonExtendable
	public GroupBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (GroupBehaviour<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Custom Implementation Boilerplate>">
	/// The list of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached list
	///
	/// @see MemoryTest
	/// @return The [List] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	@Override
	public List<MemoryCondition<?, ?>> getMemoryRequirements() {
		return List.of();
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

	@ApiStatus.Internal
	@Override
	protected boolean doStartCheck(ServerLevel level, BO entity, long gameTime) {
		if (!super.doStartCheck(level, entity, gameTime))
			return false;

		return (this.runningBehaviour = pickBehaviour(level, entity, gameTime, this.behaviours)) != null;
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
		return this.runningBehaviour != null && this.runningBehaviour.canStillUse((ServerLevel)entity.level(), entity, entity.level().getGameTime());
	}

	/// Determine whether this behaviour has run for longer than its [Behavior#endTimestamp] allows it to run for
	///
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	@ApiStatus.Internal
	@Override
	protected boolean timedOut(long gameTime) {
		return this.runningBehaviour == null || this.runningBehaviour.timedOut(gameTime);
	}

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
