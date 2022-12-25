package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
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
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));

	protected Predicate<LivingEntity> canAttackPredicate = entity -> entity.isAlive() && (!(entity instanceof Player player) || !player.isCreative());

	protected LivingEntity toTarget = null;
	protected BiPredicate<E, Entity> alertAlliesPredicate = (owner, attacker) -> false;
	protected BiPredicate<E, LivingEntity> allyPredicate = (owner, ally) -> BrainUtils.getTargetOfEntity(ally) != null && owner.getClass().isAssignableFrom(ally.getClass()) && (!(owner instanceof TamableAnimal pet) || pet.getOwner() == ((TamableAnimal)ally).getOwner()) && !ally.isAlliedTo(BrainUtils.getMemory(ally, MemoryModuleType.HURT_BY_ENTITY));

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
		this.toTarget = BrainUtils.getMemory(owner, MemoryModuleType.HURT_BY_ENTITY);

		if (this.toTarget.isAlive() && this.toTarget.level == level && this.canAttackPredicate.test(this.toTarget)) {
			if (this.alertAlliesPredicate.test(owner, this.toTarget))
				alertAllies(level, owner);

			return true;
		}

		return false;
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setTargetOfEntity(entity, this.toTarget);
		BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		this.toTarget = null;
	}

	protected void alertAllies(ServerLevel level, E owner) {
		double followRange = owner.getAttributeValue(Attributes.FOLLOW_RANGE);

		for (LivingEntity ally : EntityRetrievalUtil.<LivingEntity>getEntities(level, owner.getBoundingBox().inflate(followRange, 10, followRange),
				entity -> entity != owner && entity instanceof LivingEntity livingEntity && this.allyPredicate.test(owner, livingEntity))) {
			BrainUtils.setTargetOfEntity(ally, this.toTarget);
		}
	}
}