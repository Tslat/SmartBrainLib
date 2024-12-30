package net.tslat.smartbrainlib.object;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

/**
 * PositionTracker implementation that takes a raw {@link Vec3} for precision
 */
public class FreePositionTracker implements PositionTracker {
	private final Vec3 pos;

	public FreePositionTracker(Vec3 pos) {
		this.pos = pos;
	}

	@Override
	public Vec3 currentPosition() {
		return this.pos;
	}

	@Override
	public BlockPos currentBlockPosition() {
		return BlockPos.containing(this.pos);
	}

	@Override
	public boolean isVisibleBy(LivingEntity entity) {
		return true;
	}

	@Override
	public String toString() {
		return "FreePositionTracker{pos=" + this.pos + "}";
	}
}
