package net.tslat.smartbrainlib.object;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class to store radius values without needlessly using 3 values. <br>
 * Also comes with some handy helper methods.
 * @param xzRadius The lateral radius value (X/Z direction)
 * @param yRadius The vertical radius value (Y direction)
 */
public final class SquareRadius {
	private final double xzRadius;
	private final double yRadius;

	public SquareRadius(double xzRadius, double yRadius) {
		this.xzRadius = xzRadius;
		this.yRadius = yRadius;
	}

	public double xzRadius() {
		return this.xzRadius;
	}

	public double yRadius() {
		return this.yRadius;
	}

	public Vec3i toVec3i() {
		return new Vec3i(this.xzRadius, this.yRadius, this.xzRadius);
	}

	public BlockPos toBlockPos() {
		return new BlockPos(this.xzRadius, this.yRadius, this.xzRadius);
	}

	public Vec3 toVec3() {
		return new Vec3(this.xzRadius, this.yRadius, this.xzRadius);
	}

	public AABB inflateAABB(AABB bounds) {
		return bounds.inflate(this.xzRadius, this.yRadius, this.xzRadius);
	}
}