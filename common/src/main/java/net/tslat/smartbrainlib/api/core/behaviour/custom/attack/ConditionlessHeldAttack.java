package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.HeldBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Attack behaviour for held attacks that doesn't require line of sight or proximity to target, or to even have a target at all.
 * This is useful for special attacks.<br>
 * Set the actual condition for activation via {@link net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour#startCondition ExtendedBehaviour.startCondition}
 * @param <E> The entity
 */
public class ConditionlessHeldAttack<E extends LivingEntity> extends HeldBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);

	protected boolean requireTarget = false;

	@Nullable
	protected LivingEntity target = null;

	/**
	 * Set that the attack requires that the entity have an attack target set to activate.
	 * @return this
	 */
	public ConditionlessHeldAttack<E> requiresTarget() {
		this.requireTarget = true;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		if (!this.requireTarget)
			return true;

		this.target = BrainUtil.getTargetOfEntity(entity);

		return this.target != null;
	}

	@Override
	protected void start(E entity) {
		entity.swing(InteractionHand.MAIN_HAND);

		if (this.requireTarget)
			BehaviorUtils.lookAtEntity(entity, this.target);
	}

	@Override
	protected void stop(ServerLevel level, E entity, long gameTime) {
		super.stop(level, entity, gameTime);

		this.target = null;
	}
}
