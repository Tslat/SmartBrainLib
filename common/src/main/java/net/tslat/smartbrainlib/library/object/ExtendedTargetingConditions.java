package net.tslat.smartbrainlib.library.object;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.tslat.smartbrainlib.util.SensoryUtil;
import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.ToDoubleFunction;

/// Replacement for Vanilla's [net.minecraft.world.entity.ai.targeting.TargetingConditions] due to its somewhat limited implementation
///
/// It is encouraged for instances of this class to be cached statically as a [BiPredicate] where possible so they don't need to be recreated constantly
public class ExtendedTargetingConditions implements BiPredicate<@Nullable LivingEntity, LivingEntity> {
    protected BiPredicate<@Nullable LivingEntity, LivingEntity> customFilter = (_, _) -> true;
    protected ToDoubleFunction<LivingEntity> maxRange = _ -> 0;
    protected boolean isAttacking = true;
    protected boolean checkLineOfSight = true;
    protected boolean ignoresInvisibility = false;

    protected ExtendedTargetingConditions() {}

    /// Create a new [ExtendedTargetingConditions] for combat targeting
    public static ExtendedTargetingConditions create() {
        return new ExtendedTargetingConditions();
    }

    /// Create a new [ExtendedTargetingConditions] for combat targeting, but additionally ignoring invisibility conditions.<br/>
    /// This is often used where the entity is already being targeted/tracked, and we're just checking for attackability.
    public static ExtendedTargetingConditions createIgnoringInvisibility() {
        return create().skipInvisibilityCheck();
    }

    /// Create a new [ExtendedTargetingConditions] purely for visual targeting
    ///
    /// @see #isJustLooking()
    public static ExtendedTargetingConditions forLookTarget() {
        return create().isJustLooking();
    }

    /// Create a new [ExtendedTargetingConditions] purely for [visual targeting][#isJustLooking()], but additionally ignoring invisibility conditions.<br/>
    /// This is often used where the entity is already being targeted/tracked, and we're just checking for additional conditions.
    public static ExtendedTargetingConditions forLookTargetIgnoringInvisibility() {
        return forLookTarget().skipInvisibilityCheck();
    }

    /// Skip any attack-related checks in the predicate, such as difficulty, invulnerability, or teams
    public ExtendedTargetingConditions isJustLooking() {
        this.isAttacking = false;

        return this;
    }

    /// Filter out any entities further away than the provided `maxRange`
    public ExtendedTargetingConditions withRange(double maxRange) {
        return withRange(_ -> maxRange);
    }

    /// Filter out any entities further away than the provided `maxRange`
    public ExtendedTargetingConditions withRange(ToDoubleFunction<LivingEntity> maxRange) {
        this.maxRange = maxRange;

        return this;
    }

    /// Filter out any entities outside the entity's [Attributes#FOLLOW_RANGE] attribute
    public ExtendedTargetingConditions withFollowRange() {
        return withRange(entity -> entity.getAttribute(Attributes.FOLLOW_RANGE) != null ? entity.getAttributeValue(Attributes.FOLLOW_RANGE) : 16d);
    }

    /// Filter out any specific cases that may apply.<br/>
    /// This check is applied <u>before</u> any other conditions are checked
    ///
    /// Note that the targeting entity may be null, for generic checks
    ///
    /// @param predicate The predicate that determines if a target entity is applicable or not
    public ExtendedTargetingConditions onlyTargeting(BiPredicate<@Nullable LivingEntity, LivingEntity> predicate) {
        this.customFilter = predicate;

        return this;
    }

    /// Skip line-of-sight checks when testing.<br/>
    /// This can be useful for entities that track with other senses, or for other special-case situations
    public ExtendedTargetingConditions ignoreLineOfSight() {
        this.checkLineOfSight = false;

        return this;
    }

    /// Skip the invisibility check when testing.<br/>
    /// This is often used where the entity is already being targeted/tracked, and we're just checking for attackability.
    public ExtendedTargetingConditions skipInvisibilityCheck() {
        this.ignoresInvisibility = true;

        return this;
    }

    /// Tests the given entity and its potential target
    ///
    /// The targeting entity may be null for generic checks
    public boolean test(@Nullable LivingEntity entity, LivingEntity target) {
        if (entity == target || !target.canBeSeenByAnyone())
            return false;

        if (!this.customFilter.test(entity, target))
            return false;

        if (entity == null)
            return !this.isAttacking || (target.canBeSeenAsEnemy() && target.level().getDifficulty() != Difficulty.PEACEFUL);

        if (this.isAttacking && (!entity.canAttack(target) || !entity.canAttack(target) || entity.isAlliedTo(target)))
            return false;

        final double range = this.maxRange.applyAsDouble(entity);

        if (range > 0) {
            double sightRange = Math.max(range * (this.ignoresInvisibility ? 1 : target.getVisibilityPercent(entity)), 2);

            if (entity.distanceToSqr(target) > sightRange * sightRange)
                return false;
        }

        return !this.checkLineOfSight || SensoryUtil.hasLineOfSight(entity, target);
    }
}
