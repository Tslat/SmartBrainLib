package net.tslat.smartbrainlib.object;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * PositionTracker implementation that takes a {@link Vec3} supplier, allowing for dynamic positioning
 */
public class DynamicPositionTracker implements PositionTracker {
	private final Supplier<Vec3> posSupplier;

	public DynamicPositionTracker(Supplier<Vec3> posSupplier) {
		this.posSupplier = posSupplier;
	}

	@Override
	public Vec3 currentPosition() {
		return this.posSupplier.get();
	}

	@Override
	public BlockPos currentBlockPosition() {
		return BlockPos.containing(this.posSupplier.get());
	}

	@Override
	public boolean isVisibleBy(LivingEntity entity) {
		return true;
	}
}
