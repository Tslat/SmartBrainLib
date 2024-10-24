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
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.RandomUtil;

import java.util.List;
import java.util.function.Function;

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

	protected void teleportToTarget(E entity, T target) {
		Level level = entity.level();
		BlockPos entityPos = target.blockPosition();

		BlockPos pos = RandomUtil.getRandomPositionWithinRange(entityPos, 5, 5, 5, 1, 1, 1, true, level, 10, (state, statePos) -> {
			PathType pathTypes = entity.getNavigation().getNodeEvaluator().getPathType(new PathfindingContext(level, entity), statePos.getX(), statePos.getY(), statePos.getZ());

			if (pathTypes != PathType.WALKABLE)
				return false;

			return level.noCollision(entity, entity.getBoundingBox().move(Vec3.atBottomCenterOf(statePos).subtract(entity.position())));
		});

		if (pos != entityPos) {
			entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, entity.getYRot(), entity.getXRot());
			entity.getNavigation().stop();
			BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
	}
}