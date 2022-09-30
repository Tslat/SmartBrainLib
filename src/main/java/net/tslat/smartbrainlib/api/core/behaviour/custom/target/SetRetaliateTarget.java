package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;

/**
 * Sets the attack target of the entity based on the last entity to hurt it if a target isn't already set. <br>
 * Defaults:
 * <ul>
 *     <li>Targets any live entity, as long as it's not a creative mode player</li>
 * </ul>
 * @param <E> The entity
 */
public class SetRetaliateTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));

	protected Predicate<LivingEntity> canAttackPredicate = entity -> entity.isAlive() && (!(entity instanceof Player player) || !player.isCreative());

	protected LivingEntity toTarget = null;

	/**
	 * Set the predicate to determine whether a given entity should be targeted or not.
	 * @param predicate The predicate
	 * @return this
	 */
	public SetRetaliateTarget<E> attackablePredicate(Predicate<LivingEntity> predicate) {
		this.canAttackPredicate = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel pLevel, E owner) {
		Brain<?> brain = owner.getBrain();
		this.toTarget = BrainUtils.getMemory(brain, MemoryModuleType.HURT_BY_ENTITY);

		return this.toTarget != null && this.canAttackPredicate.test(this.toTarget);
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setTargetOfEntity(entity, this.toTarget);
		BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		this.toTarget = null;
	}
}