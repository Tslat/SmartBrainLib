package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.Predicate;

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
public class StayWithinDistanceOfAttackTarget<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.WALK_TARGET);

	protected ToFloatBiFunction<E, LivingEntity> distMax = (entity, target) -> 20f;
	protected ToFloatBiFunction<E, LivingEntity> distMin = (entity, target) -> 5f;
	protected Predicate<E> stopWhen = entity -> false;
	protected float speedMod = 1;
	protected float repositionSpeedMod = 1.3f;

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
	public StayWithinDistanceOfAttackTarget<E> minDistance(ToFloatBiFunction<E, LivingEntity> distance) {
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
	public StayWithinDistanceOfAttackTarget<E> maxDistance(ToFloatBiFunction<E, LivingEntity> distance) {
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
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return BrainUtil.hasMemory(entity, MemoryModuleType.ATTACK_TARGET) && !this.stopWhen.test(entity);
	}

	@Override
	protected void tick(E entity) {
		LivingEntity target = BrainUtil.getTargetOfEntity(entity);
		double distanceToTarget = target.distanceToSqr(entity);
		float maxDist = this.distMax.applyAsFloat(entity, target);
		double maxDistSq = Math.pow(maxDist, 2);
		double minDistSq = Math.pow(this.distMin.applyAsFloat(entity, target), 2);
		PathNavigation navigation = entity.getNavigation();

		if (distanceToTarget > maxDistSq || !entity.hasLineOfSight(target)) {
			if (navigation.isDone())
				navigation.moveTo(target, this.repositionSpeedMod);

			return;
		}

		if (distanceToTarget < minDistSq) {
			if (navigation.isDone()) {
				Vec3 runPos = DefaultRandomPos.getPosAway(entity, (int)maxDist, 5, target.position());

				if (runPos != null)
					navigation.moveTo(navigation.createPath(BlockPos.containing(runPos), 1), this.repositionSpeedMod);
			}

			return;
		}

		if (navigation instanceof GroundPathNavigation)
			navigation.stop();

		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));

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
