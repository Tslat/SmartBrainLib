package net.tslat.smartbrainlib.library.object;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// Helper class to store radius values without needlessly using 3 values
///
/// @param xzRadius The lateral radius value (X/Z direction)
/// @param yRadius The vertical radius value (Y direction)
public record SquareRadius(double xzRadius, double yRadius) {
	public SquareRadius(double cubeRadius) {
		this(cubeRadius, cubeRadius);
	}

	/// @return This `SquareRadius` instance, expressed as a [Vec3i]
	public Vec3i toVec3i() {
		return new Vec3i(Mth.floor(this.xzRadius), Mth.floor(this.yRadius), Mth.floor(this.xzRadius));
	}

	/// @return This `SquareRadius` instance, expressed as a [BlockPos]
	public BlockPos toBlockPos() {
		return BlockPos.containing(this.xzRadius, this.yRadius, this.xzRadius);
	}

	/// @return This `SquareRadius` instance, expressed as a [Vec3]
	public Vec3 toVec3() {
		return new Vec3(this.xzRadius, this.yRadius, this.xzRadius);
	}

	/// @return This `SquareRadius` instance, expressed as an [AABB] centered on the world origin `(0,0,0)`
	public AABB toAABB() {
		return createRegion(Vec3.ZERO);
	}

	/// @return This `SquareRadius` instance, expressed as an [AABB] centered on the provided origin point
	public AABB createRegion(Vec3 origin) {
		return AABB.ofSize(origin, this.xzRadius * 2, this.yRadius * 2, this.xzRadius * 2);
	}

	/// @return Whether the given target point is within this instance's radius of the given center point
	public boolean contains(Vec3 center, Vec3 target) {
		return createRegion(center).contains(target);
	}

	/// @return An [AABB] instance of the input bounds, inflated by this radius' values in all directions
	public AABB inflateAABB(AABB bounds) {
		return bounds.inflate(this.xzRadius, this.yRadius, this.xzRadius);
	}
	
	/// Generate a random [Vec3] within this radius' bounds of the origin point
	public Vec3 getRandomPos(Vec3 origin, RandomSource random) {
		final EasyRandom rand = EasyRandom.wrap(random);
		
		return origin.add(rand.valueBetween(-this.xzRadius, this.xzRadius), rand.valueBetween(-this.yRadius, this.yRadius), rand.valueBetween(-this.xzRadius, this.xzRadius));
	}
}