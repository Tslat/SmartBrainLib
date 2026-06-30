package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Custom behaviour for conditionally invalidating/resetting existing memories<br/>
/// This allows for custom handling of stored memories and clearing them at will
///
/// @param <BO> The brain owner entity
/// @param <M> The data type of the memory
public class InvalidateMemory<BO extends LivingEntity, M> extends ExtendedBehaviour<BO> {
	protected final Set<MemoryCondition<?, ?>> memoryRequirements;

	protected final MemoryModuleType<M> memory;
	protected BiPredicate<BO, M> invalidateIf = (_, _) -> true;

	public InvalidateMemory(MemoryModuleType<M> memory) {
		this.memory = memory;
		this.memoryRequirements = MemoryTest.builder(1).hasMemory(memory);
		
		super();
	}

	/// Sets a custom predicate to invalidate the memory if none of the previous checks invalidate it first
	@ApiStatus.NonExtendable
	public InvalidateMemory<BO, M> invalidateIf(BiPredicate<BO, M> predicate) {
		this.invalidateIf = predicate;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> whenStarting(Consumer<BO> callback) {
		return (InvalidateMemory<BO, M>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> whenStopping(Consumer<BO> callback) {
		return (InvalidateMemory<BO, M>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> runFor(int ticks) {
		return (InvalidateMemory<BO, M>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> runFor(int minTicks, int maxTicks) {
		return (InvalidateMemory<BO, M>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> runFor(ToIntFunction<BO> timeProvider) {
		return (InvalidateMemory<BO, M>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> noTimeout() {
		return (InvalidateMemory<BO, M>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> cooldownFor(int ticks) {
		return (InvalidateMemory<BO, M>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> cooldownFor(int minTicks, int maxTicks) {
		return (InvalidateMemory<BO, M>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (InvalidateMemory<BO, M>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> startCondition(Predicate<BO> predicate) {
		return (InvalidateMemory<BO, M>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateMemory<BO, M> stopIf(Predicate<BO> predicate) {
		return (InvalidateMemory<BO, M>)super.stopIf(predicate);
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
		return this.memoryRequirements;
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	///
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	@Override
	protected void start(BO entity) {
		final M memory = BrainUtil.getMemory(entity, this.memory);

		if (memory != null && this.invalidateIf.test(entity, memory))
			BrainUtil.clearMemory(entity, this.memory);
	}
	//</editor-fold>
}
