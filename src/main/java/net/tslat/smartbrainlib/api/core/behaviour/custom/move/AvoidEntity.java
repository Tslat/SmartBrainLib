package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Try to move away from certain entities when they get too close. <br>
 * Defaults:
 * <ul>
 *     <li>3 block minimum distance</li>
 *     <li>7 block maximum distance</li>
 *     <li>1x move speed modifier</li>
 * </ul>
 */
public class AvoidEntity<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));

	protected Predicate<LivingEntity> avoidingPredicate = target -> false;
	protected float noCloserThanSqr = 9f;
	protected float stopAvoidingAfterSqr = 49f;
	protected float speedModifier = 1;

	private Path runPath = null;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	/**
	 * Set the minimum distance the target entity should be allowed to come before the entity starts retreating.
	 * @param blocks The distance, in blocks
	 * @return this
	 */
	public AvoidEntity<E> noCloserThan(float blocks) {
		this.noCloserThanSqr = blocks * blocks;

		return this;
	}

	/**
	 * Set the maximum distance the target entity should be before the entity stops retreating.
	 * @param blocks The distance, in blocks
	 * @return this
	 */
	public AvoidEntity<E> stopCaringAfter(float blocks) {
		this.stopAvoidingAfterSqr = blocks * blocks;

		return this;
	}

	/**
	 * Sets the predicate for entities to avoid.
	 * @param predicate The predicate
	 * @return this
	 */
	public AvoidEntity<E> avoiding(Predicate<LivingEntity> predicate) {
		this.avoidingPredicate = predicate;

		return this;
	}

	/**
	 * Set the movespeed modifier for when the entity is running away.
	 * @param mod The speed multiplier modifier
	 * @return this
	 */
	public AvoidEntity<E> speedModifier(float mod) {
		this.speedModifier = mod;

		return this;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		Optional<LivingEntity> target = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).findClosest(this.avoidingPredicate);

		if (target.isEmpty())
			return false;

		LivingEntity avoidingEntity = target.get();
		double distToTarget = avoidingEntity.distanceToSqr(entity);

		if (distToTarget > this.noCloserThanSqr)
			return false;

		Vec3 runPos = DefaultRandomPos.getPosAway(entity, 16, 7, avoidingEntity.position());

		if (runPos == null || avoidingEntity.distanceToSqr(runPos.x, runPos.y, runPos.z) < distToTarget)
			return false;

		this.runPath = entity.getNavigation().createPath(runPos.x, runPos.y, runPos.z, 0);

		return this.runPath != null;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return !this.runPath.isDone();
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return false;
	}

	@Override
	protected void start(E entity) {
		entity.getNavigation().moveTo(this.runPath, this.speedModifier);
	}

	@Override
	protected void stop(E entity) {
		this.runPath = null;

		entity.getNavigation().setSpeedModifier(1);
	}
}
