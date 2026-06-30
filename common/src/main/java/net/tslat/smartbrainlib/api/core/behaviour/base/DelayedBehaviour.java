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
	@ApiStatus.NonExtendable
	public DelayedBehaviour<BO> whenActivating(Consumer<BO> callback) {
		this.delayedCallback = callback;

		return this;
	}

	/// The action to take once the delay period has elapsed
	protected void doDelayedAction(BO entity) {}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (DelayedBehaviour<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (DelayedBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> runFor(int ticks) {
		return (DelayedBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (DelayedBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (DelayedBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> noTimeout() {
		return (DelayedBehaviour<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> cooldownFor(int ticks) {
		return (DelayedBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (DelayedBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (DelayedBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (DelayedBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public DelayedBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (DelayedBehaviour<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
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
		final int nextDelay = this.delayTime.applyAsInt(entity);

		if (nextDelay > 0) {
			this.delayFinishedAt = gameTime + nextDelay;

			super.start(level, entity, gameTime);
		}
		else {
			super.start(level, entity, gameTime);
			doDelayedAction(entity);
			this.delayedCallback.accept(entity);
		}
	}
	
	/// Perform any internal cleanup functionality on task stop for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
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
	
	/// Perform any internal per-tick functionality for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
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
