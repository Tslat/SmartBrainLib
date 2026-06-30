package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.function.*;

/// Extended behaviour for attacking with a bow<br/>
/// Natively supports animation hit delays
///
/// @param <BO> The brain owner entity
public class BowAttack<BO extends LivingEntity & RangedAttackMob> extends AnimatableRangedAttack<BO> {
	public BowAttack(int delayTicks) {
		super(delayTicks);
	}

	public BowAttack(ToIntFunction<BO> delayTicks) {
		super(delayTicks);
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the number of ticks between attacks
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> attackInterval(int ticks) {
		return (BowAttack<BO>)super.attackInterval(ticks);
	}

	/// Set the function that determines the time (in ticks) between attacks
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> attackInterval(ToIntBiFunction<BO, @Nullable LivingEntity> supplier) {
		return (BowAttack<BO>)super.attackInterval(supplier);
	}

	/// Set the radius (in blocks) in which the entity can attack
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> attackRadius(float radius) {
		return (BowAttack<BO>)super.attackRadius(radius);
	}

	/// Set the function that determines the radius (in blocks) in which the entity can attack
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> attackRadius(ToFloatBiFunction<BO, LivingEntity> radiusFunction) {
		return (BowAttack<BO>)super.attackRadius(radiusFunction);
	}

	/// Set a custom predicate for whether a target is valid to shoot or not
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> canShootAt(BiPredicate<BO, LivingEntity> predicate) {
		return (BowAttack<BO>)super.canShootAt(predicate);
	}

	/// Set an additional callback to run when the delayed activation is called
	///
	/// @param callback The callback function
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> whenActivating(Consumer<BO> callback) {
		return (BowAttack<BO>)super.whenActivating(callback);
	}

	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> whenStarting(Consumer<BO> callback) {
		return (BowAttack<BO>)super.whenStarting(callback);
	}

	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> whenStopping(Consumer<BO> callback) {
		return (BowAttack<BO>)super.whenStopping(callback);
	}

	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> runFor(int ticks) {
		return (BowAttack<BO>)super.runFor(ticks);
	}

	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> runFor(int minTicks, int maxTicks) {
		return (BowAttack<BO>)super.runFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (BowAttack<BO>)super.runFor(timeProvider);
	}

	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> noTimeout() {
		return (BowAttack<BO>)super.noTimeout();
	}

	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> cooldownFor(int ticks) {
		return (BowAttack<BO>)super.cooldownFor(ticks);
	}

	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> cooldownFor(int minTicks, int maxTicks) {
		return (BowAttack<BO>)super.cooldownFor(minTicks, maxTicks);
	}

	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (BowAttack<BO>)super.cooldownFor(timeProvider);
	}

	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> startCondition(Predicate<BO> predicate) {
		return (BowAttack<BO>)super.startCondition(predicate);
	}

	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public BowAttack<BO> stopIf(Predicate<BO> predicate) {
		return (BowAttack<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	/// 
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method  
	@MustBeInvokedByOverriders
	@Override
	protected void start(BO entity) {
        //noinspection DataFlowIssue
        BehaviorUtils.lookAtEntity(entity, this.target);
		entity.startUsingItem(ProjectileUtil.getWeaponHoldingHand(entity, Items.BOW));
	}

	/// The action to take once the delay period has elapsed
	@Override
	protected void doDelayedAction(BO entity) {
		if (this.target == null || !this.validTarget.test(entity, this.target))
			return;

		entity.performRangedAttack(this.target, BowItem.getPowerForTime(entity.getTicksUsingItem()));
		entity.stopUsingItem();
		BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackInterval.applyAsInt(entity, this.target));
	}

	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		super.stop(entity);
		entity.stopUsingItem();
	}
	//</editor-fold>
}
