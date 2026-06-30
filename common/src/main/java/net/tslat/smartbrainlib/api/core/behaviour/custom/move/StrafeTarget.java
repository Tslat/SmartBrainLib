package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Movement behaviour similar to how [AbstractSkeleton]s act that makes them strafe backwards and sideways while moving around an [MemoryModuleType#ATTACK_TARGET]
///
/// @param <BO> The brain owner entity
public class StrafeTarget<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.WALK_TARGET);
	
	protected BiPredicate<BO, LivingEntity> stopStrafingWhen = (_, _) -> false;
	protected ToFloatBiFunction<BO, LivingEntity> speedModifier = (_, _) -> 1;
	protected ToFloatBiFunction<BO, LivingEntity> strafeDistance = (_, _) -> 12;
	
	protected boolean strafingLaterally = false;
	protected boolean strafingBack = false;
	protected int strafeCounter = -1;
	protected int targetingTime = 0;
	
	/// Set a custom predicate to determine if the behaviour should end early
	@ApiStatus.NonExtendable
	public StrafeTarget<BO> stopStrafingWhen(BiPredicate<BO, LivingEntity> predicate) {
		this.stopStrafingWhen = predicate;
		
		return this;
	}
	
	/// Set the distance (in blocks) from the target to attempt to strafe at
	@ApiStatus.NonExtendable
	public StrafeTarget<BO> strafeDistance(float dist) {
		return strafeDistance((_, _) -> dist);
	}
	
	/// Set a function to determine the distance (in blocks) from the target to attempt to strafe at
	@ApiStatus.NonExtendable
	public StrafeTarget<BO> strafeDistance(ToFloatBiFunction<BO, LivingEntity> function) {
		this.strafeDistance = function;
		
		return this;
	}
	
	/// Set the movement speed modifier for strafing
	@ApiStatus.NonExtendable
	public StrafeTarget<BO> speedModifier(float modifier) {
		return speedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for strafing
	@ApiStatus.NonExtendable
	public StrafeTarget<BO> speedModifier(ToFloatBiFunction<BO, LivingEntity> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> whenStarting(Consumer<BO> callback) {
		return (StrafeTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> whenStopping(Consumer<BO> callback) {
		return (StrafeTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> runFor(int ticks) {
		return (StrafeTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> runFor(int minTicks, int maxTicks) {
		return (StrafeTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (StrafeTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> noTimeout() {
		return (StrafeTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> cooldownFor(int ticks) {
		return (StrafeTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (StrafeTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (StrafeTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> startCondition(Predicate<BO> predicate) {
		return (StrafeTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public StrafeTarget<BO> stopIf(Predicate<BO> predicate) {
		return (StrafeTarget<BO>)super.stopIf(predicate);
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
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		final LivingEntity target = BrainUtil.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
		
		return target != null && !this.stopStrafingWhen.test(entity, target);
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [ExtendedBehaviour#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@MustBeInvokedByOverriders
	@Override
	protected void tick(BO entity) {
		final LivingEntity target = BrainUtil.getTargetOfEntity(entity);
		@SuppressWarnings("DataFlowIssue")
		final float distanceToTarget = (float)target.distanceToSqr(entity);
		final float strafeDistance = this.strafeDistance.applyAsFloat(entity, target);
		final boolean canSeeTarget = BrainUtil.canSee(entity, target);
		final boolean couldSeeTarget = this.targetingTime > 0;

		if (canSeeTarget != couldSeeTarget)
			this.targetingTime = 0;

		this.targetingTime += canSeeTarget ? 1 : -1;

		if (distanceToTarget <= Mth.square(strafeDistance) && this.targetingTime >= 20) {
			entity.getNavigation().stop();
			this.strafeCounter++;
		}
		else {
			entity.getNavigation().moveTo(target, this.speedModifier.applyAsFloat(entity, target));
			
			this.strafeCounter = -1;
		}

		if (this.strafeCounter >= 20) {
			if (entity.getRandom().nextFloat() < 0.3)
				this.strafingLaterally = !this.strafingLaterally;

			if (entity.getRandom().nextFloat() < 0.3)
				this.strafingBack = !this.strafingBack;

			this.strafeCounter = 0;
		}

		if (this.strafeCounter > -1) {
			if (distanceToTarget > Mth.square(strafeDistance * 0.75f)) {
				this.strafingBack = false;
			}
			else if (distanceToTarget < Mth.square(strafeDistance * 0.25f)) {
				this.strafingBack = true;
			}

			entity.lookAt(target, 30, 30);
			entity.getMoveControl().strafe(this.strafingBack ? -0.5f : 0.5f, this.strafingLaterally ? 0.5f : -0.5f);
		}
	}
	//</editor-fold>
}
