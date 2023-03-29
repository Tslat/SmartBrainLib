package net.tslat.smartbrainlib.object.backport;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Backported various miscellaneous functions from modern MC to allow for a clean backport
 */
public final class BackportUtils {
	public static boolean hasLineOfSight(LivingEntity entity, Entity target) {
		if (target.level != entity.level)
			return false;

		Vec3 startVec = entity.getEyePosition(1);
		Vec3 endVec = target.getEyePosition(1);

		if (endVec.distanceTo(startVec) > 128.0D)
			return false;

		return entity.level.clip(new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
	}
}
