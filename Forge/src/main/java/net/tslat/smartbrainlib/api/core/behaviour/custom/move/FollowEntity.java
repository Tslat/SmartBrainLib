package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.object.TriPredicate;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.RandomUtil;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A movement behaviour for automatically following a given entity.<br>
 * Defaults:
 * <ul>
 *     <li>Will stop attempting to move closer than 2 blocks from the target</li>
 *     <li>Won't attempt to follow until the target is more than 10 blocks away</li>
 *     <li>Won't teleport to the target if it gets too far away</li>
 *     <li>1x movespeed modifier for following</li>
 * </ul>
 * @param <E> The owner of the brain
 * @param <T> The minimum common class of the entity expected to be following
 */
public class FollowEntity<E extends PathfinderMob, T extends Entity> extends ExtendedBehaviour<E> {
	protected Function<E, T> followingEntityProvider = entity -> null;

	protected ToFloatBiFunction<E, T> teleportDistance = (entity, target) -> Float.MAX_VALUE;
	protected ToFloatBiFunction<E, T> followDistMin = (entity, target) -> 4f;
	protected ToFloatBiFunction<E, T> speedMod = (entity, target) -> 1f;
	protected TriPredicate<E, BlockPos, BlockState> teleportPredicate = this::isTeleportable;
	protected Predicate<E> canTeleportOffGround = entity -> entity.getNavigation().getNodeEvaluator() instanceof SwimNodeEvaluator || entity.getNavigation().getNodeEvaluator() instanceof FlyNodeEvaluator;

	protected float oldWaterPathMalus = 0;
	protected float oldLavaPathMalus = 0;

	/**
	 * Determines the entity that the brain owner should follow.
	 * @param following The function that provides the entity to follow
	 * @return this
	 */
	public FollowEntity<E, T> following(Function<E, T> following) {
		this.followingEntityProvider = following;

		return this;
	}

	/**
	 * Determines the distance (in blocks) after which the entity should just attempt to teleport closer to the target entity
	 * @param distance The function to provide the distance to teleport after
	 * @return this
	 */
	public FollowEntity<E, T> teleportToTargetAfter(float distance) {
		return teleportToTargetAfter((entity, target) -> distance);
	}

	/**
	 * Determines the distance (in blocks) after which the entity should just attempt to teleport closer to the target entity
	 * @param distanceProvider The function to provide the distance to teleport after
	 * @return this
	 */
	public FollowEntity<E, T> teleportToTargetAfter(ToFloatBiFunction<E, T> distanceProvider) {
		this.teleportDistance = distanceProvider;

		return this;
	}

	/**
	 * Determines the distance (in blocks) within which the entity will stop pathing and will do other activities
	 * @param distance The distance to stop following within
	 * @return this
	 */
	public FollowEntity<E, T> stopFollowingWithin(float distance) {
		return stopFollowingWithin((entity, target) -> distance);
	}

	/**
	 * Determines the distance (in blocks) within which the entity will stop pathing and will do other activities
	 * @param distanceProvider The function to provide the distance to stop following within
	 * @return this
	 */
	public FollowEntity<E, T> stopFollowingWithin(ToFloatBiFunction<E, T> distanceProvider) {
		this.followDistMin = distanceProvider;

		return this;
	}

	/**
	 * Set the movespeed modifier for when the entity is strafing.
	 * @param modifier The multiplier for movement speed
	 * @return this
	 */
	public FollowEntity<E, T> speedMod(float modifier) {
		return speedMod((entity, target) -> modifier);
	}

	/**
	 * Set the movespeed modifier for when the entity is strafing.
	 * @param modifier The multiplier function for movement speed
	 * @return this
	 */
	public FollowEntity<E, T> speedMod(ToFloatBiFunction<E, T> modifier) {
		this.speedMod = modifier;

		return this;
	}

	/**
	 * Override the default predicate for whether an entity can teleport to a given location when attempting to teleport to the following entity
	 *
	 * @param teleportPosPredicate The predicate for the teleport position
	 * @return this
	 */
	public FollowEntity<E, T> canTeleportTo(TriPredicate<E, BlockPos, BlockState> teleportPosPredicate) {
		this.teleportPredicate = teleportPosPredicate;

		return this;
	}

