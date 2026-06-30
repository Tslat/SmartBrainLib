package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.SensoryUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Invalidates the current [MemoryModuleType#ATTACK_TARGET] memory if the given conditions are met
///
/// @param <BO> The brain owner entity
public class InvalidateAttackTarget<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3).hasMemory(MemoryModuleType.ATTACK_TARGET).usesMemory(MemoryModuleType.LOOK_TARGET).usesMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

	protected BiPredicate<BO, LivingEntity> targetInvalidIf = (entity, target) -> (entity instanceof Player pl && pl.getAbilities().invulnerable) ||
	                                                                              !SensoryUtil.isInFollowRange(entity, target) ||
	                                                                              !entity.canAttack(target);
	protected long pathfindingAttentionSpan = 200;

	/// Sets a custom predicate to invalidate the attack target if none of the previous checks invalidate it first
	///
	/// @see #targetInvalidIf
	@ApiStatus.NonExtendable
	public InvalidateAttackTarget<BO> invalidateIf(BiPredicate<BO, LivingEntity> predicate) {
		this.targetInvalidIf = predicate;

		return this;
	}

	/// Skips the check to see if the entity has been unable to path to its target for a while
	@ApiStatus.NonExtendable
	public InvalidateAttackTarget<BO> ignoreFailedPathfinding() {
		return stopTryingToPathAfter(0);
	}

	/// Sets the attention span for the brain owner's pathfinding
	///
	/// If the entity has been unable to find a good path to the target after this time, it will invalidate the target
	@ApiStatus.NonExtendable
	public InvalidateAttackTarget<BO> stopTryingToPathAfter(long ticks) {
		this.pathfindingAttentionSpan = ticks;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> whenStarting(Consumer<BO> callback) {
		return (InvalidateAttackTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> whenStopping(Consumer<BO> callback) {
		return (InvalidateAttackTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> runFor(int ticks) {
		return (InvalidateAttackTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> runFor(int minTicks, int maxTicks) {
		return (InvalidateAttackTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (InvalidateAttackTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> noTimeout() {
		return (InvalidateAttackTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> cooldownFor(int ticks) {
		return (InvalidateAttackTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (InvalidateAttackTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (InvalidateAttackTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> startCondition(Predicate<BO> predicate) {
		return (InvalidateAttackTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public InvalidateAttackTarget<BO> stopIf(Predicate<BO> predicate) {
		return (InvalidateAttackTarget<BO>)super.stopIf(predicate);
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
		final LivingEntity target = BrainUtil.getTargetOfEntity(entity);

		if (target == null)
			return;
		
		if (isTargetInvalid(entity, target) || isTiredOfPathing(entity) || this.targetInvalidIf.test(entity, target)) {
			BrainUtil.setTargetOfEntity(entity, null);
			
			if (entity instanceof Mob mob)
				mob.setAggressive(false);
		}
	}

	/// @return Whether the given target entity is objectively invalid and should not be targeted
	protected boolean isTargetInvalid(BO entity, LivingEntity target) {
		return entity.level() != target.level() || target.isDeadOrDying() || target.isRemoved();
	}

	/// @return Whether the given entity has a [pathfinding attention span][#pathfindingAttentionSpan] and hasn't been able to path for that long or more
	protected boolean isTiredOfPathing(BO entity) {
		if (this.pathfindingAttentionSpan <= 0)
			return false;

		final Long lastWalkableTime = BrainUtil.getMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		return lastWalkableTime != null && entity.level().getGameTime() - lastWalkableTime > this.pathfindingAttentionSpan;
	}
	//</editor-fold>
}
