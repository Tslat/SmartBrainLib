package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.*;

/// Extended behaviour for attacking with melee<br/>
/// Natively supports animation hit delays
///
/// @param <BO> The brain owner entity
public class AnimatableMeleeAttack<BO extends Mob> extends DelayedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);

	protected ToIntBiFunction<BO, @Nullable LivingEntity> attackInterval = (_, _) -> 20;
	protected BiPredicate<BO, LivingEntity> validTarget = (entity, target) -> target.isAlive() && BrainUtil.canSee(entity, target) && entity.isWithinMeleeAttackRange(target);

	protected @Nullable LivingEntity target = null;

	public AnimatableMeleeAttack(int delayTicks) {
		super(delayTicks);
	}

	public AnimatableMeleeAttack(ToIntFunction<BO> delayTicks) {
		super(delayTicks);
	}

	/// Set the number of ticks between attacks
	@ApiStatus.NonExtendable
	public AnimatableMeleeAttack<BO> attackInterval(int ticks) {
		return attackInterval((_, _) -> ticks);
	}

	/// Set the function that determines the time (in ticks) between attacks
	@ApiStatus.NonExtendable
	public AnimatableMeleeAttack<BO> attackInterval(ToIntBiFunction<BO, @Nullable LivingEntity> supplier) {
		this.attackInterval = supplier;

		return this;
	}
	
	/// Set a custom predicate for whether a target is valid to attack or not
	@ApiStatus.NonExtendable
	public AnimatableMeleeAttack<BO> canAttack(BiPredicate<BO, LivingEntity> predicate) {
		this.validTarget = predicate;
		
		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set an additional callback to run when the delayed activation is called
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> whenActivating(Consumer<BO> callback) {
		return (AnimatableMeleeAttack<BO>)super.whenActivating(callback);
	}

	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> whenStarting(Consumer<BO> callback) {
		return (AnimatableMeleeAttack<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> whenStopping(Consumer<BO> callback) {
		return (AnimatableMeleeAttack<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> runFor(int ticks) {
		return (AnimatableMeleeAttack<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> runFor(int minTicks, int maxTicks) {
		return (AnimatableMeleeAttack<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (AnimatableMeleeAttack<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> noTimeout() {
		return (AnimatableMeleeAttack<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> cooldownFor(int ticks) {
		return (AnimatableMeleeAttack<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> cooldownFor(int minTicks, int maxTicks) {
		return (AnimatableMeleeAttack<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (AnimatableMeleeAttack<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> startCondition(Predicate<BO> predicate) {
		return (AnimatableMeleeAttack<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public AnimatableMeleeAttack<BO> stopIf(Predicate<BO> predicate) {
		return (AnimatableMeleeAttack<BO>)super.stopIf(predicate);
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
		return MEMORY_REQUIREMENTS;
	}

	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		this.target = BrainUtil.getTargetOfEntity(entity);

		return this.target != null && this.validTarget.test(entity, this.target);
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
		//noinspection DataFlowIssue
		BehaviorUtils.lookAtEntity(entity, this.target);
	}

	/// The action to take once the delay period has elapsed
	@Override
	protected void doDelayedAction(BO entity) {
		if (this.target == null || !this.validTarget.test(entity, this.target))
			return;

		entity.doHurtTarget((ServerLevel)entity.level(), this.target);
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackInterval.applyAsInt(entity, this.target));
		this.target = null;
	}
	//</editor-fold>
}
