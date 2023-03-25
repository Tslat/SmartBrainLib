package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Attack behaviour that doesn't require line of sight or proximity to target, or to even have a target at all. This is useful for special attacks. <br>
 * Set the actual condition for activation via {@link ExtendedBehaviour#startCondition(Predicate)}
 * @param <E> The entity
 */
public class ConditionlessAttack<E extends LivingEntity> extends DelayedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.VALUE_ABSENT)});

	protected Function<E, Integer> attackIntervalSupplier = entity -> 20;
	protected boolean requireTarget = false;
	protected Consumer<E> effect = entity -> {};

	@Nullable
	protected LivingEntity target = null;

	public ConditionlessAttack(int delayTicks) {
		super(delayTicks);
	}

	/**
	 * Set the time between attacks.
	 * @param supplier The tick value provider
	 * @return this
	 */
	public ConditionlessAttack<E> attackInterval(Function<E, Integer> supplier) {
		this.attackIntervalSupplier = supplier;

		return this;
	}

	/**
	 * Set that the attack requires that the entity have an attack target set to activate.
	 * @return this
	 */
	public ConditionlessAttack<E> requiresTarget() {
		this.requireTarget = true;

		return this;
	}

	/**
	 * Set the callback for the actual attack when the delay time has elapsed
	 * @param consumer The callback
	 * @return this
	 */
	public ConditionlessAttack<E> attack(Consumer<E> consumer) {
		this.effect = consumer;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		if (!this.requireTarget)
			return true;

		this.target = BrainUtils.getTargetOfEntity(entity);

		return this.target != null;
	}

	@Override
	protected void start(E entity) {
		entity.swing(Hand.MAIN_HAND);

		if (this.requireTarget)
			BrainUtils.lookAtEntity(entity, this.target);
	}

	@Override
	protected void doDelayedAction(E entity) {
		if (this.requireTarget && this.target == null)
			return;

		this.effect.accept(entity);
		BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.apply(entity));
	}
}
