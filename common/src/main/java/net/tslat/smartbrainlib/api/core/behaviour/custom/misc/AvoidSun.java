package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.EscapeSun;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Tell the entity's [GroundPathNavigation] that it should avoid pathing into the sun<br/>
/// This is not the same as actively pathing away from the sun. [EscapeSun] should be used for that
///
/// Only applies to entities that use `GroundPathNavigation`
///
/// @see FleeSunGoal
/// @param <BO> The brain owner entity
public class AvoidSun<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected Predicate<BO> shouldAvoid = entity -> entity.level().isBrightOutside() && entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
	
	/// Set the predicate that determines whether the entity should avoid the sun or not
	@ApiStatus.NonExtendable
	public AvoidSun<BO> avoidSunIf(Predicate<BO> predicate) {
		this.shouldAvoid = predicate;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> whenStarting(Consumer<BO> callback) {
		return (AvoidSun<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> whenStopping(Consumer<BO> callback) {
		return (AvoidSun<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> runFor(int ticks) {
		return (AvoidSun<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> runFor(int minTicks, int maxTicks) {
		return (AvoidSun<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (AvoidSun<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> noTimeout() {
		return (AvoidSun<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> cooldownFor(int ticks) {
		return (AvoidSun<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> cooldownFor(int minTicks, int maxTicks) {
		return (AvoidSun<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (AvoidSun<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> startCondition(Predicate<BO> predicate) {
		return (AvoidSun<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public AvoidSun<BO> stopIf(Predicate<BO> predicate) {
		return (AvoidSun<BO>)super.stopIf(predicate);
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
	
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		return entity.getNavigation() instanceof GroundPathNavigation && this.shouldAvoid.test(entity);
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return this.shouldAvoid.test(entity);
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
		((GroundPathNavigation)entity.getNavigation()).setAvoidSun(true);
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		if (entity.getNavigation() instanceof GroundPathNavigation groundNavigation)
			groundNavigation.setAvoidSun(false);
	}
	//</editor-fold>
}
