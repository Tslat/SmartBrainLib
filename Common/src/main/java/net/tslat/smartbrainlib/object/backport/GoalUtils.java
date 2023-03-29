package net.tslat.smartbrainlib.object.backport;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * BACKPORT FROM MODERN MC
 */
public final class GoalUtils {
	/**
	 * @return if a mob is stuck, within a certain radius beyond it's restriction radius
	 */
	public static boolean mobRestricted(PathfinderMob mob, int radius) {
		if (!mob.hasRestriction())
			return false;

		double restrictRadius = mob.getRestrictRadius() + radius + 1;

		return mob.getRestrictCenter().distSqr(mob.blockPosition()) < restrictRadius * restrictRadius;
	}

	/**
	 * @return if a mob is above or below the map
	 */
	public static boolean isOutsideLimits(BlockPos pos, PathfinderMob mob) {
		return pos.getY() < 0 || pos.getY() > mob.level.getMaxBuildHeight();
	}

	/**
	 * @return if a mob is restricted. The first parameter short circuits the operation.
	 */
	public static boolean isRestricted(boolean preCondition, PathfinderMob mob, BlockPos pos) {
		return preCondition && !mob.isWithinRestriction(pos);
	}

	/**
	 * @return if the destination can't be pathfinded to
	 */
	public static boolean isNotStable(PathNavigation navigator, BlockPos pos) {
		return !navigator.isStableDestination(pos);
	}

	/**
	 * @return if the position is water in the mob's level
	 */
	public static boolean isWater(PathfinderMob mob, BlockPos pos) {
		return mob.level.getFluidState(pos).is(FluidTags.WATER);
	}

	/**
	 * @return if the pathfinding malus exists
	 */
	public static boolean hasMalus(PathfinderMob mob, BlockPos pos) {
		return mob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(mob.level, pos.mutable())) != 0;
	}

	/**
	 * @return if the mob is standing on a solid material
	 */
	public static boolean isSolid(PathfinderMob mob, BlockPos pos) {
		return mob.level.getBlockState(pos).getMaterial().isSolid();
	}
}
