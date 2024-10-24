package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Extended behaviour for melee attacking. Natively supports animation hit delays or other delays. <br>
 * Defaults:
 * <ul>
 *     <li>20 tick attack interval</li>
 * </ul>
 * @param <E> The entity
 */
public class AnimatableMeleeAttack<E extends Mob> extends DelayedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);

	protected ToIntFunction<E> attackIntervalSupplier = entity -> 20;

	@Nullable
	protected LivingEntity target = null;

	public AnimatableMeleeAttack(int delayTicks) {
		super(delayTicks);
	}

	/**
	 * Set the time between attacks.
	 * @param supplier The tick value provider
	 * @return this
	 */
	public AnimatableMeleeAttack<E> attackInterval(ToIntFunction<E> supplier) {
		this.attackIntervalSupplier = supplier;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		this.target = BrainUtil.getTargetOfEntity(entity);

		return entity.getSensing().hasLineOfSight(this.target) && entity.isWithinMeleeAttackRange(this.target);
	}

	@Override
	protected void start(E entity) {
		entity.swing(InteractionHand.MAIN_HAND);
		BehaviorUtils.lookAtEntity(entity, this.target);
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}

	@Override
	protected void doDelayedAction(E entity) {
		BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.applyAsInt(entity));

		if (this.target == null)
			return;

		if (!entity.getSensing().hasLineOfSight(this.target) || !entity.isWithinMeleeAttackRange(this.target))
			return;

		entity.doHurtTarget((ServerLevel)entity.level(), this.target);
	}
}
