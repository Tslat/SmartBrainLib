package net.tslat.smartbrainlib.library.object;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

/// PositionTracker implementation that takes a plain [Vec3] for precision
public class ExactPositionTracker implements PositionTracker {
	protected final Vec3 pos;

	public ExactPositionTracker(Vec3 pos) {
		this.pos = pos;
	}

	/// @return The current position of this tracker
	@Override
	public Vec3 currentPosition() {
		return this.pos;
	}

	/// @return The block position equivalent of the current position of this tracker
	@Override
	public BlockPos currentBlockPosition() {
		return BlockPos.containing(this.pos);
	}

	/// @return Whether the given position is considered 'visible' by the provided entity. Always true
	@Override
	public boolean isVisibleBy(LivingEntity entity) {
		return true;
	}

	@Override
	public String toString() {
		return "ExactPositionTracker{pos=" + this.pos + "}";
	}
}
