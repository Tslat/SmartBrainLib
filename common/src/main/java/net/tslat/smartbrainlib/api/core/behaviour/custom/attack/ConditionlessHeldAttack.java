package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.HeldBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// Attack behaviour that runs over a number of ticks and doesn't require line of sight or proximity to target, or to even have a target at all
/// This is useful for special attacks
///
/// @see #startCondition(Predicate)
/// @see HeldBehaviour#whenTicking(Consumer)
/// @param <BO> The brain owner entity
public class ConditionlessHeldAttack<BO extends LivingEntity> extends HeldBehaviour<BO> {
	protected MemoryTest memoryRequirements = MemoryTest.builder(1).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
	
	protected ToIntBiFunction<BO, @Nullable LivingEntity> attackInterval = (_, _) -> 20;
	protected BiPredicate<BO, @Nullable LivingEntity> attackTickTest = (_, _) -> false;
	protected boolean requireTarget = false;
	
	protected @Nullable LivingEntity target = null;
	
	/// Set that the attack requires that the entity have an attack target set to activate
	@ApiStatus.NonExtendable
	public ConditionlessHeldAttack<BO> requiresTarget() {
		this.requireTarget = true;
		this.memoryRequirements = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
		
		return this;
	}
	
	/// Set the action callback to run when every tick the attack is held for
	///
	/// Return true to continue the attack for the next tick, or false to end it
	@ApiStatus.NonExtendable
	public ConditionlessHeldAttack<BO> tickAttack(BiPredicate<BO, @Nullable LivingEntity> action) {
		this.attackTickTest = action;
		
		return this;
	}
	
	/// Set the number of ticks between attacks
	@ApiStatus.NonExtendable
	public ConditionlessHeldAttack<BO> attackInterval(int ticks) {
		return attackInterval((_, _) -> ticks);
	}
	
	/// Set the function that determines the time (in ticks) between attacks
	@ApiStatus.NonExtendable
	public ConditionlessHeldAttack<BO> attackInterval(ToIntBiFunction<BO, @Nullable LivingEntity> supplier) {
		this.attackInterval = supplier;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Add an additional per-tick callback to be run each time this behaviour ticks
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> whenTicking(Consumer<BO> callback) {
		return (ConditionlessHeldAttack<BO>)super.whenTicking(callback);
	}
	
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> whenStarting(Consumer<BO> callback) {
		return (ConditionlessHeldAttack<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> whenStopping(Consumer<BO> callback) {
		return (ConditionlessHeldAttack<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> runFor(int ticks) {
		return (ConditionlessHeldAttack<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> runFor(int minTicks, int maxTicks) {
		return (ConditionlessHeldAttack<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (ConditionlessHeldAttack<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> noTimeout() {
		return (ConditionlessHeldAttack<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> cooldownFor(int ticks) {
		return (ConditionlessHeldAttack<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> cooldownFor(int minTicks, int maxTicks) {
		return (ConditionlessHeldAttack<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (ConditionlessHeldAttack<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> startCondition(Predicate<BO> predicate) {
		return (ConditionlessHeldAttack<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessHeldAttack<BO> stopIf(Predicate<BO> predicate) {
		return (ConditionlessHeldAttack<BO>)super.stopIf(predicate);
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
		return memoryRequirements;
	}
	
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		if (!this.requireTarget)
			return true;

		this.target = BrainUtil.getTargetOfEntity(entity);

		return this.target != null;
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
		entity.swing(InteractionHand.MAIN_HAND);

		if (this.requireTarget && this.target != null)
			BehaviorUtils.lookAtEntity(entity, this.target);
	}
	
	/// This method is run every tick, for as long as this behaviour is running
	///
	/// Run the intended per-tick behaviour here, then return true to continue for another tick, or false to stop
	@MustBeInvokedByOverriders
	@Override
	public boolean onTick(BO entity) {
		return this.attackTickTest.test(entity, this.target);
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		super.stop(entity);
		
		BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackInterval.applyAsInt(entity, this.target));
		this.target = null;
	}
	
	//</editor-fold>
}
