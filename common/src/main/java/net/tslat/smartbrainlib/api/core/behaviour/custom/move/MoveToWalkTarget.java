package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Use an existing [MemoryModuleType#WALK_TARGET] memory to tell the entity's navigation to begin pathing if not already pathing
///
/// This behaviour is typically required for all entities that do pathfinding
///
/// @param <BO> The brain owner entity
public class MoveToWalkTarget<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3).hasMemory(MemoryModuleType.WALK_TARGET).noMemory(MemoryModuleType.PATH).usesMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
	
	protected @Nullable Path path;
	protected @Nullable BlockPos lastTargetPos;
	protected float speedModifier;

	public MoveToWalkTarget() {
		runFor(entity -> entity.getRandom().nextInt(100) + 150);
		cooldownFor(entity -> entity.getRandom().nextInt(40));
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> whenStarting(Consumer<BO> callback) {
		return (MoveToWalkTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> whenStopping(Consumer<BO> callback) {
		return (MoveToWalkTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> runFor(int ticks) {
		return (MoveToWalkTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> runFor(int minTicks, int maxTicks) {
		return (MoveToWalkTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (MoveToWalkTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> noTimeout() {
		return (MoveToWalkTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> cooldownFor(int ticks) {
		return (MoveToWalkTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (MoveToWalkTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (MoveToWalkTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> startCondition(Predicate<BO> predicate) {
		return (MoveToWalkTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public MoveToWalkTarget<BO> stopIf(Predicate<BO> predicate) {
		return (MoveToWalkTarget<BO>)super.stopIf(predicate);
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
		final Brain<?> brain = entity.getBrain();
		final WalkTarget walkTarget = BrainUtil.getMemory(brain, MemoryModuleType.WALK_TARGET);
		
		//noinspection DataFlowIssue
		if (!hasReachedTarget(entity, walkTarget) && attemptNewPath(entity, walkTarget, false)) {
			this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();

			return true;
		}

		return false;
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	///
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	@Override
	protected void start(BO entity) {
		BrainUtil.clearMemories(entity, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		
		if (this.path != null)
			startOnNewPath(entity, this.path);
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@MustBeInvokedByOverriders
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		if (this.path == null || this.lastTargetPos == null)
			return false;

		if (entity.getNavigation().isDone())
			return false;

		final WalkTarget walkTarget = BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET);

		return walkTarget != null && !hasReachedTarget(entity, walkTarget);
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [ExtendedBehaviour#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		final Path path = entity.getNavigation().getPath();
		final Brain<?> brain = entity.getBrain();

		if (this.path != path) {
			this.path = path;

			BrainUtil.setOrClearMemory(brain, MemoryModuleType.PATH, path);
		}

		if (path != null && this.lastTargetPos != null) {
			final WalkTarget walkTarget = BrainUtil.getMemory(brain, MemoryModuleType.WALK_TARGET);
			
			//noinspection DataFlowIssue
			if (walkTarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4 && attemptNewPath(entity, walkTarget, hasReachedTarget(entity, walkTarget))) {
				this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();

				if (this.path != null)
					startOnNewPath(entity, this.path);
			}
		}
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		final Brain<?> brain = entity.getBrain();
		final WalkTarget walkTarget = BrainUtil.getMemory(brain, MemoryModuleType.WALK_TARGET);

		if (!entity.getNavigation().isStuck() || walkTarget == null || hasReachedTarget(entity, walkTarget))
			this.cooldownFinishedAt = 0;

		entity.getNavigation().stop();
		BrainUtil.clearMemories(brain, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);

		this.path = null;
	}

	/// Attempt to create and set a new [Path] for the entity to navigate
	///
	/// The actual instruction to start navigating is not handled here
	protected boolean attemptNewPath(BO entity, WalkTarget walkTarget, boolean reachedCurrentTarget) {
		final Brain<?> brain = entity.getBrain();
		final BlockPos targetPos = walkTarget.getTarget().currentBlockPosition();
		this.path = entity.getNavigation().createPath(targetPos, 0);
		this.speedModifier = walkTarget.getSpeedModifier();

		if (reachedCurrentTarget) {
			BrainUtil.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

			return false;
		}

		if (this.path != null && this.path.canReach()) {
			BrainUtil.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		}
		else {
			BrainUtil.setMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, entity.level().getGameTime());
		}

		if (this.path != null)
			return true;

		final Vec3 newTargetPos = DefaultRandomPos.getPosTowards(entity, 10, 7, Vec3.atBottomCenterOf(targetPos), Mth.HALF_PI);

		if (newTargetPos != null) {
			this.path = entity.getNavigation().createPath(newTargetPos.x(), newTargetPos.y(), newTargetPos.z(), 0);

			return this.path != null;
		}

		return false;
	}

	/// @return Whether the entity has come within an acceptable distance of the provided [WalkTarget] to be considered complete
	protected boolean hasReachedTarget(BO entity, WalkTarget target) {
		return target.getTarget().currentBlockPosition().distManhattan(entity.blockPosition()) <= target.getCloseEnoughDist();
	}
	
	/// Instruct the entity's [PathNavigation] to begin walking on the provided [Path]
	///
	/// @return Whether the entity's navigator successfully set the path
	protected boolean startOnNewPath(BO entity, Path path) {
		BrainUtil.setMemory(entity, MemoryModuleType.PATH, path);
		
		return entity.getNavigation().moveTo(path, this.speedModifier);
	}
	//</editor-fold>
}
