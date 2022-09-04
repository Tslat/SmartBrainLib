package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import net.tslat.smartbrainlib.api.util.BrainUtils;

import javax.annotation.Nullable;
import java.util.List;

public class MoveToWalkTarget<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED), Pair.of(MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT));

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
		WalkTarget walkTarget = BrainUtils.getMemory(brain, MemoryModuleType.WALK_TARGET);

		if (!hasReachedTarget(entity, walkTarget) && attemptNewPath(entity, walkTarget, false)) {
			this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();

			return true;
		}

		BrainUtils.clearMemory(brain, MemoryModuleType.WALK_TARGET);
		BrainUtils.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		return false;
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		if (this.path == null || this.lastTargetPos == null)
			return false;

		if (entity.getNavigation().isDone())
			return false;

		WalkTarget walkTarget = BrainUtils.getMemory(entity, MemoryModuleType.WALK_TARGET);

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

			BrainUtils.setMemory(brain, MemoryModuleType.PATH, path);
		}

		if (path != null && this.lastTargetPos != null) {
			WalkTarget walkTarget = BrainUtils.getMemory(brain, MemoryModuleType.WALK_TARGET);

			if (walkTarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4 && attemptNewPath(entity, walkTarget, hasReachedTarget(entity, walkTarget))) {
				this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();

				startOnNewPath(entity);
			}
		}
	}

	@Override
	protected void stop(E entity) {
		Brain<?> brain = entity.getBrain();

		if (!entity.getNavigation().isStuck() || !BrainUtils.hasMemory(brain, MemoryModuleType.WALK_TARGET) || hasReachedTarget(entity, BrainUtils.getMemory(brain, MemoryModuleType.WALK_TARGET)))
			this.cooldownFinishedAt = 0;

		entity.getNavigation().stop();
		BrainUtils.clearMemories(brain, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);

		this.path = null;
	}

	protected boolean attemptNewPath(E entity, WalkTarget walkTarget, boolean reachedCurrentTarget) {
		Brain<?> brain = entity.getBrain();
		BlockPos pos = walkTarget.getTarget().currentBlockPosition();
		this.path = entity.getNavigation().createPath(pos, 0);
		this.speedModifier = walkTarget.getSpeedModifier();

		if (reachedCurrentTarget) {
			BrainUtils.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

			return false;
		}

		if (this.path != null && this.path.canReach()) {
			BrainUtils.clearMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		}
		else {
			BrainUtils.setMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, entity.level.getGameTime());
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
		BrainUtils.setMemory(entity, MemoryModuleType.PATH, this.path);
		entity.getNavigation().moveTo(this.path, this.speedModifier);
	}
}
