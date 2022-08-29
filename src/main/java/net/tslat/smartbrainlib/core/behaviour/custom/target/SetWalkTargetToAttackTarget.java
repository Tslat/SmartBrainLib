package net.tslat.smartbrainlib.core.behaviour.custom.target;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.EntityPosWrapper;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour;

/**
 * Set the walk target of the entity to its current attack target.
 * @param <E> The entity
 */
public class SetWalkTargetToAttackTarget<E extends MobEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED), Pair.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED), Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT)});

	private float speedModifier = 1;

	/**
	 * Set the movespeed modifier for the entity when moving to the target.
	 * @param speedModifier The movespeed modifier/multiplier
	 * @return this
	 */
	public SetWalkTargetToAttackTarget<E> speedMod(float speedModifier) {
		this.speedModifier = speedModifier;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected void start(E entity) {
		Brain<?> brain = entity.getBrain();
		LivingEntity target = BrainUtils.getTargetOfEntity(entity);

		if (entity.getSensing().canSee(target) && BrainUtils.isWithinAttackRange(entity, target, 1)) {
			BrainUtils.clearMemory(brain, MemoryModuleType.WALK_TARGET);
		}
		else {
			BrainUtils.setMemory(brain, MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(target, true));
			BrainUtils.setMemory(brain, MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(target, false), this.speedModifier, 0));
		}
	}
}
