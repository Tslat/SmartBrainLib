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
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.RandomUtil;

import java.util.List;
import java.util.function.BiFunction;
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

	protected BiFunction<E, T, Double> teleportDistance = (entity, target) -> Double.MAX_VALUE;
	protected BiFunction<E, T, Double> followDistMin = (entity, target) -> 4d;
	protected BiFunction<E, T, Float> speedMod = (entity, target) -> 1f;

	protected float oldWaterPathMalus = 0;
	protected float oldLavaPathMalus = 0;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

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
	public FollowEntity<E, T> teleportToTargetAfter(double distance) {
		return teleportToTargetAfter((entity, target) -> distance);
	}

	/**
	 * Determines the distance (in blocks) after which the entity should just attempt to teleport closer to the target entity
	 * @param distanceProvider The function to provide the distance to teleport after
	 * @return this
	 */
	public FollowEntity<E, T> teleportToTargetAfter(BiFunction<E, T, Double> distanceProvider) {
		this.teleportDistance = distanceProvider;

		return this;
	}

	/**
	 * Determines the distance (in blocks) within which the entity will stop pathing and will do other activities
	 * @param distance The distance to stop following within
	 * @return this
	 */
	public FollowEntity<E, T> stopFollowingWithin(double distance) {
		return stopFollowingWithin((entity, target) -> distance);
	}

	/**
	 * Determines the distance (in blocks) within which the entity will stop pathing and will do other activities
	 * @param distanceProvider The function to provide the distance to stop following within
	 * @return this
	 */
	public FollowEntity<E, T> stopFollowingWithin(BiFunction<E, T, Double> distanceProvider) {
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
	public FollowEntity<E, T> speedMod(BiFunction<E, T, Float> modifier) {
		this.speedMod = modifier;

		return this;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		T target = this.followingEntityProvider.apply(entity);

		if (target == null || target.isSpectator())
			return false;

		double minDist = this.followDistMin.apply(entity, target);

		return entity.distanceToSqr(target) > minDist * minDist;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		if (entity.getNavigation().isDone())
			return false;

		T target = this.followingEntityProvider.apply(entity);

		if (target == null)
			return false;

		double dist = entity.distanceToSqr(target);
		double minDist = this.followDistMin.apply(entity, target);

		return dist > minDist * minDist;
	}

	@Override
	protected void start(E entity) {
		T target = this.followingEntityProvider.apply(entity);
		double minDist = this.followDistMin.apply(entity, target);
		float speedMod = this.speedMod.apply(entity, target);
		this.oldWaterPathMalus = entity.getPathfindingMalus(BlockPathTypes.WATER);

		if (entity.fireImmune()) {
			this.oldLavaPathMalus = entity.getPathfindingMalus(BlockPathTypes.LAVA);

			entity.setPathfindingMalus(BlockPathTypes.LAVA, 0);
		}

		BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(target, speedMod, (int)minDist));
		BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
		entity.setPathfindingMalus(BlockPathTypes.WATER, 0);
	}

	@Override
	protected void stop(E entity) {
		entity.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterPathMalus);

		if (entity.fireImmune())
			entity.setPathfindingMalus(BlockPathTypes.LAVA, this.oldLavaPathMalus);

		entity.getNavigation().stop();
		BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
	}

	@Override
	protected void tick(E entity) {
		T target = this.followingEntityProvider.apply(entity);
		double teleportDist = this.teleportDistance.apply(entity, target);

		if (entity.distanceToSqr(target) >= teleportDist * teleportDist)
			teleportToTarget(entity, target);
	}

	protected void teleportToTarget(E entity, T target) {
		Level level = entity.level;
		BlockPos entityPos = target.blockPosition();

		BlockPos pos = RandomUtil.getRandomPositionWithinRange(entityPos, 5, 5, 5, 1, 1, 1, true, level, 10, (state, statePos) -> {
			BlockPathTypes pathTypes = entity.getNavigation().getNodeEvaluator().getBlockPathType(level, statePos.getX(), statePos.getY(), statePos.getZ());

			if (pathTypes != BlockPathTypes.WALKABLE)
				return false;

			return level.noCollision(entity, entity.getBoundingBox().move(statePos.subtract(entityPos)));
		});

		if (pos != entityPos) {
			entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, entity.getYRot(), entity.getXRot());
			entity.getNavigation().stop();
			BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
	}
}