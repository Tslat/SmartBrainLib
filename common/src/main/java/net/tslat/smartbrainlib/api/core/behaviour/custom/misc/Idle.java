package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Do nothing at all
///
/// @param <BO> The brain owner entity
public class Idle<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> whenStarting(Consumer<BO> callback) {
		return (Idle<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> whenStopping(Consumer<BO> callback) {
		return (Idle<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> runFor(int ticks) {
		return (Idle<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> runFor(int minTicks, int maxTicks) {
		return (Idle<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (Idle<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> noTimeout() {
		return (Idle<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> cooldownFor(int ticks) {
		return (Idle<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> cooldownFor(int minTicks, int maxTicks) {
		return (Idle<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (Idle<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> startCondition(Predicate<BO> predicate) {
		return (Idle<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public Idle<BO> stopIf(Predicate<BO> predicate) {
		return (Idle<BO>)super.stopIf(predicate);
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
		return Set.of();
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return true;
	}
	//</editor-fold>
}
