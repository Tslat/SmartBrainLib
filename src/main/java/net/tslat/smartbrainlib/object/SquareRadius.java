package net.tslat.smartbrainlib.object;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

/**
 * Helper class to store radius values without needlessly using 3 values. <br>
 * Also comes with some handy helper methods.
 * @param xzRadius The lateral radius value (X/Z direction)
 * @param yRadius The vertical radius value (Y direction)
 */
public class SquareRadius {
	
	protected double xzRadius;
	protected double yRadius;
	
	public SquareRadius(double xzRadius, double yRadius) {
		this.xzRadius = xzRadius;
		this.yRadius = yRadius;
	}
	public Vector3i toVec3i() {
		return new Vector3i(this.xzRadius, this.yRadius, this.xzRadius);
	}

	public BlockPos toBlockPos() {
		return new BlockPos(this.xzRadius, this.yRadius, this.xzRadius);
	}

	public Vector3d toVec3() {
		return new Vector3d(this.xzRadius, this.yRadius, this.xzRadius);
	}

	public AxisAlignedBB inflateAABB(AxisAlignedBB bounds) {
		return bounds.inflate(this.xzRadius, this.yRadius, this.xzRadius);
	}
	public double xzRadius() {
		return this.xzRadius;
	}
	public double yRadius() {
		return this.yRadius;
	}
}