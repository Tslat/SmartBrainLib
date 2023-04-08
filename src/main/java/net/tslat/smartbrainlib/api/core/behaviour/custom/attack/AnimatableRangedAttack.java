package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.Hand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Extended behaviour for ranged attacking. Natively supports animation hit delays or other delays.
 * Defaults:
 * <ul>
 *     <li>40-tick firing interval, decreased to 20 ticks when on {@link net.minecraft.world.Difficulty Hard Difficulty}</li>
 *     <li>16-block firing radius</li>
 * </ul>
 * @param <E>
 */
public class AnimatableRangedAttack<E extends LivingEntity & IRangedAttackMob> extends DelayedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.VALUE_ABSENT)});

	protected Function<E, Integer> attackIntervalSupplier = entity -> entity.level.getDifficulty() == Difficulty.HARD ? 20 : 40;
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
	public AnimatableRangedAttack<E> attackInterval(Function<E, Integer> supplier) {
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
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		this.target = BrainUtils.getTargetOfEntity(entity);

		return BrainUtils.canSee(entity, this.target) && entity.distanceToSqr(this.target) <= this.attackRadius;
	}

	@Override
	protected void start(E entity) {
		entity.swing(Hand.MAIN_HAND);
		BrainUtils.lookAtEntity(entity, this.target);
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}

	@Override
	protected void doDelayedAction(E entity) {
		if (this.target == null)
			return;

		if (!BrainUtils.canSee(entity, this.target) || entity.distanceToSqr(this.target) > this.attackRadius)
			return;

		entity.performRangedAttack(this.target, 1);
		BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.apply(entity));
	}
}
