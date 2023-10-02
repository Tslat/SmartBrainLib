package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Set the walk target of the entity to its current attack target.
 * @param <E> The entity
 */
public class SetWalkTargetToAttackTarget<E extends Mob> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));

	@Deprecated(forRemoval = true)
	protected float speedModifier = 1;
	protected BiFunction<E, LivingEntity, Float> speedMod = (owner, target) -> 1f;
	protected BiFunction<E, LivingEntity, Integer> closeEnoughWhen = (owner, target) -> 0;

	/**
	 * Set the movespeed modifier for the entity when moving to the target.
	 * @param speedModifier The movespeed modifier/multiplier
	 * @return this
	 */
	@Deprecated(forRemoval = true)
	public SetWalkTargetToAttackTarget<E> speedMod(float speedModifier) {
		return speedMod((owner, target) -> speedModifier);
	}

	/**
	 * Set the movespeed modifier for the entity when moving to the target.
	 * @param speedModifier The movespeed modifier/multiplier
	 * @return this
	 */
	public SetWalkTargetToAttackTarget<E> speedMod(BiFunction<E, LivingEntity, Float> speedModifier) {
		this.speedMod = speedModifier;

		return this;
	}

	/**
	 * Sets the amount (in blocks) that the mob can be considered 'close enough' to their target that they can stop pathfinding
	 * @param closeEnoughMod The distance modifier
	 * @return this
	 */
	public SetWalkTargetToAttackTarget<E> closeEnoughDist(BiFunction<E, LivingEntity, Integer> closeEnoughMod) {
		this.closeEnoughWhen = closeEnoughMod;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected void start(E entity) {
		Brain<?> brain = entity.getBrain();
		LivingEntity target = BrainUtils.getTargetOfEntity(entity);

		if (entity.getSensing().hasLineOfSight(target) && BehaviorUtils.isWithinAttackRange(entity, target, 1)) {
			BrainUtils.clearMemory(brain, MemoryModuleType.WALK_TARGET);
		}
		else {
			BrainUtils.setMemory(brain, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
			BrainUtils.setMemory(brain, MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(target, false), this.speedMod.apply(entity, target), this.closeEnoughWhen.apply(entity, target)));
		}
	}
}
