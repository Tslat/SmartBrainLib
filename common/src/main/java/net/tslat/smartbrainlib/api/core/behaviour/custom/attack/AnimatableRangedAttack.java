package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Extended behaviour for ranged attacking. Natively supports animation hit delays or other delays.
 * Defaults:
 * <ul>
 *     <li>40-tick firing interval, decreased to 20 ticks when on {@link Difficulty Hard Difficulty}</li>
 *     <li>16-block firing radius</li>
 * </ul>
 */
public class AnimatableRangedAttack<E extends LivingEntity & RangedAttackMob> extends DelayedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).noMemory(MemoryModuleType.ATTACK_COOLING_DOWN);

	protected ToIntFunction<E> attackIntervalSupplier = entity -> entity.level().getDifficulty() == Difficulty.HARD ? 20 : 40;
	protected float attackRadius;

	@Nullable
	protected LivingEntity target = null;

	public AnimatableRangedAttack(int delayTicks) {
		super(delayTicks);

		attackRadius(16);
	}

	/**
	 * Set the time between attacks.
	 * @param supplier The tick value provider
	 * @return this
	 */
	public AnimatableRangedAttack<E> attackInterval(ToIntFunction<E> supplier) {
		this.attackIntervalSupplier = supplier;

		return this;
	}

	/**
	 * Set the radius in blocks that the entity should be able to fire on targets.
	 * @param radius The radius, in blocks
	 * @return this
	 */
	public AnimatableRangedAttack<E> attackRadius(float radius) {
		this.attackRadius = radius * radius;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		this.target = BrainUtil.getTargetOfEntity(entity);

		return BrainUtil.canSee(entity, this.target) && entity.distanceToSqr(this.target) <= this.attackRadius;
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
		if (this.target == null)
			return;

		if (!BrainUtil.canSee(entity, this.target) || entity.distanceToSqr(this.target) > this.attackRadius)
			return;

		entity.performRangedAttack(this.target, 1);
		BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.applyAsInt(entity));
	}
}
