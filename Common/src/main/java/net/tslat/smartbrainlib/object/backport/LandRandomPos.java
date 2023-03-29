package net.tslat.smartbrainlib.object.backport;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.ToDoubleFunction;

public class LandRandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob mob, int lateralRadius, int verticalRadius) {
		return getPos(mob, lateralRadius, verticalRadius, mob::getWalkTargetValue);
	}

	@Nullable
	public static Vec3 getPos(PathfinderMob mob, int lateralRadius, int verticalRadius, ToDoubleFunction<BlockPos> posPathWeightFunction) {
		return RandomPos.generateRandomPos(() -> {
			BlockPos randomPos = generateRandomPosTowardDirection(mob, lateralRadius, GoalUtils.mobRestricted(mob, lateralRadius), RandomPos.generateRandomDirection(mob.getRandom(), lateralRadius, verticalRadius));

			return randomPos == null ? null : movePosUpOutOfSolid(mob, randomPos);
		}, posPathWeightFunction);
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob mob, int lateralRadius, int verticalRadius, Vec3 center) {
		return getPosInDirection(mob, lateralRadius, verticalRadius, center.subtract(mob.getX(), mob.getY(), mob.getZ()), GoalUtils.mobRestricted(mob, lateralRadius));
	}

	@Nullable
	public static Vec3 getPosAway(PathfinderMob mob, int lateralRadius, int verticalRadius, Vec3 center) {
		return getPosInDirection(mob, lateralRadius, verticalRadius, mob.position().subtract(center), GoalUtils.mobRestricted(mob, lateralRadius));
	}

	@Nullable
	private static Vec3 getPosInDirection(PathfinderMob mob, int lateralRadius, int verticalRadius, Vec3 center, boolean preCondition) {
		return RandomPos.generateRandomPos(mob, () -> {
			BlockPos randomPos = RandomPos.generateRandomDirectionWithinRadians(mob.getRandom(), lateralRadius, verticalRadius, 0, center.x, center.z, (float)Math.PI / 2f);

			if (randomPos == null)
				return null;

			BlockPos towardsRandomPos = generateRandomPosTowardDirection(mob, lateralRadius, preCondition, randomPos);

			return towardsRandomPos == null ? null : movePosUpOutOfSolid(mob, towardsRandomPos);
		});
	}

	@Nullable
	public static BlockPos movePosUpOutOfSolid(PathfinderMob mob, BlockPos pos) {
		pos = RandomPos.moveUpOutOfSolid(pos, mob.level.getMaxBuildHeight(), testPos -> GoalUtils.isSolid(mob, testPos));

		return !GoalUtils.isWater(mob, pos) && !GoalUtils.hasMalus(mob, pos) ? pos : null;
	}

	@Nullable
	public static BlockPos generateRandomPosTowardDirection(PathfinderMob mob, int radius, boolean preCondition, BlockPos pos) {
		BlockPos randomPos = RandomPos.generateRandomPosTowardDirection(mob, radius, mob.getRandom(), pos);

		return !GoalUtils.isOutsideLimits(randomPos, mob) && !GoalUtils.isRestricted(preCondition, mob, randomPos) && !GoalUtils.isNotStable(mob.getNavigation(), randomPos) ? randomPos : null;
	}
}