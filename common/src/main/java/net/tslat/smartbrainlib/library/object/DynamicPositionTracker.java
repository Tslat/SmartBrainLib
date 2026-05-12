package net.tslat.smartbrainlib.library.object;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/// PositionTracker implementation that takes a [Vec3] supplier, allowing for dynamic positioning
public class DynamicPositionTracker implements PositionTracker {
	protected final Supplier<Vec3> posSupplier;

	public DynamicPositionTracker(Supplier<Vec3> posSupplier) {
		this.posSupplier = posSupplier;
	}

	/// @return The current position of this tracker
	@Override
	public Vec3 currentPosition() {
		return this.posSupplier.get();
	}

	/// @return The block position equivalent of the current position of this tracker
	@Override
	public BlockPos currentBlockPosition() {
		return BlockPos.containing(currentPosition());
	}

	/// @return Whether the given position is considered 'visible' by the provided entity. Always true
	@Override
	public boolean isVisibleBy(LivingEntity entity) {
		return true;
	}
}
