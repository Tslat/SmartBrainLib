package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Sets the [attack target][MemoryModuleType#ATTACK_TARGET] of the entity if one is available from [MemoryModuleType#NEAREST_ATTACKABLE]
///
/// @see StartAttacking
/// @param <BO> The brain owner entity
public class SetAttackTarget<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.NEAREST_ATTACKABLE).noMemory(MemoryModuleType.ATTACK_TARGET).usesMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

	protected BiPredicate<BO, LivingEntity> canAttackPredicate = (_, _) -> true;

	/// Add a custom [Predicate] to determine if a target is valid or not
	///
	/// Typically, your attack target would already be filtered from prior memory storage behaviours/sensors
	public SetAttackTarget<BO> canAttack(BiPredicate<BO, LivingEntity> predicate) {
		this.canAttackPredicate = predicate;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> whenStarting(Consumer<BO> callback) {
		return (SetAttackTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> whenStopping(Consumer<BO> callback) {
		return (SetAttackTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> runFor(int ticks) {
		return (SetAttackTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> runFor(int minTicks, int maxTicks) {
		return (SetAttackTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (SetAttackTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> noTimeout() {
		return (SetAttackTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> cooldownFor(int ticks) {
		return (SetAttackTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (SetAttackTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (SetAttackTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> startCondition(Predicate<BO> predicate) {
		return (SetAttackTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public SetAttackTarget<BO> stopIf(Predicate<BO> predicate) {
		return (SetAttackTarget<BO>)super.stopIf(predicate);
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
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	/// 
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method  
	@MustBeInvokedByOverriders
	@Override
	protected void start(BO entity) {
		LivingEntity target = getTarget(entity);
		
		if (target != null && !this.canAttackPredicate.test(entity, target))
			target = null;

		BrainUtil.setOrClearMemory(entity, MemoryModuleType.ATTACK_TARGET, target);
		
		if (entity instanceof Mob mob)
			mob.setAggressive(target != null);
		
		if (target != null)
			BrainUtil.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
	}
	
	/// Get the target entity to set, or null to clear the target memory
	protected @Nullable LivingEntity getTarget(BO entity) {
		return BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE);
	}
	//</editor-fold>
}
