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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An extension of the base Behavior class that is used for tasks in the brain system<br/>
/// This extension auto-handles some boilerplate and adds in some additional auto-handled functions:
/// - Task start and stop callbacks for additional entity-interactions
/// - A functional implementation of a duration provider
/// - A functional implementation of a cooldown provider
///
/// Ideally, all custom behaviours should use at least this class as a base, instead of the core [BehaviorControl] class
///
/// @param <BO> The brain owner entity
public abstract class ExtendedBehaviour<BO extends LivingEntity> extends Behavior<BO> {
	protected Predicate<BO> startCondition = _ -> true;
	protected Predicate<BO> stopCondition = _ -> false;
	protected Consumer<BO> taskStartCallback = _ -> {};
	protected Consumer<BO> taskStopCallback = _ -> {};

	protected ToIntFunction<BO> runtimeProvider = _ -> 60;
	protected ToIntFunction<BO> cooldownProvider = _ -> 0;
	protected long cooldownFinishedAt = 0;

	public ExtendedBehaviour() {
		super(new Reference2ObjectArrayMap<>());

		for (MemoryCondition<?, ?> condition : getMemoryRequirements()) {
			this.entryCondition.put(condition.memory(), condition.condition());
		}
	}

	/// A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task
	///
	/// @param callback The function to call when starting the behaviour, immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> whenStarting(Consumer<BO> callback) {
		this.taskStartCallback = callback;

		return this;
	}

	/// A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task
	///
	/// Note that the task stopping does not necessarily mean it was successful
	///
	/// @param callback The function to call when stopping the behaviour, immediately prior to [#stop(LivingEntity)]
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> whenStopping(Consumer<BO> callback) {
		this.taskStopCallback = callback;

		return this;
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param ticks The number of ticks to run for
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> runFor(int ticks) {
		return runFor(_ -> ticks);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param minTicks The minimum number of ticks to run for
	/// @param maxTicks The maximum number of ticks to run for
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return runFor(entity -> entity.getRandom().nextIntBetweenInclusive(minTicks, maxTicks));
	}

	/// Set the length (in ticks) that the task should run for once activated
	///
	/// @param timeProvider A function for the tick value
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		this.runtimeProvider = timeProvider;

		return this;
	}

	/// Prevent a tick-based timeout for this behaviour; and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> noTimeout() {
		return runFor(Integer.MAX_VALUE);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param ticks The number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> cooldownFor(int ticks) {
		return cooldownFor(_ -> ticks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param minTicks The minimum number of ticks to cooldown for
	/// @param maxTicks The maximum number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return cooldownFor(entity -> entity.getRandom().nextIntBetweenInclusive(minTicks, maxTicks));
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param timeProvider A function for the tick value to cooldown for
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		this.cooldownProvider = timeProvider;

		return this;
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> startCondition(Predicate<BO> predicate) {
		this.startCondition = predicate;

		return this;
	}

	/// Set an automatic condition for the behaviour to stop<br/>
	/// Has no effect on one-shot behaviours that don't have a runtime
	///
	/// Stops the behaviour if it is active and this predicate returns true
	///
	/// @param predicate The condition to cause an early stop of the behaviour
	@ApiStatus.NonExtendable
	public ExtendedBehaviour<BO> stopIf(Predicate<BO> predicate) {
		this.stopCondition = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Custom Implementation Boilerplate>">
	/// The list of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached list
	///
	/// @see MemoryTest
	/// @return The [List] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	public abstract List<MemoryCondition<?, ?>> getMemoryRequirements();

	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [ExtendedBehaviour#getMemoryRequirements()] have already been checked
	///
	/// @param level  The level the entity is in
	/// @param entity The brain owner entity
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		return true;
	}

	/// Run any custom functionality when the behaviour starts<br/>
	/// This method is called once per behaviour run, right as it begins
	///
	/// By this stage any memory requirements set in [ExtendedBehaviour#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve.
	///
	/// Normally, this is where you would run preparation tasks and set up any relevant behaviour variables
	///
	/// @param entity The brain owner entity
	protected void start(BO entity) {}

	/// Check whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(BO)]
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	///
	/// @param entity The brain owner entity
	/// @return Whether the behaviour should continue ticking
	protected boolean shouldKeepRunning(BO entity) {
		return false;
	}

	/// Handle per-tick functionality for this behaviour
	///
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// NOTE: Memory requirements are _not_ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	///
	/// @param entity The brain owner entity
	protected void tick(BO entity) {}

	/// Called every tick when this behaviour is still running, but has been asked to expire<br/>
	/// You may use this to gracefully retire a behaviour when switching activities, rather than abruptly ending it
	///
	/// Usually this occurs when the [Activity] this behaviour belonged to has been stopped
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
	///
	/// @param entity The brain owner entity
	protected void stop(BO entity) {}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	@ApiStatus.Internal
	@Override
	public boolean tryStart(ServerLevel level, BO entity, long gameTime) {
		if (!doStartCheck(level, entity, gameTime))
			return false;

		this.status = Status.RUNNING;
		this.endTimestamp = gameTime + this.runtimeProvider.applyAsInt(entity);

		start(level, entity, gameTime);

		return true;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @ApiStatus.Internal
	protected boolean doStartCheck(ServerLevel level, BO entity, long gameTime) {
		return this.cooldownFinishedAt <= gameTime && hasRequiredMemories(entity) && this.startCondition.test(entity)
				&& checkExtraStartConditions(level, entity);
	}

	@ApiStatus.Internal
	@Override
	protected void start(ServerLevel level, BO entity, long gameTime) {
		this.taskStartCallback.accept(entity);
		start(entity);
	}

	/// Determine whether the conditions for this behaviour are still valid for the current tick
	///
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	@ApiStatus.Internal
	@Override
	protected boolean canStillUse(ServerLevel level, BO entity, long gameTime) {
		return shouldKeepRunning(entity) && !this.stopCondition.test(entity);
	}

	/// Determine whether this behaviour has run for longer than its [Behavior#endTimestamp] allows it to run for
	///
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	@Override
	protected boolean timedOut(long timestamp) {
		return super.timedOut(timestamp);
	}

	@ApiStatus.Internal
	@Override
	protected void tick(ServerLevel level, BO entity, long gameTime) {
		tick(entity);
	}

	@ApiStatus.Internal
	@Override
	protected void stop(ServerLevel level, BO entity, long gameTime) {
		this.cooldownFinishedAt = gameTime + this.cooldownProvider.applyAsInt(entity);

		this.taskStopCallback.accept(entity);
		stop(entity);
	}

	/// Vanilla's entrypoint for checking whether the brain owner entity meets all the memory conditions this behaviour has
	@ApiStatus.Obsolete
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
