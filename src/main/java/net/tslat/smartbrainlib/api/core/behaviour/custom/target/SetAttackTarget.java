package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;

/**
 * Sets the attack target of the entity if one is available. <br>
 * Defaults:
 * <ul>
 *     <li>Will target anything set as the {@link MemoryModuleType#NEAREST_ATTACKABLE} memory</li>
 * </ul>
 * @see net.minecraft.world.entity.ai.behavior.StartAttacking
 */
public class SetAttackTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT), Pair.of(SBLMemoryTypes.NEAREST_ATTACKABLE.get(), MemoryModuleStatus.VALUE_PRESENT)});
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> CUSTOM_TARGETING_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT)});

	private final boolean usingNearestAttackable;
	private Predicate<E> canAttackPredicate = entity -> true;
	private Function<E, ? extends LivingEntity> targetFinder = entity -> BrainUtils.getMemory(entity, SBLMemoryTypes.NEAREST_ATTACKABLE.get());

	public SetAttackTarget() {
		this(true);
	}

	public SetAttackTarget(boolean usingNearestAttackable) {
		this.usingNearestAttackable = usingNearestAttackable;
	}

	/**
	 * Set the predicate to determine whether the entity is ready to attack or not.
	 * @param predicate The predicate
	 * @return this
	 */
	public SetAttackTarget<E> attackPredicate(Predicate<E> predicate) {
		this.canAttackPredicate = predicate;

		return this;
	}

	/**
	 * Set the target finding function. If replacing the {@link MemoryModuleType#NEAREST_ATTACKABLE} memory retrieval, set false in the constructor of the behaviour.
	 * @param targetFindingFunction The function
	 * @return this
	 */
	public SetAttackTarget<E> targetFinder(Function<E, ? extends LivingEntity> targetFindingFunction) {
		this.targetFinder = targetFindingFunction;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return this.usingNearestAttackable ? MEMORY_REQUIREMENTS : CUSTOM_TARGETING_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		return this.canAttackPredicate.test(entity);
	}

	@Override
	protected void start(E entity) {
		LivingEntity target = this.targetFinder.apply(entity);

		if (target == null) {
			BrainUtils.clearMemory(entity, MemoryModuleType.ATTACK_TARGET);
		}
		else {
			BrainUtils.setMemory(entity, MemoryModuleType.ATTACK_TARGET, target);
			BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		}
	}
}
