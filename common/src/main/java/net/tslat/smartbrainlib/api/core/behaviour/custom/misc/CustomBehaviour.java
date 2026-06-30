package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A pseudo-abstract behaviour that simply calls a callback when running
///
/// Useful for handling custom minor actions that are either too specific to warrant a new behaviour or not worth implementing into a full behaviour
///
/// @see #startCondition(Predicate)
/// @see #whenStarting(Consumer)
/// @param <BO> The brain owner entity
public final class CustomBehaviour<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	private Set<MemoryCondition<?, ?>> memoryConditions = Set.of();
	
	/// Set the memory conditions for this behaviour to start
	///
	/// @see MemoryTest
	/// @see #getMemoryRequirements()
	public CustomBehaviour<BO> memoryConditions(Set<MemoryCondition<?, ?>> memoryConditions) {
		this.memoryConditions = memoryConditions;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@Override
	public CustomBehaviour<BO> whenStarting(Consumer<BO> callback) {
		return (CustomBehaviour<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@Override
	public CustomBehaviour<BO> whenStopping(Consumer<BO> callback) {
		return (CustomBehaviour<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@Override
	public CustomBehaviour<BO> runFor(int ticks) {
		return (CustomBehaviour<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@Override
	public CustomBehaviour<BO> runFor(int minTicks, int maxTicks) {
		return (CustomBehaviour<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@Override
	public CustomBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (CustomBehaviour<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@Override
	public CustomBehaviour<BO> noTimeout() {
		return (CustomBehaviour<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@Override
	public CustomBehaviour<BO> cooldownFor(int ticks) {
		return (CustomBehaviour<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@Override
	public CustomBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
		return (CustomBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@Override
	public CustomBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (CustomBehaviour<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@Override
	public CustomBehaviour<BO> startCondition(Predicate<BO> predicate) {
		return (CustomBehaviour<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@Override
	public CustomBehaviour<BO> stopIf(Predicate<BO> predicate) {
		return (CustomBehaviour<BO>)super.stopIf(predicate);
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
		return this.memoryConditions;
	}
	//</editor-fold>
}
