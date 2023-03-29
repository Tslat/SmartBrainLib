package net.tslat.smartbrainlib.object.backport;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * BACKPORT FROM MODERN MC
 */
public class RandomPos {
	/**
	 * Gets a random position within a certain distance.
	 */
	public static BlockPos generateRandomDirection(Random random, int lateralRadius, int verticalRadius) {
		return new BlockPos(
				random.nextInt(2 * lateralRadius + 1) - lateralRadius,
				random.nextInt(2 * verticalRadius + 1) - verticalRadius,
				random.nextInt(2 * lateralRadius + 1) - lateralRadius);
	}

	/**
	 * @return a random (x, y, z) coordinate by picking a point (x, z), adding a random angle, up to a difference of
	 * {@code maxAngleDelta}. The y position is randomly chosen from the range {@code [y - yRange, y + yRange]}. Will be
	 * {@code null} if the chosen coordinate is outside a distance of {@code maxHorizontalDistance} from the origin.
	 * @param lateralRadius The maximum value in x and z, in absolute value, that could be returned.
	 * @param verticalRadius The range plus or minus the y position to be chosen
	 * @param yOffset The target y position
	 * @param x The x offset to the target position
	 * @param z The z offset to the target position
	 * @param amplifier The maximum variance of the returned angle, from the base angle being a vector from (0, 0)
	 * to (x, z).
	 */
	@Nullable
	public static BlockPos generateRandomDirectionWithinRadians(Random random, int lateralRadius, int verticalRadius, int yOffset, double x, double z, double amplifier) {
		double angle = (Mth.atan2(z, x) - (double)((float)Math.PI / 2f)) + (double)(2f * random.nextFloat() - 1) * amplifier;
		double length = Math.sqrt(random.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)lateralRadius;
		double zDist = -length * Math.sin(angle);
		double xDist = length * Math.cos(angle);

		if (Math.abs(zDist) <= (double)lateralRadius && Math.abs(xDist) <= (double)lateralRadius)
			return new BlockPos(zDist, random.nextInt(2 * verticalRadius + 1) - verticalRadius + yOffset, xDist);

		return null;
	}

	/**
	 * @return the highest above position that is within the provided conditions
	 */
	public static BlockPos moveUpOutOfSolid(BlockPos pos, int maxY, Predicate<BlockPos> posPredicate) {
		if (!posPredicate.test(pos))
			return pos;

		for(pos = pos.above(); pos.getY() < maxY && posPredicate.test(pos); pos = pos.above()) {}

		return pos;
	}

	/**
	 * Finds a position above based on the conditions.
	 *
	 * After it finds the position once, it will continue to move up until aboveSolidAmount is reached or the position is
	 * no longer valid
	 */
	@VisibleForTesting
	public static BlockPos moveUpToAboveSolid(BlockPos pos, int minGroundClearance, int maxY, Predicate<BlockPos> posPredicate) {
		if (!posPredicate.test(pos))
			return pos;

		for(pos = pos.above(); pos.getY() < maxY && posPredicate.test(pos); pos = pos.above()) {}

		BlockPos testPos;
		BlockPos lastGoodPos;

		for(testPos = pos; testPos.getY() < maxY && testPos.getY() - pos.getY() < minGroundClearance; testPos = lastGoodPos) {
			lastGoodPos = testPos.above();

			if (posPredicate.test(lastGoodPos))
				break;
		}

		return testPos;
	}

	@Nullable
	public static Vec3 generateRandomPos(PathfinderMob mob, Supplier<BlockPos> posSupplier) {
		return generateRandomPos(posSupplier, mob::getWalkTargetValue);
	}

	/**
	 * Tries 10 times to maximize the return value of the position to double function based on the supplied position
	 */
	@Nullable
	public static Vec3 generateRandomPos(Supplier<BlockPos> posSupplier, ToDoubleFunction<BlockPos> posPathWeightFunction) {
		double minWeight = Double.NEGATIVE_INFINITY;
		BlockPos pos = null;

		for(int i = 0; i < 10; ++i) {
			BlockPos nextPos = posSupplier.get();

			if (nextPos != null) {
				double pathWeight = posPathWeightFunction.applyAsDouble(nextPos);

				if (pathWeight > minWeight) {
					minWeight = pathWeight;
					pos = nextPos;
				}
			}
		}

		return pos != null ? Vec3.atBottomCenterOf(pos) : null;
	}

	/**
	 * @return a random position within range, only if the mob is currently restricted
	 */
	public static BlockPos generateRandomPosTowardDirection(PathfinderMob mob, int radius, Random random, BlockPos pos) {
		int x = pos.getX();
		int z = pos.getZ();

		if (mob.hasRestriction() && radius > 1) {
			BlockPos restrictionCenter = mob.getRestrictCenter();
			x += random.nextInt(radius / 2) * (mob.getX() > (double)restrictionCenter.getX() ? -1 : 1);
			z += random.nextInt(radius / 2) * (mob.getZ() > (double)restrictionCenter.getZ() ? -1 : 1);
		}

		return new BlockPos((double)x + mob.getX(), (double)pos.getY() + mob.getY(), (double)z + mob.getZ());
	}
}
