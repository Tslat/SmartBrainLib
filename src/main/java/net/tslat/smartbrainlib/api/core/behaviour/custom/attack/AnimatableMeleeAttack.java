package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Extended behaviour for melee attacking. Natively supports animation hit delays or other delays. <br>
 * Defaults:
 * <ul>
 *     <li>20 tick attack interval</li>
 * </ul>
 * @param <E> The entity
 */
public class AnimatableMeleeAttack<E extends MobEntity> extends DelayedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {new Pair(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT), new Pair(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.VALUE_ABSENT)});

	private Function<E, Integer> attackIntervalSupplier = entity -> 20;

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
	public AnimatableMeleeAttack<E> attackInterval(Function<E, Integer> supplier) {
		this.attackIntervalSupplier = supplier;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		this.target = BrainUtils.getTargetOfEntity(entity);

		return entity.getSensing().canSee(this.target) && BrainUtil.isWithinMeleeAttackRange(entity, this.target);
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
		BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.apply(entity));

		if (this.target == null)
			return;

		if (!entity.getSensing().canSee(this.target) || !BrainUtil.isWithinMeleeAttackRange(entity, this.target))
			return;

		entity.doHurtTarget(this.target);
	}
}
