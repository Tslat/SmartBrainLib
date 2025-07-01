package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.util.BrainUtil;

/**
 * Extension of MoveToWalkTarget, but auto-marking the sprinting flag depending on the movespeed.<br>
 * This can be useful for using sprint animations on the client.
 */
public class WalkOrRunToWalkTarget<E extends PathfinderMob> extends MoveToWalkTarget<E> {
	@Override
	protected void startOnNewPath(E entity) {
		BrainUtil.setMemory(entity, MemoryModuleType.PATH, this.path);

		if (entity.getNavigation().moveTo(this.path, this.speedModifier))
			entity.setSharedFlag(3, this.speedModifier > 1);
	}

	@Override
	protected void stop(E entity) {
		super.stop(entity);

		entity.setSharedFlag(3, false);
	}
}