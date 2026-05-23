package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;

/// Invalidates the current [attack target][MemoryModuleType#ATTACK_TARGET] if the given conditions are met.
/// Defaults:
///
///   - Will give up trying to path to the target if it hasn't been able to reach it in 200 ticks
///   - Invalidates the target if it's a creative or spectator mode player
///
public class InvalidateAttackTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).usesMemory(MemoryModuleType.LOOK_TARGET).usesMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

	protected BiPredicate<E, LivingEntity> targetInvalidIf = (entity, target) ->
			(target instanceof Player player && player.getAbilities().invulnerable) ||
			(entity.getAttributes().hasAttribute(Attributes.FOLLOW_RANGE) && entity.distanceToSqr(target) >= Mth.square(entity.getAttributeValue(Attributes.FOLLOW_RANGE))) ||
			!entity.canAttack(target);
	protected long pathfindingAttentionSpan = 200;

	/// Sets a custom predicate to invalidate the attack target if none of the previous checks invalidate it first.
	/// Overrides the default player gamemode check
	public InvalidateAttackTarget<E> invalidateIf(BiPredicate<E, LivingEntity> predicate) {
		this.targetInvalidIf = predicate;

		return this;
	}

	/// Skips the check to see if the entity has been unable to path to its target for a while
	public InvalidateAttackTarget<E> ignoreFailedPathfinding() {
		return stopTryingToPathAfter(0);
	}

	/// Sets the attention span for the brain owner's pathfinding. If the entity has been unable to find a good path to
	/// the target after this time, it will invalidate the target.
	public InvalidateAttackTarget<E> stopTryingToPathAfter(long ticks) {
		this.pathfindingAttentionSpan = ticks;

		return this;
	}

	public List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected void start(E entity) {
		LivingEntity target = BrainUtil.getTargetOfEntity(entity);

		if (target == null)
			return;

		if (isTargetInvalid(entity, target) || !canAttack(entity, target) ||
			isTiredOfPathing(entity) || this.targetInvalidIf.test(entity, target)) {
			BrainUtil.clearMemory(entity, MemoryModuleType.ATTACK_TARGET);

			if (BrainUtil.getMemory(entity, MemoryModuleType.LOOK_TARGET) instanceof EntityTracker entityTracker && entityTracker.getEntity() == entity)
				BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
		}
	}

	protected boolean isTargetInvalid(E entity, LivingEntity target) {
		if (entity.level() != target.level())
			return true;

		return target.isDeadOrDying() || target.isRemoved();
	}

	protected boolean canAttack(E entity, LivingEntity target) {
		return entity.canAttack(target);
	}

	protected boolean isTiredOfPathing(E entity) {
		if (this.pathfindingAttentionSpan <= 0)
			return false;

		Long time = BrainUtil.getMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		return time != null && entity.level().getGameTime() - time > this.pathfindingAttentionSpan;
	}
}
