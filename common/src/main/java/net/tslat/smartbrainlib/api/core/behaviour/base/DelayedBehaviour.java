package net.tslat.smartbrainlib.api.core.behaviour.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An abstract behaviour used for tasks that should have a start, and then a followup delayed action<br/>
/// This is most useful for things like attacks that have associated animations, or action which require a charge up or prep time
///
/// @param <BO> The brain owner entity
public abstract class DelayedBehaviour<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected final ToIntFunction<BO> delayTime;
	protected long delayFinishedAt = 0;
	protected Consumer<BO> delayedCallback = _ -> {};

	public DelayedBehaviour(int delayTicks) {
		this(_ -> delayTicks);
	}

	public DelayedBehaviour(ToIntFunction<BO> delayTicks) {
		this.delayTime = delayTicks;

		runFor(entity -> this.delayTime.applyAsInt(entity) + 1);
	}

	/// Set an additional callback to run when the delayed activation is called
	///
	/// @param callback The callback function
	public final DelayedBehaviour<BO> whenActivating(Consumer<BO> callback) {
		this.delayedCallback = callback;

		return this;
	}

	/// The action to take once the delay period has elapsed.
	///
	/// @param entity The owner of the brain
	protected void doDelayedAction(BO entity) {}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task
	///
	/// @param callback The function to call when starting the behaviour, immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (DelayedBehaviour<BO>)super.whenStarting(callback);
	}

	/// A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task
	///
	/// Note that the task stopping does not necessarily mean it was successful
	///
	/// @param callback The function to call when stopping the behaviour, immediately prior to [#stop(LivingEntity)]
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (DelayedBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param ticks The number of ticks to run for
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> runFor(int ticks) {
		return (DelayedBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param minTicks The minimum number of ticks to run for
	/// @param maxTicks The maximum number of ticks to run for
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (DelayedBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should run for once activated
	///
	/// @param timeProvider A function for the tick value
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (DelayedBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Prevent a tick-based timeout for this behaviour; and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> noTimeout() {
		return (DelayedBehaviour<BO>)super.noTimeout();
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param ticks The number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> cooldownFor(int ticks) {
		return (DelayedBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param minTicks The minimum number of ticks to cooldown for
	/// @param maxTicks The maximum number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (DelayedBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param timeProvider A function for the tick value to cooldown for
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (DelayedBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (DelayedBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set an automatic condition for the behaviour to stop<br/>
	/// Has no effect on one-shot behaviours that don't have a runtime
	///
	/// Stops the behaviour if it is active and this predicate returns true
	///
	/// @param predicate The condition to cause an early stop of the behaviour
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (DelayedBehaviour<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	@ApiStatus.Internal
	@Override
	protected void start(ServerLevel level, BO entity, long gameTime) {
		final int nextDelay = this.delayTime.applyAsInt(entity);

		if (nextDelay > 0) {
			this.delayFinishedAt = gameTime + nextDelay;

			super.start(level, entity, gameTime);
		}
		else {
			super.start(level, entity, gameTime);
			doDelayedAction(entity);
		}
	}

	@ApiStatus.Internal
	@Override
	protected void stop(ServerLevel level, BO entity, long gameTime) {
		super.stop(level, entity, gameTime);

		this.delayFinishedAt = 0;
	}

	@ApiStatus.Internal
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return this.delayFinishedAt >= entity.level().getGameTime();
	}

	@ApiStatus.Internal
	@Override
	protected void tick(ServerLevel level, BO entity, long gameTime) {
		super.tick(level, entity, gameTime);

		if (this.delayFinishedAt <= gameTime) {
			doDelayedAction(entity);
			this.delayedCallback.accept(entity);
		}
	}
	//</editor-fold>
}
