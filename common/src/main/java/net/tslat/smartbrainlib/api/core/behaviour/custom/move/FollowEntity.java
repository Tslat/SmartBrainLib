package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.interfaces.TriPredicate;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// A movement behaviour for automatically following a [nearby visible entity][MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES]
///
/// This may also include teleporting to the target entity when too far
///
/// @param <BO> The brain owner entity
public class FollowEntity<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_CONDITIONS = MemoryTest.builder(1).usesMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
	
	protected final BiPredicate<BO, LivingEntity> shouldFollow;
	
	protected ToFloatBiFunction<BO, LivingEntity> speedModifier = (_, _) -> 1;
	protected ToFloatBiFunction<BO, LivingEntity> closeEnoughDist = (_, _) -> 4f;
	protected ToFloatBiFunction<BO, LivingEntity> startFollowingAfter = (_, _) -> 8f;
	protected ToFloatBiFunction<BO, LivingEntity> teleportAfterDist = (_, _) -> Float.MAX_VALUE;
	protected BiPredicate<BO, LivingEntity> canTeleportOffGround = (entity, _) -> entity.getNavigation().getNodeEvaluator() instanceof SwimNodeEvaluator || entity.getNavigation().getNodeEvaluator() instanceof FlyNodeEvaluator;
	protected TriPredicate<BO, LivingEntity, BlockInWorld> canTeleportTo = this::checkTeleportDestination;

	protected @Nullable LivingEntity followingEntity = null;
	protected @Nullable WalkTarget walkTarget = null;
	protected @Nullable PositionTracker lookTarget = null;
	protected float oldWaterPathMalus = 0;
	protected float oldLavaPathMalus = 0;
	
	public FollowEntity(BiPredicate<BO, LivingEntity> shouldFollow) {
		this.shouldFollow = shouldFollow;
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public FollowEntity<BO> speedModifier(float modifier) {
		return speedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public FollowEntity<BO> speedModifier(ToFloatBiFunction<BO, LivingEntity> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Set the distance (in blocks) from the followed entity to path to when following
	@ApiStatus.NonExtendable
	public FollowEntity<BO> closeEnoughDist(float dist) {
		return closeEnoughDist((_, _) -> dist);
	}
	
	/// Set a function to determine the distance (in blocks) from the followed entity to path to when following
	@ApiStatus.NonExtendable
	public FollowEntity<BO> closeEnoughDist(ToFloatBiFunction<BO, LivingEntity> function) {
		this.closeEnoughDist = function;
		
		return this;
	}
	
	/// Set the distance (in blocks) from the followed entity that the entity feels compelled to move closer
	@ApiStatus.NonExtendable
	public FollowEntity<BO> startFollowingAfter(float dist) {
		return startFollowingAfter((_, _) -> dist);
	}
	
	/// Set a function to determine the distance (in blocks) from the followed entity that the entity feels compelled to move closer
	@ApiStatus.NonExtendable
	public FollowEntity<BO> startFollowingAfter(ToFloatBiFunction<BO, LivingEntity> function) {
		this.startFollowingAfter = function;
		
		return this;
	}
	
	/// Set the distance (in blocks) from the followed entity that the entity tries to teleport to catch up
	@ApiStatus.NonExtendable
	public FollowEntity<BO> teleportAfterDist(float dist) {
		return teleportAfterDist((_, _) -> dist);
	}
	
	/// Set a function to determine the distance (in blocks) from the followed entity that the entity tries to teleport to catch up
	@ApiStatus.NonExtendable
	public FollowEntity<BO> teleportAfterDist(ToFloatBiFunction<BO, LivingEntity> function) {
		this.teleportAfterDist = function;
		
		return this;
	}
	
	/// Set the entity's teleportation behaviour to default to true at all times
	///
	/// Has no effect if [#teleportAfterDist(ToFloatBiFunction)] has not been set
	@ApiStatus.NonExtendable
	public FollowEntity<BO> canTeleportMidair() {
		return canTeleportMidairWhen((_, _) -> true);
	}
	
	/// Set a function to determine whether, when teleporting, the target location can be in midair (or mid-fluid)
	///
	/// Typically used for flying or swimming entities
	///
	/// Has no effect if [#teleportAfterDist(ToFloatBiFunction)] has not been set
	@ApiStatus.NonExtendable
	public FollowEntity<BO> canTeleportMidairWhen(BiPredicate<BO, LivingEntity> predicate) {
		this.canTeleportOffGround = predicate;
		
		return this;
	}
	
	/// Override the predicate that determines a valid teleport target location
	///
	/// Has no effect if [#teleportAfterDist(ToFloatBiFunction)] has not been set
	@ApiStatus.NonExtendable
	public FollowEntity<BO> canTeleportTo(TriPredicate<BO, LivingEntity, BlockInWorld> predicate) {
		this.canTeleportTo = predicate;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> whenStarting(Consumer<BO> callback) {
		return (FollowEntity<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> whenStopping(Consumer<BO> callback) {
		return (FollowEntity<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> runFor(int ticks) {
		return (FollowEntity<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> runFor(int minTicks, int maxTicks) {
		return (FollowEntity<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (FollowEntity<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> noTimeout() {
		return (FollowEntity<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> cooldownFor(int ticks) {
		return (FollowEntity<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> cooldownFor(int minTicks, int maxTicks) {
		return (FollowEntity<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (FollowEntity<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> startCondition(Predicate<BO> predicate) {
		return (FollowEntity<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public FollowEntity<BO> stopIf(Predicate<BO> predicate) {
		return (FollowEntity<BO>)super.stopIf(predicate);
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
		return MEMORY_CONDITIONS;
	}
	
	/// Get the entity to follow, or null if a target entity is not available
	protected @Nullable LivingEntity getFollowingEntity(BO entity) {
		return BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty())
		                .findClosest(nearby -> this.shouldFollow.test(entity, nearby))
		                .orElse(null);
	}
	
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		this.followingEntity = getFollowingEntity(entity);
		
		if (this.followingEntity == null || this.followingEntity.isSpectator())
			return false;
		
		return !entity.closerThan(this.followingEntity, this.startFollowingAfter.applyAsFloat(entity, this.followingEntity));
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
		this.oldWaterPathMalus = entity.getPathfindingMalus(PathType.WATER);
		
		entity.setPathfindingMalus(PathType.WATER, 0);

		if (entity.fireImmune()) {
			this.oldLavaPathMalus = entity.getPathfindingMalus(PathType.LAVA);

			entity.setPathfindingMalus(PathType.LAVA, 0);
		}
		
		//noinspection DataFlowIssue
		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
		                    this.walkTarget = new WalkTarget(this.followingEntity, this.speedModifier.applyAsFloat(entity, this.followingEntity), Mth.ceil(this.closeEnoughDist.applyAsFloat(entity, this.followingEntity))));
		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET,
		                    this.lookTarget = new EntityTracker(this.followingEntity, true));
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		if (this.followingEntity == null || this.followingEntity.isDeadOrDying() || this.followingEntity.isSpectator())
			return false;
		
		return !entity.closerThan(this.followingEntity, this.closeEnoughDist.applyAsFloat(entity, this.followingEntity));
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [ExtendedBehaviour#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		//noinspection DataFlowIssue
		if (!entity.closerThan(this.followingEntity, this.teleportAfterDist.applyAsFloat(entity, this.followingEntity)))
			teleportToTarget(entity, this.followingEntity);
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		this.followingEntity = null;
		this.oldWaterPathMalus = 0;
		this.oldLavaPathMalus = 0;
		
		entity.setPathfindingMalus(PathType.WATER, this.oldWaterPathMalus);

		if (entity.fireImmune())
			entity.setPathfindingMalus(PathType.LAVA, this.oldLavaPathMalus);

		if (BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET) == this.walkTarget)
			BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);

		if (BrainUtil.getMemory(entity, MemoryModuleType.LOOK_TARGET) == this.lookTarget)
			BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
	}

	/// Attempt to teleport to a random safe position near the target entity
	///
	/// @param entity The entity to teleport
	/// @param target The target entity to teleport around
	protected void teleportToTarget(BO entity, LivingEntity target) {
		final BlockPos targetPos = target.blockPosition();
		final BlockPos teleportPos = getTeleportPos(entity, target, targetPos);

		if (!teleportPos.equals(targetPos)) {
			entity.snapTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5, entity.getYRot(), entity.getXRot());
			entity.getNavigation().stop();
			BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
	}

	/// Attempt to locate a safe teleport position for the given entity around the target
	///
	/// Failing to find a safe location should return the <code>targetPos</code> parameter instead
	///
	/// @param entity The entity to teleport
	/// @param target The target entity to teleport around
	/// @param targetPos The block position that the entity is teleporting from
	/// @return A safe teleport position, or the provided <code>targetPos</code> arg if not found
	protected BlockPos getTeleportPos(BO entity, LivingEntity target, BlockPos targetPos) {
		final Level level = entity.level();

		return RandomUtil.positionWithinRange(targetPos, 5, 5, 5, 1, 1, 1, !this.canTeleportOffGround.test(entity, target), level, 10, (state, statePos) ->
				this.canTeleportTo.test(entity, target, new BlockInWorld(level, statePos, false)));
	}

	/// Determine whether the given teleport target location is suitable to teleport to
	protected boolean checkTeleportDestination(BO entity, LivingEntity following, BlockInWorld block) {
		final BlockPos pos = block.getPos();
		final PathType pathType = entity.getNavigation().getNodeEvaluator().getPathType(new PathfindingContext(entity.level(), entity), pos.getX(), pos.getY(), pos.getZ());

		if (!this.canTeleportOffGround.test(entity, following)) {
			if (pathType != PathType.WALKABLE)
				return false;
		}
		else if (pathType != PathType.OPEN && pathType != PathType.WALKABLE) {
			return false;
		}

		return entity.level().noCollision(entity, entity.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(entity.position())));
	}
	//</editor-fold>
}