package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import java.util.List;
import java.util.function.BiFunction;

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
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

/**
 * Flee the current attack target. <br>
 * Defaults:
 * <ul>
 *     <li>20 block flee distance</li>
 *     <li>1x move speed modifier</li>
 * </ul>
 * @param <E> The entity
 */
public class FleeTarget<E extends CreatureEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT)});

	protected BiFunction<E, LivingEntity, Integer> fleeDistance = (entity, target) -> 20;
	protected BiFunction<E, Vector3d, Float> speedModifier = (entity, targetPos) -> 1f;

	protected Path runPath = null;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	/**
	 * Set the maximum distance the entity should try to flee to
	 * @param blocks The distance, in blocks
	 * @return this
	 */
	public FleeTarget<E> fleeDistance(int blocks) {
		return fleeDistance((entity, target) -> blocks);
	}

	/**
	 * Set the maximum distance the entity should try to flee to
	 * @param function The distance function
	 * @return this
	 */
	public FleeTarget<E> fleeDistance(BiFunction<E, LivingEntity, Integer> function) {
		this.fleeDistance = function;

		return this;
	}

	/**
	 * Set the movespeed modifier for when the entity is running away.
	 * @param modifier The speed multiplier modifier
	 * @return this
	 */
	public FleeTarget<E> speedModifier(float modifier) {
		return speedModifier((entity, targetPos) -> modifier);
	}

	/**
	 * Set the movespeed modifier for when the entity is running away.
	 * @param function The speed multiplier modifier function
	 * @return this
	 */
	public FleeTarget<E> speedModifier(BiFunction<E, Vector3d, Float> function) {
		this.speedModifier = function;

		return this;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		LivingEntity target = BrainUtils.getTargetOfEntity(entity);
		double distToTarget = entity.distanceToSqr(target);
		//Vector3d runPos = DefaultRandomPos.getPosAway(entity, this.fleeDistance, 10, target.position());
		Vector3d runPos = RandomPositionGenerator.getPosAvoid(entity, this.fleeDistance.apply(entity, target), 10, target.position());

		if (runPos == null || target.distanceToSqr(runPos.x, runPos.y, runPos.z) < distToTarget)
			return false;

		this.runPath = entity.getNavigation().createPath(runPos.x, runPos.y, runPos.z, 0);

		return this.runPath != null;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return entity.getNavigation().getPath() == this.runPath && !entity.getNavigation().isDone();
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return false;
	}

	@Override
	protected void start(E entity) {
		entity.getNavigation().moveTo(this.runPath, this.speedModifier.apply(entity, this.runPath.getEntityPosAtNode(entity, this.runPath.getNodeCount() - 1)));
		BrainUtils.clearMemory(entity, MemoryModuleType.ATTACK_TARGET);
	}

	@Override
	protected void stop(E entity) {
		if (entity.getNavigation().getPath() == this.runPath)
			entity.getNavigation().setSpeedModifier(1);

		this.runPath = null;
	}
}
