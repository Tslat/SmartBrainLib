package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Sets the attack target of the entity, utilising a few sources of targets. <br>
 * In order:
 * <ol>
 *     <li>The {@link MemoryModuleType#NEAREST_ATTACKABLE} memory value</li>
 *     <li>The {@link MemoryModuleType#HURT_BY_ENTITY} memory value</li>
 *     <li>The closest applicable entity from the {@link MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES} memory value</li>
 * </ol>
 * Defaults:
 * <ul>
 *     <li>Targets any live entity, as long as it's not a creative-mode player</li>
 *     <li>Does not alert nearby allies when retaliating</li>
 *     <li>If enabled, only alerts allies of the same class, if they don't already have a target themselves</li>
 * </ul>
 * @param <E> The entity
 */
public class TargetOrRetaliate<E extends Mob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(4).usesMemories(MemoryModuleType.ATTACK_TARGET, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

	protected Predicate<LivingEntity> canAttackPredicate = entity -> entity.isAlive() && (!(entity instanceof Player player) || (!player.getAbilities().invulnerable && entity.level().getDifficulty() != Difficulty.PEACEFUL));
	protected BiPredicate<E, Entity> alertAlliesPredicate = (owner, attacker) -> false;
	protected BiPredicate<E, LivingEntity> allyPredicate = (owner, ally) -> {
		if (!owner.getClass().isAssignableFrom(ally.getClass()) || BrainUtil.getTargetOfEntity(ally) != null)
			return false;

		if (owner instanceof OwnableEntity pet && pet.getOwner() != ((OwnableEntity)ally).getOwner())
			return false;

		Entity lastHurtBy = BrainUtil.getMemory(ally, MemoryModuleType.HURT_BY_ENTITY);

		return lastHurtBy == null || !ally.isAlliedTo(lastHurtBy);
	};
	protected boolean canSwapTarget = true;

	protected LivingEntity toTarget = null;
	protected MemoryModuleType<? extends LivingEntity> priorityTargetMemory = MemoryModuleType.NEAREST_ATTACKABLE;

	/**
	 * Set the predicate to determine whether a given entity should be targeted or not.
	 * @param predicate The predicate
	 * @return this
	 */
	public TargetOrRetaliate<E> attackablePredicate(Predicate<LivingEntity> predicate) {
		this.canAttackPredicate = predicate;

		return this;
	}

	/**
	 * Set the memory type that is checked first to target an entity.
	 * Useful for switching to player-only targeting
	 * @return this
	 */
	public TargetOrRetaliate<E> useMemory(MemoryModuleType<? extends LivingEntity> memory) {
		this.priorityTargetMemory = memory;

		return this;
	}

	/**
	 * Set the predicate to determine whether the brain owner should alert nearby allies of the same entity type when retaliating
	 * @param predicate The predicate
	 * @return this
	 */
	public TargetOrRetaliate<E> alertAlliesWhen(BiPredicate<E, Entity> predicate) {
		this.alertAlliesPredicate = predicate;

		return this;
	}

	/**
	 * Set the predicate to determine whether a given entity should be alerted to the target as an ally of the brain owner.<br>
	 * Overriding replaces the default predicate, so be sure to include any portions of the default predicate in your own if applicable
	 * @param predicate The predicate
	 * @return this
	 */
	public TargetOrRetaliate<E> isAllyIf(BiPredicate<E, LivingEntity> predicate) {
		this.allyPredicate = predicate;

		return this;
	}

	/**
	 * Disable the ability to occasionally swap targets if a higher priority target source has one
	 */
	public TargetOrRetaliate<E> noTargetSwapping() {
		this.canSwapTarget = false;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean doStartCheck(ServerLevel level, E entity, long gameTime) {
		return (!BrainUtil.hasMemory(entity, MemoryModuleType.ATTACK_TARGET) || (this.canSwapTarget && entity.tickCount % 100 == 0)) && super.doStartCheck(level, entity, gameTime);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
		this.toTarget = getTarget(owner, level, BrainUtil.getTargetOfEntity(owner));

		return this.toTarget != null;
	}

	@Nullable
	protected LivingEntity getTarget(E owner, ServerLevel level, @Nullable LivingEntity existingTarget) {
		Brain<?> brain = owner.getBrain();
		LivingEntity newTarget = BrainUtil.getMemory(brain, this.priorityTargetMemory);

		if (newTarget == null) {
			newTarget = BrainUtil.getMemory(brain, MemoryModuleType.HURT_BY_ENTITY);

			if (newTarget == null) {
				NearestVisibleLivingEntities nearbyEntities = BrainUtil.getMemory(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

				if (nearbyEntities != null)
					newTarget = nearbyEntities.findClosest(this.canAttackPredicate).orElse(null);

				if (newTarget == null)
					return null;
			}
		}

		if (newTarget == existingTarget)
			return null;

		return this.canAttackPredicate.test(newTarget) ? newTarget : null;
	}

	@Override
	protected void start(E entity) {
		LivingEntity existingTarget = BrainUtil.getTargetOfEntity(entity);

		BrainUtil.setTargetOfEntity(entity, this.toTarget);
		BrainUtil.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		if (this.alertAlliesPredicate.test(entity, this.toTarget) && existingTarget == null)
			alertAllies((ServerLevel)entity.level(), entity);

		this.toTarget = null;
	}

	protected void alertAllies(ServerLevel level, E owner) {
		double followRange = owner.getAttributeValue(Attributes.FOLLOW_RANGE);

		for (LivingEntity ally : EntityRetrievalUtil.getEntities(owner, followRange, 10, followRange, LivingEntity.class, entity -> this.allyPredicate.test(owner, entity))) {
			BrainUtil.setTargetOfEntity(ally, this.toTarget);
		}
	}
}