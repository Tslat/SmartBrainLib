package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.tslat.smartbrainlib.util.BrainUtil;

/**
 * Extended behaviour for charging and firing a
 * {@link BowItem bow}.
 */
public class BowAttack<E extends LivingEntity & RangedAttackMob> extends AnimatableRangedAttack<E> {
	public BowAttack(int delayTicks) {
		super(delayTicks);
	}

	@Override
	protected void start(E entity) {
		BehaviorUtils.lookAtEntity(entity, this.target);
		entity.startUsingItem(ProjectileUtil.getWeaponHoldingHand(entity, Items.BOW));
	}

	@Override
	protected void doDelayedAction(E entity) {
		if (this.target == null)
			return;

		if (!BrainUtil.canSee(entity, this.target) || entity.distanceToSqr(this.target) > this.attackRadius)
			return;

		entity.performRangedAttack(this.target, BowItem.getPowerForTime(entity.getTicksUsingItem()));
		entity.stopUsingItem();
		BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.applyAsInt(entity));
	}

	@Override
	protected void stop(E entity) {
		super.stop(entity);
		entity.stopUsingItem();
	}
}
