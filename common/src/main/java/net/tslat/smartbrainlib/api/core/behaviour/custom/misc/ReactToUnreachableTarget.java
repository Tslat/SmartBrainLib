package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

/// React to the brain owner entity not being able to reach their walk target for a period of time
///
/// @see UnreachableTargetSensor
/// @see MoveToWalkTarget
/// @param <BO> The brain owner entity
public class ReactToUnreachableTarget<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemories(MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, SBLMemoryTypes.TARGET_UNREACHABLE.get());

	protected ToIntBiFunction<BO, WalkTarget> ticksToReact = (_, _) -> 100;
	protected TriConsumer<BO, WalkTarget, Boolean> callback = (_, _, _) -> {};

	protected long reactAtTime = 0;
	protected @Nullable WalkTarget walkTarget = null;

	/// Set the length of time (in ticks) that the target should be unreachable before reacting
	@ApiStatus.NonExtendable
	public ReactToUnreachableTarget<BO> timeBeforeReacting(ToIntBiFunction<BO, WalkTarget> ticksToReact) {
		this.ticksToReact = ticksToReact;

		return this;
	}

	/// Set the function to run when the given time has elapsed and the target is still unreachable
	@ApiStatus.NonExtendable
	public ReactToUnreachableTarget<BO> reaction(TriConsumer<BO, WalkTarget, Boolean> callback) {
		this.callback = callback;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> whenStarting(Consumer<BO> callback) {
		return (ReactToUnreachableTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> whenStopping(Consumer<BO> callback) {
		return (ReactToUnreachableTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> runFor(int ticks) {
		return (ReactToUnreachableTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> runFor(int minTicks, int maxTicks) {
		return (ReactToUnreachableTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (ReactToUnreachableTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> noTimeout() {
		return (ReactToUnreachableTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> cooldownFor(int ticks) {
		return (ReactToUnreachableTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (ReactToUnreachableTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (ReactToUnreachableTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> startCondition(Predicate<BO> predicate) {
		return (ReactToUnreachableTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public ReactToUnreachableTarget<BO> stopIf(Predicate<BO> predicate) {
		return (ReactToUnreachableTarget<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// The set of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached set
	///
	/// @return The [Set] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	/// @see MemoryTest
	@Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}
	
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@MustBeInvokedByOverriders
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		this.walkTarget = BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET);
		
		return this.walkTarget != null;
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	///
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	@MustBeInvokedByOverriders
	@Override
	protected void start(BO entity) {
		//noinspection DataFlowIssue
		this.reactAtTime = entity.level().getGameTime() + this.ticksToReact.applyAsInt(entity, this.walkTarget);
	}
	
	/// Determine whether this behaviour has run for longer than its [Behavior#endTimestamp] allows it to run for
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	///
	/// @see #runFor
	@MustBeInvokedByOverriders
	@ApiStatus.Internal
	@Override
	protected boolean timedOut(long gameTime) {
		return this.reactAtTime == 0 || this.reactAtTime < gameTime;
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@MustBeInvokedByOverriders
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		if (!hasRequiredMemories(entity))
			return false;
		
		this.walkTarget = BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET);
		
		return this.walkTarget != null;
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		if (entity.level().getGameTime() == this.reactAtTime)
			//noinspection DataFlowIssue
			this.callback.accept(entity, this.walkTarget, BrainUtil.memoryOrDefault(entity, SBLMemoryTypes.TARGET_UNREACHABLE.get(), false));
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		this.reactAtTime = 0;
		this.walkTarget = null;
	}
	//</editor-fold>
}
