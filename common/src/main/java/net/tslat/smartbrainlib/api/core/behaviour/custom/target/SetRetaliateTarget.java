package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Sets the attack target of the entity based on the last entity to hurt it if a target isn't already set. <br>
 * Defaults:
 * <ul>
 *     <li>Targets any live entity, as long as it's not a creative mode player</li>
 *     <li>Does not alert nearby allies when retaliating</li>
 *     <li>If enabled, only alerts allies of the same class, if they don't already have a target themselves</li>
 * </ul>
 * @param <E> The entity
 */
public class SetRetaliateTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.HURT_BY_ENTITY).noMemory(MemoryModuleType.ATTACK_TARGET);

	protected Predicate<LivingEntity> canAttackPredicate = entity -> entity.isAlive() && (!(entity instanceof Player player) || !player.getAbilities().invulnerable);

	protected LivingEntity toTarget = null;
	protected BiPredicate<E, Entity> alertAlliesPredicate = (owner, attacker) -> false;
	protected BiPredicate<E, LivingEntity> allyPredicate = (owner, ally) -> {
		if (!owner.getClass().isAssignableFrom(ally.getClass()) || BrainUtil.getTargetOfEntity(ally) != null)
			return false;

		if (owner instanceof OwnableEntity pet && pet.getOwner() != ((OwnableEntity)ally).getOwner())
			return false;

		Entity lastHurtBy = BrainUtil.getMemory(ally, MemoryModuleType.HURT_BY_ENTITY);

		return lastHurtBy == null || !ally.isAlliedTo(lastHurtBy);
	};

	/**
	 * Set the predicate to determine whether a given entity should be targeted or not.
	 * @param predicate The predicate
	 * @return this
	 */
	public SetRetaliateTarget<E> attackablePredicate(Predicate<LivingEntity> predicate) {
		this.canAttackPredicate = predicate;

		return this;
	}

	/**
	 * Set the predicate to determine whether the brain owner should alert nearby allies of the same entity type when retaliating
	 * @param predicate The predicate
	 * @return this
	 */
	public SetRetaliateTarget<E> alertAlliesWhen(BiPredicate<E, Entity> predicate) {
		this.alertAlliesPredicate = predicate;

		return this;
	}

	/**
	 * Set the predicate to determine whether a given entity should be alerted to the target as an ally of the brain owner.<br>
	 * Overriding replaces the default predicate, so be sure to include any portions of the default predicate in your own if applicable
	 * @param predicate The predicate
	 * @return this
	 */
	public SetRetaliateTarget<E> isAllyIf(BiPredicate<E, LivingEntity> predicate) {
		this.allyPredicate = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
		this.toTarget = BrainUtil.getMemory(owner, MemoryModuleType.HURT_BY_ENTITY);

		if (this.toTarget.isAlive() && this.toTarget.level() == level && this.canAttackPredicate.test(this.toTarget)) {
			if (this.alertAlliesPredicate.test(owner, this.toTarget))
				alertAllies(level, owner);

			return true;
		}

		return false;
	}

	@Override
	protected void start(E entity) {
		BrainUtil.setTargetOfEntity(entity, this.toTarget);
		BrainUtil.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		this.toTarget = null;
	}

	protected void alertAllies(ServerLevel level, E owner) {
		double followRange = owner.getAttributeValue(Attributes.FOLLOW_RANGE);

		for (LivingEntity ally : EntityRetrievalUtil.getEntities(owner, followRange, 10, followRange, LivingEntity.class, entity -> this.allyPredicate.test(owner, entity))) {
			BrainUtil.setTargetOfEntity(ally, this.toTarget);
		}
	}
}