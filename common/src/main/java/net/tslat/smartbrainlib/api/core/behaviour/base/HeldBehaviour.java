package net.tslat.smartbrainlib.api.core.behaviour.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An abstract behaviour used for tasks that should have an ongoing effect, optionally with an early finish<br/>
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
	/// A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task
	///
	/// @param callback The function to call when starting the behaviour, immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (HeldBehaviour<BO>)super.whenStarting(callback);
	}

	/// A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task
	///
	/// Note that the task stopping does not necessarily mean it was successful
	///
	/// @param callback The function to call when stopping the behaviour, immediately prior to [#stop(LivingEntity)]
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (HeldBehaviour<BO>)super.whenStopping(callback);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param ticks The number of ticks to run for
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> runFor(int ticks) {
		return (HeldBehaviour<BO>)super.runFor(ticks);
	}

	/// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
	/// The value used is in _ticks_
	///
	/// @param minTicks The minimum number of ticks to run for
	/// @param maxTicks The maximum number of ticks to run for
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (HeldBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should run for once activated
	///
	/// @param timeProvider A function for the tick value
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (HeldBehaviour<BO>)super.runFor(timeProvider);
	}

	/// Prevent a tick-based timeout for this behaviour; and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> noTimeout() {
		return (HeldBehaviour<BO>)super.noTimeout();
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param ticks The number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> cooldownFor(int ticks) {
		return (HeldBehaviour<BO>)super.cooldownFor(ticks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param minTicks The minimum number of ticks to cooldown for
	/// @param maxTicks The maximum number of ticks to cooldown for
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (HeldBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set the length (in ticks) that the task should wait for between activations<br/>
	/// This is the time between when the task stops, and it is able to start again
	///
	/// @param timeProvider A function for the tick value to cooldown for
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (HeldBehaviour<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (HeldBehaviour<BO>)super.startCondition(predicate);
	}

	/// Set an automatic condition for the behaviour to stop<br/>
	/// Has no effect on one-shot behaviours that don't have a runtime
	///
	/// Stops the behaviour if it is active and this predicate returns true
	///
	/// @param predicate The condition to cause an early stop of the behaviour
	@ApiStatus.NonExtendable
	public HeldBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (HeldBehaviour<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Check whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(BO)]
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	///
	/// @param entity The brain owner entity
	/// @return Whether the behaviour should continue ticking
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return true;
	}

	@ApiStatus.Internal
	@Override
	protected void start(ServerLevel level, BO entity, long gameTime) {
		super.start(level, entity, gameTime);

		this.runningTime = 0;
	}

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
