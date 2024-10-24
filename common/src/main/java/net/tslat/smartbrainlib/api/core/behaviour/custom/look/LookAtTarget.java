package net.tslat.smartbrainlib.api.core.behaviour.custom.look;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

/**
 * Look at the look target for as long as it is present
 * <p>
 * Additionally, invalidates the look target if it is an {@link EntityTracker} and the entity has expired
 *
 * @param <E> The entity
 */
public class LookAtTarget<E extends Mob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.LOOK_TARGET);

	public LookAtTarget() {
		noTimeout();
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return testAndInvalidateLookTarget(entity);
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return testAndInvalidateLookTarget(entity);
	}

	@Override
	protected void tick(E entity) {
		BrainUtil.withMemory(entity, MemoryModuleType.LOOK_TARGET, target -> entity.getLookControl().setLookAt(target.currentPosition()));
	}

	/**
	 * Check and expire the look target if it is no longer valid
	 *
	 * @return true if the look target is valid
	 */
	protected boolean testAndInvalidateLookTarget(E entity) {
		PositionTracker lookTarget = BrainUtil.getMemory(entity, MemoryModuleType.LOOK_TARGET);

		if (lookTarget == null)
			return false;

		if (lookTarget instanceof EntityTracker entityTracker && !entityTracker.getEntity().isAlive()) {
			BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);

			return false;
		}

		return true;
	}
}
