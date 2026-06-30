package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.util.EntityUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.*;

/// Sets the attack target of the entity, utilising a few sources of targets<br/>
/// In order:
/// <ol>
///   <li>The [MemoryModuleType#NEAREST_ATTACKABLE] memory value</li>
///   <li>The [MemoryModuleType#HURT_BY_ENTITY] memory value</li>
///   <li>The closest applicable entity from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory value</li>
/// </ol>
///
/// @param <BO> The brain owner entity
public class TargetOrRetaliate<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(4).usesMemories(MemoryModuleType.ATTACK_TARGET, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

	protected Predicate<LivingEntity> retaliationPredicate = entity -> entity.isAlive() && (!(entity instanceof Player player) || (!player.getAbilities().invulnerable && entity.level().getDifficulty() != Difficulty.PEACEFUL));
	protected BiPredicate<BO, Entity> alertAlliesPredicate = (_, _) -> false;
	protected BiPredicate<BO, LivingEntity> alertableAllyPredicate = (entity, ally) -> EntityUtil.areAllies(entity, ally) && BrainUtil.getTargetOfEntity(ally) == null;
	protected ToFloatBiFunction<BO, LivingEntity> allyAlertRange = (entity, _) -> entity.getAttributes().hasAttribute(Attributes.FOLLOW_RANGE) ? (float)entity.getAttributeValue(Attributes.FOLLOW_RANGE) : 16f;
	protected MemoryModuleType<? extends LivingEntity> priorityTargetMemory = MemoryModuleType.NEAREST_ATTACKABLE;
	protected ToIntBiFunction<BO, LivingEntity> targetSwapCooldownTicks = (_, _) -> 100;
	protected boolean canSwapTarget = true;
	
	protected @Nullable LivingEntity target = null;
	protected long lastTargetSwap = 0;
	
	/// Set the predicate to determine whether a given entity is a valid retaliation target
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> canRetaliateAgainst(Predicate<LivingEntity> predicate) {
		this.retaliationPredicate = predicate;

		return this;
	}

	/// Set the memory type that is checked first to target an entity
	///
	/// Useful for switching to player-only targeting
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> useMemory(MemoryModuleType<? extends LivingEntity> memory) {
		this.priorityTargetMemory = memory;

		return this;
	}
	
	/// Set the behaviour to alert nearby allies of the victim when retaliating
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> alertAllies() {
		return alertAlliesIf((_, _) -> true);
	}
	
	/// Set the predicate to determine whether the brain owner should alert nearby allies of the victim when retaliating
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> alertAlliesIf(BiPredicate<BO, Entity> predicate) {
		this.alertAlliesPredicate = predicate;

		return this;
	}
	
	/// Set the predicate to determine whether a given entity should be considered an ally for the purpose of [alerting][#alertAlliesIf(BiPredicate)]
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> alertPotentialAllyIf(BiPredicate<BO, LivingEntity> predicate) {
		this.alertableAllyPredicate = predicate;
		
		return this;
	}
	
	/// Set a function to determine the distance (in blocks) at which allies can be alerted
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> allyAlertRange(float distance) {
		return allyAlertRange((_, _) -> distance);
	}
	
	/// Set a function to determine the distance (in blocks) at which allies can be alerted
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> allyAlertRange(ToFloatBiFunction<BO, LivingEntity> function) {
		this.allyAlertRange = function;
		
		return this;
	}

	/// Disable the ability to occasionally swap targets if a more recent retaliation target source has one
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> noTargetSwapping() {
		this.canSwapTarget = false;

		return this;
	}
	
	/// Set the number of ticks to prevent swapping targets after having previously swapped
	///
	/// Has no effect if [#noTargetSwapping()] is used
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> targetSwapCooldown(int ticks) {
		return targetSwapCooldown((_, _) -> ticks);
	}

	/// Set a function to determine the number of ticks to prevent swapping targets after having previously swapped
	///
	/// Has no effect if [#noTargetSwapping()] is used
	@ApiStatus.NonExtendable
	public TargetOrRetaliate<BO> targetSwapCooldown(ToIntBiFunction<BO, LivingEntity> function) {
		this.targetSwapCooldownTicks = function;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> whenStarting(Consumer<BO> callback) {
		return (TargetOrRetaliate<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> whenStopping(Consumer<BO> callback) {
		return (TargetOrRetaliate<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> runFor(int ticks) {
		return (TargetOrRetaliate<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> runFor(int minTicks, int maxTicks) {
		return (TargetOrRetaliate<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (TargetOrRetaliate<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> noTimeout() {
		return (TargetOrRetaliate<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> cooldownFor(int ticks) {
		return (TargetOrRetaliate<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> cooldownFor(int minTicks, int maxTicks) {
		return (TargetOrRetaliate<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (TargetOrRetaliate<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> startCondition(Predicate<BO> predicate) {
		return (TargetOrRetaliate<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public TargetOrRetaliate<BO> stopIf(Predicate<BO> predicate) {
		return (TargetOrRetaliate<BO>)super.stopIf(predicate);
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
		if (BrainUtil.hasMemory(entity, MemoryModuleType.ATTACK_TARGET)) {
			if (!this.canSwapTarget || (this.target = getTarget(entity, BrainUtil.getTargetOfEntity(entity))) == null)
				return false;
			
			final int swapDelay = this.targetSwapCooldownTicks.applyAsInt(entity, this.target);
			
			if (this.lastTargetSwap + swapDelay > entity.tickCount)
				return false;
		}
		
		if (this.target == null)
			this.target = getTarget(entity, BrainUtil.getTargetOfEntity(entity));
		
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
		final LivingEntity prevTarget = BrainUtil.getTargetOfEntity(entity);

		BrainUtil.setTargetOfEntity(entity, this.target);
		BrainUtil.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		
		if (entity instanceof Mob mob)
			mob.setAggressive(this.target != null);
		
		//noinspection DataFlowIssue
		if (prevTarget == null && this.alertAlliesPredicate.test(entity, this.target))
			alertAllies(entity);

		this.target = null;
		
		if (prevTarget != null)
			this.lastTargetSwap = entity.tickCount;
	}
	
	/// Alert nearby available ally entities of the new enemy
	protected void alertAllies(BO entity) {
		final double followRange = entity.getAttributeValue(Attributes.FOLLOW_RANGE);

		for (LivingEntity ally : EntityRetrievalUtil.getEntities(entity, followRange, 10, followRange, LivingEntity.class, ally -> this.alertableAllyPredicate.test(entity, ally))) {
			BrainUtil.setTargetOfEntity(ally, this.target);
		}
	}
	
	/// Get the target to set from the various [MemoryModuleType]s sources
	@Nullable
	protected LivingEntity getTarget(BO owner, @Nullable LivingEntity existingTarget) {
		final Brain<?> brain = owner.getBrain();
		LivingEntity newTarget = BrainUtil.getMemory(brain, this.priorityTargetMemory);
		
		if (newTarget == null)
			newTarget = BrainUtil.getMemory(brain, MemoryModuleType.HURT_BY_ENTITY);
		
		if (newTarget == null) {
			newTarget = BrainUtil.memoryOrDefault(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty())
			                     .findClosest(this.retaliationPredicate).orElse(null);
		}
		
		if (newTarget == null || newTarget == existingTarget)
			return null;
		
		return this.retaliationPredicate.test(newTarget) ? newTarget : null;
	}
	//</editor-fold>
}