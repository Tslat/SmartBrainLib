package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoveToWalkTarget<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3).hasMemory(MemoryModuleType.WALK_TARGET).noMemory(MemoryModuleType.PATH).usesMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

	@Nullable
	protected Path path;
	@Nullable
	protected BlockPos lastTargetPos;
	protected float speedModifier;

	public MoveToWalkTarget() {
		runFor(entity -> entity.getRandom().nextInt(100) + 150);
		cooldownFor(entity -> entity.getRandom().nextInt(40));
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		Brain<?> brain = entity.getBrain();
		WalkTarget walkTarget = BrainUtil.getMemory(brain, MemoryModuleType.WALK_TARGET);

		if (!hasReachedTarget(entity, walkTarget) && attemptNewPath(entity, walkTarget, false)) {
			this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();

			return true;
		}

		BrainUtil.clearMemory(brain, MemoryModuleType.WALK_TARGET);
		BrainUtil.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		return false;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		if (this.path == null || this.lastTargetPos == null)
			return false;

		if (entity.getNavigation().isDone())
			return false;

		WalkTarget walkTarget = BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET);

		return walkTarget != null && !hasReachedTarget(entity, walkTarget);
	}

	@Override
	protected void start(E entity) {
		startOnNewPath(entity);
	}

	@Override
	protected void tick(E entity) {
		Path path = entity.getNavigation().getPath();
		Brain<?> brain = entity.getBrain();

		if (this.path != path) {
			this.path = path;

			BrainUtil.setMemory(brain, MemoryModuleType.PATH, path);
		}

		if (path != null && this.lastTargetPos != null) {
			WalkTarget walkTarget = BrainUtil.getMemory(brain, MemoryModuleType.WALK_TARGET);

			if (walkTarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4 && attemptNewPath(entity, walkTarget, hasReachedTarget(entity, walkTarget))) {
				this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();

				startOnNewPath(entity);
			}
		}
	}

	@Override
	protected void stop(E entity) {
		Brain<?> brain = entity.getBrain();

		if (!entity.getNavigation().isStuck() || !BrainUtil.hasMemory(brain, MemoryModuleType.WALK_TARGET) || hasReachedTarget(entity, BrainUtil.getMemory(brain, MemoryModuleType.WALK_TARGET)))
			this.cooldownFinishedAt = 0;

		entity.getNavigation().stop();
		BrainUtil.clearMemories(brain, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);

		this.path = null;
	}

	protected boolean attemptNewPath(E entity, WalkTarget walkTarget, boolean reachedCurrentTarget) {
		Brain<?> brain = entity.getBrain();
		BlockPos pos = walkTarget.getTarget().currentBlockPosition();
		this.path = entity.getNavigation().createPath(pos, 0);
		this.speedModifier = walkTarget.getSpeedModifier();

		if (reachedCurrentTarget) {
			BrainUtil.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

			return false;
		}

		if (this.path != null && this.path.canReach()) {
			BrainUtil.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		}
		else {
			BrainUtil.setMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, entity.level().getGameTime());
		}

		if (this.path != null)
			return true;

		Vec3 newTargetPos = DefaultRandomPos.getPosTowards(entity, 10, 7, Vec3.atBottomCenterOf(pos), Mth.HALF_PI);

		if (newTargetPos != null) {
			this.path = entity.getNavigation().createPath(newTargetPos.x(), newTargetPos.y(), newTargetPos.z(), 0);

			return this.path != null;
		}

		return false;
	}

	protected boolean hasReachedTarget(E entity, WalkTarget target) {
		return target.getTarget().currentBlockPosition().distManhattan(entity.blockPosition()) <= target.getCloseEnoughDist();
	}

	protected void startOnNewPath(E entity) {
		BrainUtil.setMemory(entity, MemoryModuleType.PATH, this.path);
		entity.getNavigation().moveTo(this.path, this.speedModifier);
	}
}
