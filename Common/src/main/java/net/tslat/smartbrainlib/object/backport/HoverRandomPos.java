package net.tslat.smartbrainlib.object.backport;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

// BACKPORT FROM MODERN MC
public class HoverRandomPos {
	/**
	 * Tries to generate a random position a couple different ways, and if failing, sees if swimming vertically is an
	 * option.
	 */
	@Nullable
	public static Vec3 getPos(PathfinderMob mob, int lateralRadius, int verticalRadius, double x, double z, float amplifier, int maxSwimUp, int minSwimUp) {
		boolean mobRestricted = GoalUtils.mobRestricted(mob, lateralRadius);

		return RandomPos.generateRandomPos(mob, () -> {
			BlockPos randomPos = RandomPos.generateRandomDirectionWithinRadians(mob.getRandom(), lateralRadius, verticalRadius, 0, x, z, amplifier);

			if (randomPos == null)
				return null;

			BlockPos towardsRandomPos = LandRandomPos.generateRandomPosTowardDirection(mob, lateralRadius, mobRestricted, randomPos);

			if (towardsRandomPos == null)
				return null;

			towardsRandomPos = RandomPos.moveUpToAboveSolid(towardsRandomPos, mob.getRandom().nextInt(maxSwimUp - minSwimUp + 1) + minSwimUp, mob.level.getMaxBuildHeight(), pos -> GoalUtils.isSolid(mob, pos));

			return !GoalUtils.isWater(mob, towardsRandomPos) && !GoalUtils.hasMalus(mob, towardsRandomPos) ? towardsRandomPos : null;
		});
	}
}
