package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
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

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Sets the attack target of the entity based on the last entity to hurt it if a target isn't already set
///
/// @param <BO> The brian owner entity
public class SetRetaliateTarget<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.HURT_BY_ENTITY).noMemory(MemoryModuleType.ATTACK_TARGET);

	protected BiPredicate<BO, LivingEntity> retaliationPredicate = (_, target) -> (!(target instanceof Player player) || !player.getAbilities().invulnerable);
	protected BiPredicate<BO, LivingEntity> alertAlliesPredicate = (_, _) -> false;
	protected BiPredicate<BO, LivingEntity> allyPredicate = EntityUtil::areAllies;
	protected ToFloatBiFunction<BO, LivingEntity> allyAlertRange = (entity, _) -> entity.getAttributes().hasAttribute(Attributes.FOLLOW_RANGE) ? (float)entity.getAttributeValue(Attributes.FOLLOW_RANGE) : 16f;
	
	protected @Nullable LivingEntity target = null;

	/// Set the predicate to determine whether a given entity is a valid retaliation target
	@ApiStatus.NonExtendable
	public SetRetaliateTarget<BO> canRetaliateAgainst(BiPredicate<BO, LivingEntity> predicate) {
		this.retaliationPredicate = predicate;

		return this;
	}

	/// Set the behaviour to alert nearby allies of the victim when retaliating
	@ApiStatus.NonExtendable
	public SetRetaliateTarget<BO> alertAllies() {
		return alertAlliesIf((_, _) -> true);
	}

	/// Set the predicate to determine whether the brain owner should alert nearby allies of the victim when retaliating
	@ApiStatus.NonExtendable
	public SetRetaliateTarget<BO> alertAlliesIf(BiPredicate<BO, LivingEntity> predicate) {
		this.alertAlliesPredicate = predicate;

		return this;
	}

	/// Set the predicate to determine whether a given entity should be considered an ally for the purpose of [alerting][#alertAlliesIf(BiPredicate)]
	@ApiStatus.NonExtendable
	public SetRetaliateTarget<BO> isAllyIf(BiPredicate<BO, LivingEntity> predicate) {
		this.allyPredicate = predicate;

		return this;
	}
	
	/// Set a function to determine the distance (in blocks) at which allies can be alerted
	@ApiStatus.NonExtendable
	public SetRetaliateTarget<BO> allyAlertRange(float distance) {
		return allyAlertRange((_, _) -> distance);
	}
	
	/// Set a function to determine the distance (in blocks) at which allies can be alerted
	@ApiStatus.NonExtendable
	public SetRetaliateTarget<BO> allyAlertRange(ToFloatBiFunction<BO, LivingEntity> function) {
		this.allyAlertRange = function;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> whenStarting(Consumer<BO> callback) {
		return (SetRetaliateTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> whenStopping(Consumer<BO> callback) {
		return (SetRetaliateTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> runFor(int ticks) {
		return (SetRetaliateTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> runFor(int minTicks, int maxTicks) {
		return (SetRetaliateTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (SetRetaliateTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> noTimeout() {
		return (SetRetaliateTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> cooldownFor(int ticks) {
		return (SetRetaliateTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (SetRetaliateTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (SetRetaliateTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> startCondition(Predicate<BO> predicate) {
		return (SetRetaliateTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public SetRetaliateTarget<BO> stopIf(Predicate<BO> predicate) {
		return (SetRetaliateTarget<BO>)super.stopIf(predicate);
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
		this.target = BrainUtil.getLastAttacker(entity);

		if (this.target != null && this.target.isAlive() && this.target.level() == level && this.retaliationPredicate.test(entity, this.target))
			return true;
		
		this.target = null;
		
		return false;
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
		BrainUtil.setTargetOfEntity(entity, this.target);
		
		//noinspection DataFlowIssue
		if (this.alertAlliesPredicate.test(entity, this.target))
			alertAllies(entity, this.target);
		
		BrainUtil.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		this.target = null;
	}

	/// Alert nearby available ally entities of the new enemy
	protected void alertAllies(BO entity, LivingEntity target) {
		final double followRange = this.allyAlertRange.applyAsFloat(entity, target);

		for (LivingEntity ally : EntityRetrievalUtil.getEntities(entity, followRange, 10, followRange, LivingEntity.class, ally2 -> this.allyPredicate.test(entity, ally2))) {
			BrainUtil.setTargetOfEntity(ally, target);
		}
	}
	//</editor-fold>
}