package net.tslat.smartbrainlib.object.backport;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * BACKPORT FROM MODERN MC
 */
public final class AirAndWaterRandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob mob, int lateralRadius, int verticalRadius, int yOffset, double x, double z, double amplifier) {
		return RandomPos.generateRandomPos(mob, () -> generateRandomPos(mob, lateralRadius, verticalRadius, yOffset, x, z, amplifier, GoalUtils.mobRestricted(mob, lateralRadius)));
	}

	@Nullable
	public static BlockPos generateRandomPos(PathfinderMob mob, int lateralRadius, int verticalRadius, int yOffset, double x, double z, double amplifier, boolean preCondition) {
		BlockPos randomPos = RandomPos.generateRandomDirectionWithinRadians(mob.getRandom(), lateralRadius, verticalRadius, yOffset, x, z, amplifier);

		if (randomPos == null)
			return null;

		BlockPos directionalRandomPos = RandomPos.generateRandomPosTowardDirection(mob, lateralRadius, mob.getRandom(), randomPos);

		if (!GoalUtils.isOutsideLimits(directionalRandomPos, mob) && !GoalUtils.isRestricted(preCondition, mob, directionalRandomPos)) {
			directionalRandomPos = RandomPos.moveUpOutOfSolid(directionalRandomPos, mob.level.getMaxBuildHeight(), pos -> GoalUtils.isSolid(mob, pos));

			return GoalUtils.hasMalus(mob, directionalRandomPos) ? null : directionalRandomPos;
		}

		return null;
	}
}