	/**
	 * Override the default predicate for whether an entity should be looking for an on-ground position or not when teleporting.
	 * <p>
	 * Typically this returns true when the entity can fly, swim, or hover
	 *
	 * @param predicate The predicate for the teleport target
	 * @return this
	 */
	public FollowEntity<E, T> canTeleportOffGroundWhen(Predicate<E> predicate) {
		this.canTeleportOffGround = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		T target = this.followingEntityProvider.apply(entity);

		if (target == null || target.isSpectator())
			return false;

		double minDist = this.followDistMin.applyAsFloat(entity, target);

		return entity.distanceToSqr(target) > minDist * minDist;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		T target = this.followingEntityProvider.apply(entity);

		if (target == null)
			return false;

		double dist = entity.distanceToSqr(target);
		double minDist = this.followDistMin.applyAsFloat(entity, target);

		return dist > minDist * minDist;
	}

	@Override
	protected void start(E entity) {
		T target = this.followingEntityProvider.apply(entity);
		double minDist = this.followDistMin.applyAsFloat(entity, target);
		float speedMod = this.speedMod.applyAsFloat(entity, target);
		this.oldWaterPathMalus = entity.getPathfindingMalus(PathType.WATER);

		if (entity.fireImmune()) {
			this.oldLavaPathMalus = entity.getPathfindingMalus(PathType.LAVA);

			entity.setPathfindingMalus(PathType.LAVA, 0);
		}

		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(target, speedMod, (int)minDist));
		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
		entity.setPathfindingMalus(PathType.WATER, 0);
	}

	@Override
	protected void stop(E entity) {
		entity.setPathfindingMalus(PathType.WATER, this.oldWaterPathMalus);

		if (entity.fireImmune())
			entity.setPathfindingMalus(PathType.LAVA, this.oldLavaPathMalus);

		entity.getNavigation().stop();
		BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
	}

	@Override
	protected void tick(E entity) {
		T target = this.followingEntityProvider.apply(entity);
		double teleportDist = this.teleportDistance.applyAsFloat(entity, target);

		if (entity.distanceToSqr(target) >= teleportDist * teleportDist)
			teleportToTarget(entity, target);
	}

	/**
	 * Attempt to teleport to a random safe position near the target entity
	 *
	 * @param entity The entity to teleport
	 * @param target The target entity to teleport around
	 */
	protected void teleportToTarget(E entity, T target) {
		BlockPos targetPos = target.blockPosition();
		BlockPos teleportPos = getTeleportPos(entity, target, targetPos);

		if (!teleportPos.equals(targetPos)) {
			entity.snapTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5, entity.getYRot(), entity.getXRot());
			entity.getNavigation().stop();
			BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
	}

	/**
	 * Attempt to locate a safe teleport position for the given entity around the target
	 * <p>
	 * Failing to find a safe location should return the <code>targetPos</code> parameter instead
	 *
	 * @param entity The entity to teleport
	 * @param target The target entity to teleport around
	 * @param targetPos The block position that the entity is teleporting from
	 * @return A safe teleport position, or the provided <code>targetPos</code> arg if not found
	 */
	protected BlockPos getTeleportPos(E entity, T target, BlockPos targetPos) {
		Level level = entity.level();

		return RandomUtil.getRandomPositionWithinRange(targetPos, 5, 5, 5, 1, 1, 1, !this.canTeleportOffGround.test(entity), level, 10, (state, statePos) ->
				this.teleportPredicate.test(entity, statePos, state));
	}

	protected boolean isTeleportable(E entity, BlockPos pos, BlockState state) {
		NodeEvaluator nodeEvaluator = entity.getNavigation().getNodeEvaluator();
		PathType pathType = nodeEvaluator.getPathType(new PathfindingContext(entity.level(), entity), pos.getX(), pos.getY(), pos.getZ());

		if (!this.canTeleportOffGround.test(entity)) {
			if (pathType != PathType.WALKABLE)
				return false;
		}
		else if (pathType != PathType.OPEN && pathType != PathType.WALKABLE) {
			return false;
		}

		return entity.level().noCollision(entity, entity.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(entity.position())));
	}
}