package net.tslat.smartbrainlib.object.backport;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * BACKPORT FROM MODERN MC
 */
public class DefaultRandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob mob, int lateralRadius, int verticalRadius) {
		return RandomPos.generateRandomPos(mob, () -> generateRandomPosTowardDirection(mob, lateralRadius, GoalUtils.mobRestricted(mob, lateralRadius), RandomPos.generateRandomDirection(mob.getRandom(), lateralRadius, verticalRadius)));
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob mob, int lateralRadius, int verticalRadius, Vec3 center, double amplifier) {
		Vec3 distVec = center.subtract(mob.getX(), mob.getY(), mob.getZ());

		return RandomPos.generateRandomPos(mob, () -> {
			BlockPos randomPos = RandomPos.generateRandomDirectionWithinRadians(mob.getRandom(), lateralRadius, verticalRadius, 0, distVec.x, distVec.z, amplifier);

			return randomPos == null ? null : generateRandomPosTowardDirection(mob, lateralRadius, GoalUtils.mobRestricted(mob, lateralRadius), randomPos);
		});
	}

	@Nullable
	public static Vec3 getPosAway(PathfinderMob mob, int lateralRadius, int verticalRadius, Vec3 center) {
		Vec3 distVec = mob.position().subtract(center);

		return RandomPos.generateRandomPos(mob, () -> {
			BlockPos randomPos = RandomPos.generateRandomDirectionWithinRadians(mob.getRandom(), lateralRadius, verticalRadius, 0, distVec.x, distVec.z, (float)Math.PI / 2f);

			return randomPos == null ? null : generateRandomPosTowardDirection(mob, lateralRadius, GoalUtils.mobRestricted(mob, lateralRadius), randomPos);
		});
	}

	@Nullable
	private static BlockPos generateRandomPosTowardDirection(PathfinderMob mob, int lateralRadius, boolean preCondition, BlockPos towardsPos) {
		BlockPos randomPos = RandomPos.generateRandomPosTowardDirection(mob, lateralRadius, mob.getRandom(), towardsPos);

		return !GoalUtils.isOutsideLimits(randomPos, mob) && !GoalUtils.isRestricted(preCondition, mob, randomPos) && !GoalUtils.isNotStable(mob.getNavigation(), randomPos) && !GoalUtils.hasMalus(mob, randomPos) ? randomPos : null;
	}
}
