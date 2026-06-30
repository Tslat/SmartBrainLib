package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Movement behaviour to handle proximal strafing - Moving towards or away from the target to keep a rough distance
///
/// @param <BO> The brain owner entity
public class StayWithinDistanceOfAttackTarget<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.WALK_TARGET);

	protected ToFloatBiFunction<BO, LivingEntity> distMax = (_, _) -> 20f;
	protected ToFloatBiFunction<BO, LivingEntity> minDist = (_, _) -> 5f;
	protected BiPredicate<BO, LivingEntity> stopWhen = (_, _) -> false;
	protected ToFloatBiFunction<BO, LivingEntity> strafeSpeedModifier = (_, _) -> 1f;
	protected ToFloatBiFunction<BO, LivingEntity> repositionSpeedModifier = (_, _) -> 1.3f;
	
	/// Set a custom predicate to determine if the behaviour should end early
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> stopStrafingWhen(BiPredicate<BO, LivingEntity> predicate) {
		this.stopWhen = predicate;
		
		return this;
	}
	
	/// Set the movement speed modifier for strafing to remain between the minimum and maximum distances
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> strafeSpeedModifier(float modifier) {
		return strafeSpeedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for strafing to remain between the minimum and maximum distances
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> strafeSpeedModifier(ToFloatBiFunction<BO, LivingEntity> function) {
		this.strafeSpeedModifier = function;
		
		return this;
	}
	
	/// Set the movement speed modifier for when the entity is too far outside the acceptable distance range and needs to reposition
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> repositionSpeedModifier(float modifier) {
		return repositionSpeedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for when the entity is too far outside the acceptable distance range and needs to reposition
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> repositionSpeedModifier(ToFloatBiFunction<BO, LivingEntity> function) {
		this.repositionSpeedModifier = function;
		
		return this;
	}

	/// Set the approximate distance (in blocks) that should be considered too close to the target
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> tooCloseAt(float distance) {
		return tooCloseAt((_, _) -> distance);
	}
	
	/// Set a function to determine the approximate distance (in blocks) that should be considered too close to the target
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> tooCloseAt(ToFloatBiFunction<BO, LivingEntity> distance) {
		this.minDist = distance;

		return this;
	}

	/// Set the approximate distance (in blocks) that should be considered too far from the target
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> tooFarAt(float distance) {
		return tooFarAt((_, _) -> distance);
	}
	
	/// Set a function to determine the approximate distance (in blocks) that should be considered too far from the target
	@ApiStatus.NonExtendable
	public StayWithinDistanceOfAttackTarget<BO> tooFarAt(ToFloatBiFunction<BO, LivingEntity> distance) {
		this.distMax = distance;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> whenStarting(Consumer<BO> callback) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> whenStopping(Consumer<BO> callback) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> runFor(int ticks) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> runFor(int minTicks, int maxTicks) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> noTimeout() {
		return (StayWithinDistanceOfAttackTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> cooldownFor(int ticks) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> startCondition(Predicate<BO> predicate) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public StayWithinDistanceOfAttackTarget<BO> stopIf(Predicate<BO> predicate) {
		return (StayWithinDistanceOfAttackTarget<BO>)super.stopIf(predicate);
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
	/// This is checked before [#tick(net.minecraft.world.entity.PathfinderMob)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		final LivingEntity target = BrainUtil.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
		
		return target != null && !this.stopWhen.test(entity, target);
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [#shouldKeepRunning(PathfinderMob)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		final LivingEntity target = BrainUtil.getTargetOfEntity(entity);
		@SuppressWarnings("DataFlowIssue")
		final float distToTargetSqr = (float)target.distanceToSqr(entity);
		final float maxDist = this.distMax.applyAsFloat(entity, target);
		final double minDist = this.minDist.applyAsFloat(entity, target);
		final PathNavigation navigation = entity.getNavigation();

		if (distToTargetSqr > Mth.square(maxDist) || !entity.hasLineOfSight(target)) {
			if (navigation.isDone())
				navigation.moveTo(target, this.repositionSpeedModifier.applyAsFloat(entity, target));

			return;
		}

		if (distToTargetSqr < Mth.square(minDist)) {
			if (navigation.isDone()) {
				Vec3 runPos = DefaultRandomPos.getPosAway(entity, Mth.ceil(maxDist), 5, target.position());

				if (runPos != null)
					navigation.moveTo(navigation.createPath(BlockPos.containing(runPos), 1), this.repositionSpeedModifier.applyAsFloat(entity, target));
			}

			return;
		}

		if (navigation instanceof GroundPathNavigation)
			navigation.stop();

		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));

		if (distToTargetSqr > Mth.square(maxDist * 0.5f)) {
			entity.lookAt(target, 30, 30);
			entity.getMoveControl().strafe(0.5f * this.strafeSpeedModifier.applyAsFloat(entity, target), 0);
		}
		else if (distToTargetSqr < Mth.square(minDist * 3f)) {
			entity.lookAt(target, 30, 30);
			entity.getMoveControl().strafe(-0.5f * this.strafeSpeedModifier.applyAsFloat(entity, target), 0);
		}
	}
	//</editor-fold>
}
