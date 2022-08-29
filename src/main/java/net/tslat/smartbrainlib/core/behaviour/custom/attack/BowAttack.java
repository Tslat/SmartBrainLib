package net.tslat.smartbrainlib.core.behaviour.custom.attack;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.BowItem;
import net.tslat.smartbrainlib.api.util.BrainUtils;

/**
 * Extended behaviour for charging and firing a {@link net.minecraft.world.item.BowItem bow}.
 * @param <E>
 */
public class BowAttack<E extends LivingEntity & IRangedAttackMob> extends AnimatableRangedAttack<E> {
	public BowAttack(int delayTicks) {
		super(delayTicks);
	}

	@Override
	protected void start(E entity) {
		BrainUtils.lookAtEntity(entity, this.target);
		entity.startUsingItem(ProjectileHelper.getWeaponHoldingHand(entity, item -> item instanceof BowItem));
	}

	@Override
	protected void doDelayedAction(E entity) {
		if (this.target == null)
			return;

		if (!BrainUtils.canSee(entity, this.target) || entity.distanceToSqr(this.target) > this.attackRadius)
			return;

		entity.performRangedAttack(this.target, BowItem.getPowerForTime(entity.getTicksUsingItem()));
		entity.stopUsingItem();
		BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.apply(entity));
	}
}
