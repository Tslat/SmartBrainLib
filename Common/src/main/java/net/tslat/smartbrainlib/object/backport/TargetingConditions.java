package net.tslat.smartbrainlib.object.backport;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * BACKPORTED FROM MODERN MC
 */
public final class TargetingConditions {
	private final boolean isCombat;
	private double range = -1.0D;
	private boolean checkLineOfSight = true;
	private boolean testInvisible = true;
	@Nullable
	private Predicate<LivingEntity> selector;

	private TargetingConditions(boolean pIsCombat) {
		this.isCombat = pIsCombat;
	}

	public static TargetingConditions forCombat() {
		return new TargetingConditions(true);
	}

	public static TargetingConditions forNonCombat() {
		return new TargetingConditions(false);
	}

	public TargetingConditions copy() {
		TargetingConditions newConditions = this.isCombat ? forCombat() : forNonCombat();
		newConditions.range = this.range;
		newConditions.checkLineOfSight = this.checkLineOfSight;
		newConditions.testInvisible = this.testInvisible;
		newConditions.selector = this.selector;

		return newConditions;
	}

	public TargetingConditions range(double range) {
		this.range = range;

		return this;
	}

	public TargetingConditions ignoreLineOfSight() {
		this.checkLineOfSight = false;

		return this;
	}

	public TargetingConditions ignoreInvisibilityTesting() {
		this.testInvisible = false;

		return this;
	}

	public TargetingConditions selector(@Nullable Predicate<LivingEntity> selector) {
		this.selector = selector;

		return this;
	}

	public boolean test(@Nullable LivingEntity attacker, LivingEntity target) {
		if (attacker == target)
			return false;

		if (target.isSpectator() || !target.isAlive())
			return false;

		if (this.selector != null && !this.selector.test(target))
			return false;

		if (attacker == null) {
			return !this.isCombat || (!target.isInvulnerable() && target.level.getDifficulty() != Difficulty.PEACEFUL);
		}
		else {
			if (this.isCombat && (!attacker.canAttack(target) || !attacker.canAttackType(target.getType()) || attacker.isAlliedTo(target)))
				return false;

			if (this.range > 0.0D) {
				double visibilityPercent = this.testInvisible ? target.getVisibilityPercent(attacker) : 1.0D;
				double visibilityRange = Math.max(this.range * visibilityPercent, 2.0D);
				double distSqr = attacker.distanceToSqr(target.getX(), target.getY(), target.getZ());

				if (distSqr > visibilityRange * visibilityRange)
					return false;
			}

			if (this.checkLineOfSight && attacker instanceof Mob) {
				return ((Mob)attacker).getSensing().canSee(target);
			}
		}

		return true;
	}
}
