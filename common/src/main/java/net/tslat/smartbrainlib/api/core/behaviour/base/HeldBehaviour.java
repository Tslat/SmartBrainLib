package net.tslat.smartbrainlib.api.core.behaviour.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An abstract behaviour used for tasks that should have an ongoing effect, optionally with an early finish
///
/// This is most useful for things like attacks with multi-tick effects such as beams or flamethrowers, or other prolonged actions
///
/// @param <BO> The brain owner entity
public abstract class HeldBehaviour<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected Consumer<BO> tickCallback = _ -> {};
	protected int runningTime = 0;

	public HeldBehaviour() {
		noTimeout();
	}

	/// Add an additional per-tick callback to be run each time this behaviour ticks
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> whenTicking(Consumer<BO> callback) {
		this.tickCallback = callback;

		return this;
	}

	/// Gets the number of ticks this behaviour has been running for
	public int getRunningTime() {
		return this.runningTime;
	}

	/// This method is run every tick, for as long as this behaviour is running
	///
	/// Run the intended per-tick behaviour here, then return true to continue for another tick, or false to stop
	public abstract boolean onTick(BO entity);

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (HeldBehaviour<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (HeldBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> runFor(int ticks) {
		return (HeldBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (HeldBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (HeldBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> noTimeout() {
		return (HeldBehaviour<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> cooldownFor(int ticks) {
		return (HeldBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (HeldBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (HeldBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (HeldBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public HeldBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (HeldBehaviour<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	/// 
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true 
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return true;
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
		super.start(level, entity, gameTime);

		this.runningTime = 0;
	}
	
	/// Perform any internal per-tick functionality for this behaviour
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	@ApiStatus.Internal
	@Override
	protected void tick(ServerLevel level, BO owner, long gameTime) {
		super.tick(level, owner, gameTime);

		if (!onTick(owner)) {
			doStop(level, owner, gameTime);

			return;
		}

		this.tickCallback.accept(owner);
		this.runningTime++;
	}

	@ApiStatus.Obsolete
	@Override
	protected final void tick(BO entity) {}
	//</editor-fold>
}
