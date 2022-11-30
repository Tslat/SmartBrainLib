package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.util.math.vector.Vector3d;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

/**
 * Movement behaviour to handle proximal strafing. Will run away if too close, or run towards if too far. <br>
 * Useful for ranged attackers. <br>
 * Defaults:
 * <ul>
 *     <li>Continues strafing until the target is no longer in memory</li>
 *     <li>Stays between 5 and 20 blocks of the target</li>
 *     <li>Normal strafing speed</li>
 *     <li>30% speed boost to repositioning</li>
 * </ul>
 * @param <E> The entity
 */
public class StayWithinDistanceOfAttackTarget<E extends CreatureEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT)});

	protected BiFunction<E, LivingEntity, Float> distMax = (entity, target) -> 20f;
	protected BiFunction<E, LivingEntity, Float> distMin = (entity, target) -> 5f;
	protected Predicate<E> stopWhen = entity -> false;
	protected float speedMod = 1;
	protected float repositionSpeedMod = 1.3f;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	/**
	 * Set a custom condition for when the strafing should end.
	 * Deprecated, use {@link ExtendedBehaviour#stopIf}
	 * @param predicate The predicate
	 * @return this
	 */
	@Deprecated(forRemoval = true)
	public StayWithinDistanceOfAttackTarget<E> stopStrafingWhen(Predicate<E> predicate) {
		this.stopWhen = predicate;

		return this;
	}

	/**
	 * Set how far the entity should attempt to stay away from the target at a minimum.
	 * @param distance The distance, in blocks
	 * @return this
	 */
	public StayWithinDistanceOfAttackTarget<E> minDistance(float distance) {
		return minDistance((entity, target) -> distance);
	}

	/**
	 * Set how far the entity should attempt to stay away from the target at a minimum.
	 * @param distance The distance function, in blocks
	 * @return this
	 */
	public StayWithinDistanceOfAttackTarget<E> minDistance(BiFunction<E, LivingEntity, Float> distance) {
		this.distMin = distance;

		return this;
	}

	/**
	 * Set how far the entity should attempt to stay away from the target at most.
	 * @param distance The distance, in blocks
	 * @return this
	 */
	public StayWithinDistanceOfAttackTarget<E> maxDistance(float distance) {
		return maxDistance((entity, target) -> distance);
	}

	/**
	 * Set how far the entity should attempt to stay away from the target at most.
	 * @param distance The distance function, in blocks
	 * @return this
	 */
	public StayWithinDistanceOfAttackTarget<E> maxDistance(BiFunction<E, LivingEntity, Float> distance) {
		this.distMax = distance;

		return this;
	}

	/**
	 * Set the movespeed modifier for when the entity is strafing.
	 * @param modifier The multiplier for movement speed
	 * @return this
	 */
	public StayWithinDistanceOfAttackTarget<E> speedMod(float modifier) {
		this.speedMod = modifier;

		return this;
	}

	/**
	 * Set the movespeed modifier for when the entity is repositioning due to being too close or too far.
	 * @param modifier The multiplier for movement speed
	 * @return this
	 */
	public StayWithinDistanceOfAttackTarget<E> repositionSpeedMod(float modifier) {
		this.speedMod = modifier;

		return this;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return BrainUtils.hasMemory(entity, MemoryModuleType.ATTACK_TARGET) && !this.stopWhen.test(entity);
	}

	@Override
	protected void tick(E entity) {
		LivingEntity target = BrainUtils.getTargetOfEntity(entity);
		double distanceToTarget = target.distanceToSqr(entity);
		float maxDist = this.distMax.apply(entity, target);
		double maxDistSq = Math.pow(maxDist, 2);
		double minDistSq = Math.pow(this.distMin.apply(entity, target), 2);
		PathNavigator navigation = entity.getNavigation();

		if (distanceToTarget > maxDistSq || !entity.canSee(target)) {
			if (navigation.isDone())
				navigation.moveTo(target, this.repositionSpeedMod);

			return;
		}

		if (distanceToTarget < minDistSq) {
			if (navigation.isDone()) {
				Vector3d runPos = RandomPositionGenerator.getLandPosTowards(entity, (int)maxDist, 5, target.position());//Correct replacement method?

				if (runPos != null)
					navigation.moveTo(navigation.createPath(new BlockPos(runPos), 1), this.repositionSpeedMod);
			}

			return;
		}

		navigation.stop();
		BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(target, true));

		if (distanceToTarget > maxDistSq * 0.5f) {
			entity.lookAt(target, 30, 30);
			entity.getMoveControl().strafe(0.5f * this.speedMod, 0);
		}
		else if (distanceToTarget < minDistSq * 3f) {
			entity.lookAt(target, 30, 30);
			entity.getMoveControl().strafe(-0.5f * this.speedMod, 0);
		}
	}
}
