package net.tslat.smartbrainlib.core.behaviour.custom.move;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;

/**
 * Try to move away from certain entities when they get too close. <br>
 * Defaults:
 * <ul>
 *     <li>3 block minimum distance</li>
 *     <li>7 block maximum distance</li>
 *     <li>1x move speed modifier</li>
 * </ul>
 */
public class AvoidEntity<E extends CreatureEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get(), MemoryModuleStatus.VALUE_PRESENT)});

	private Predicate<LivingEntity> avoidingPredicate = target -> false;
	private float noCloserThanSqr = 9f;
	private float stopAvoidingAfterSqr = 49f;
	private float speedModifier = 1;

	private Path runPath = null;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
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
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		Optional<LivingEntity> target = BrainUtils.getMemory(entity, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get()).findFirstMatchingEntry(this.avoidingPredicate);

		if (!target.isPresent())
			return false;

		LivingEntity avoidingEntity = target.get();
		double distToTarget = avoidingEntity.distanceToSqr(entity);

		if (distToTarget > this.noCloserThanSqr)
			return false;

		Vector3d runPos = RandomPositionGenerator.getPosAvoid(entity, 16, 7, avoidingEntity.position());

		if (runPos == null || avoidingEntity.distanceToSqr(runPos.x, runPos.y, runPos.z) < distToTarget)
			return false;

		this.runPath = entity.getNavigation().createPath(runPos.x, runPos.y, runPos.z, 0);

		return this.runPath != null;
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
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
