package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// Attack behaviour that doesn't require line of sight or proximity to target, or to even have a target at all. This is useful for special attacks<br/>
/// Natively supports animation hit delays
///
/// @see #startCondition(Predicate)
/// @see DelayedBehaviour#doDelayedAction(LivingEntity)
/// @param <BO> The brain owner entity
public class ConditionlessAttack<BO extends LivingEntity> extends DelayedBehaviour<BO> {
	protected MemoryTest memoryRequirements = MemoryTest.builder(1).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);

	protected ToIntBiFunction<BO, @Nullable LivingEntity> attackInterval = (_, _) -> 20;
	protected BiConsumer<BO, @Nullable LivingEntity> attackCallback = (_, _) -> {};
	protected boolean requireTarget = false;

	protected @Nullable LivingEntity target = null;

	public ConditionlessAttack(int delayTicks) {
		super(delayTicks);
	}

	/// Set the action callback to run when the attack is taking place
	@ApiStatus.NonExtendable
	public ConditionlessAttack<BO> attack(BiConsumer<BO, @Nullable LivingEntity> action) {
		this.attackCallback = action;

		return this;
	}

	/// Set the number of ticks between attacks
	@ApiStatus.NonExtendable
	public ConditionlessAttack<BO> attackInterval(int ticks) {
		return attackInterval((_, _) -> ticks);
	}

	/// Set the function that determines the time (in ticks) between attacks
	@ApiStatus.NonExtendable
	public ConditionlessAttack<BO> attackInterval(ToIntBiFunction<BO, @Nullable LivingEntity> supplier) {
		this.attackInterval = supplier;

		return this;
	}

	/// Set that the attack requires that the entity have an attack target set to activate
	@ApiStatus.NonExtendable
	public ConditionlessAttack<BO> requiresTarget() {
		this.requireTarget = true;
		this.memoryRequirements = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set an additional callback to run when the delayed activation is called
	///
	/// @param callback The callback function
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> whenActivating(Consumer<BO> callback) {
		return (ConditionlessAttack<BO>)super.whenActivating(callback);
	}

	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> whenStarting(Consumer<BO> callback) {
		return (ConditionlessAttack<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> whenStopping(Consumer<BO> callback) {
		return (ConditionlessAttack<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> runFor(int ticks) {
		return (ConditionlessAttack<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> runFor(int minTicks, int maxTicks) {
		return (ConditionlessAttack<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (ConditionlessAttack<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> noTimeout() {
		return (ConditionlessAttack<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> cooldownFor(int ticks) {
		return (ConditionlessAttack<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> cooldownFor(int minTicks, int maxTicks) {
		return (ConditionlessAttack<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (ConditionlessAttack<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> startCondition(Predicate<BO> predicate) {
		return (ConditionlessAttack<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public ConditionlessAttack<BO> stopIf(Predicate<BO> predicate) {
		return (ConditionlessAttack<BO>)super.stopIf(predicate);
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
			this.attackCallback.accept(entity, BrainUtil.getTargetOfEntity(entity));
		}
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
			this.attackCallback.accept(entity, BrainUtil.getTargetOfEntity(entity));
		}
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
