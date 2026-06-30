package net.tslat.smartbrainlib.api.core.behaviour.base;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An extension of the base [Behavior] class that is used for tasks in the brain system<br/>
/// This extension auto-handles some boilerplate and adds in some additional auto-handled functions:
/// - Task start and stop callbacks for additional entity-interactions
/// - A functional implementation of a duration provider
/// - A functional implementation of a cooldown provider
/// - Task start and stop condition extensions
///
/// Ideally, all custom behaviours should use at least this class as a base, instead of the core [BehaviorControl] class
///
/// @param <BO> The brain owner entity
public abstract class ExtendedBehaviour<BO extends LivingEntity> extends Behavior<BO> {
	protected Predicate<BO> startCondition = _ -> true;
	protected Predicate<BO> stopCondition = _ -> false;
	protected Consumer<BO> onStartCallback = _ -> {};
	protected Consumer<BO> onStopCallback = _ -> {};

	protected ToIntFunction<BO> runtimeProvider = _ -> 60;
	protected ToIntFunction<BO> cooldownProvider = _ -> 0;
	protected long cooldownFinishedAt = 0;

	public ExtendedBehaviour() {
		super(new Reference2ObjectArrayMap<>());

		for (MemoryCondition<?, ?> condition : getMemoryRequirements()) {
			this.entryCondition.put(condition.memory(), condition.condition());
		}
	}
	
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> whenStarting(Consumer<BO> callback) {
		this.onStartCallback = callback;

		return this;
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> whenStopping(Consumer<BO> callback) {
		this.onStopCallback = callback;

		return this;
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> runFor(int ticks) {
		return runFor(_ -> ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return runFor(entity -> entity.getRandom().nextIntBetweenInclusive(minTicks, maxTicks));
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		this.runtimeProvider = timeProvider;

		return this;
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> noTimeout() {
		return runFor(Integer.MAX_VALUE);
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> cooldownFor(int ticks) {
		return cooldownFor(_ -> ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return cooldownFor(entity -> entity.getRandom().nextIntBetweenInclusive(minTicks, maxTicks));
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		this.cooldownProvider = timeProvider;

		return this;
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> startCondition(Predicate<BO> predicate) {
		this.startCondition = predicate;

		return this;
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> stopIf(Predicate<BO> predicate) {
		this.stopCondition = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Custom Implementation Boilerplate>">
	/// The set of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached set
	///
	/// @return The [Set] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	/// @see MemoryTest
	public abstract Set<MemoryCondition<?, ?>> getMemoryRequirements();
	
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		return true;
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	/// 
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	protected void start(BO entity) {}

	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	protected boolean shouldKeepRunning(BO entity) {
		return false;
	}

	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	protected void tick(BO entity) {}

	/// Called every tick when this behaviour is still running, but has been asked to expire<br/>
	/// You may use this to gracefully retire a behaviour when switching activities, rather than abruptly ending it
	///
	/// Usually this occurs when the [Activity] this behaviour belonged to has been stopped or replaced
	///
	/// @return true to confirm this behaviour has expired, or false to continue ticking it
	public boolean tryExpire(ServerLevel level, BO entity, long gameTime) {
		doStop(level, entity, gameTime);

		return true;
	}

	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	protected void stop(BO entity) {}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// The root entrypoint for all [Behavior]s when starting
	///
	/// This should perform all necessary start checks, apply the running [Status], and associated cooldowns and callbacks
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// @see #startCondition(Predicate)
	/// @see #start(LivingEntity)
	/// @see #runFor
	@ApiStatus.Internal
	@Override
	public boolean tryStart(ServerLevel level, BO entity, long gameTime) {
		if (!canStart(level, entity, gameTime))
			return false;

		this.status = Status.RUNNING;
		this.endTimestamp = gameTime + this.runtimeProvider.applyAsInt(entity);

		start(level, entity, gameTime);

		return true;
	}

	/// Check all behaviour start conditions to determine whether the behaviour can start or not
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// @see #startCondition(Predicate)
	/// @see #getMemoryRequirements()
	/// @see #runFor
	/// @see #checkExtraStartConditions(ServerLevel, LivingEntity)
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @ApiStatus.Internal
	protected boolean canStart(ServerLevel level, BO entity, long gameTime) {
		return this.cooldownFinishedAt <= gameTime && hasRequiredMemories(entity) && this.startCondition.test(entity)
				&& checkExtraStartConditions(level, entity);
	}
	
	/// Start the behaviour<br/>
	/// All pre-start checks have been checked and passed by this point
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// @see #start(LivingEntity)
	/// @see #whenStarting(Consumer)
	@ApiStatus.Internal
	@Override
	protected void start(ServerLevel level, BO entity, long gameTime) {
		this.onStartCallback.accept(entity);
		start(entity);
	}

	/// Determine whether the conditions for this behaviour are still valid for the current tick
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	///
	/// @see #shouldKeepRunning(LivingEntity)
	/// @see #stopIf(Predicate)
	@ApiStatus.Internal
	@Override
	protected boolean canStillUse(ServerLevel level, BO entity, long gameTime) {
		return shouldKeepRunning(entity) && !this.stopCondition.test(entity);
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
	protected boolean timedOut(long timestamp) {
		return super.timedOut(timestamp);
	}

	/// Perform any internal per-tick functionality for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	@ApiStatus.Internal
	@Override
	protected void tick(ServerLevel level, BO entity, long gameTime) {
		tick(entity);
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
	}

	/// Vanilla's entrypoint for checking whether the brain owner entity meets all the memory conditions this behaviour has
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	@Override
	public boolean hasRequiredMemories(BO entity) {
		final Brain<?> brain = entity.getBrain();

		for (MemoryCondition<?, ?> condition : getMemoryRequirements()) {
			if (!brain.checkMemory(condition.memory(), condition.condition()))
				return false;
		}

		return true;
	}
	//</editor-fold>
}
