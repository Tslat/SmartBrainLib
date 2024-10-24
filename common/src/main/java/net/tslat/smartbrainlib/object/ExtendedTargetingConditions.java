package net.tslat.smartbrainlib.object;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.tslat.smartbrainlib.util.SensoryUtil;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Replacement for Vanilla's {@link net.minecraft.world.entity.ai.targeting.TargetingConditions} due to its somewhat limited implementation
 */
public class ExtendedTargetingConditions {
    protected BiPredicate<LivingEntity, LivingEntity> customFilter = null;
    protected Function<LivingEntity, Double> rangeRetriever = null;
    protected boolean isAttacking = true;
    protected boolean checkLineOfSight = true;
    protected boolean ignoresInvisibility = false;

    public static ExtendedTargetingConditions forLookTarget() {
        return new ExtendedTargetingConditions().isJustLooking();
    }

    public static ExtendedTargetingConditions forLookTargetIgnoringInvisibility() {
        return forLookTarget().skipInvisibilityCheck();
    }

    public static ExtendedTargetingConditions forAttackTarget() {
        return new ExtendedTargetingConditions();
    }

    public static ExtendedTargetingConditions forAttackTargetIgnoringInvisibility() {
        return forAttackTarget().skipInvisibilityCheck();
    }

    /**
     * Skip any attack-related checks in the predicate, such as difficulty, invulnerability, or teams
     */
    public ExtendedTargetingConditions isJustLooking() {
        this.isAttacking = false;

        return this;
    }

    /**
     * Filter out any targeting that occurs for entities larger than this distance away from the entity
     */
    public ExtendedTargetingConditions withRange(double range) {
        return withRange(entity -> range);
    }

    /**
     * Filter out any targeting that occurs for entities larger than the distance provided by this function from the entity
     */
    public ExtendedTargetingConditions withRange(Function<LivingEntity, Double> function) {
        this.rangeRetriever = function;

        return this;
    }

    /**
     * Filter out any targeting that occurs for entities outside of the entity's {@link Attributes#FOLLOW_RANGE} attribute
     */
    public ExtendedTargetingConditions withFollowRange() {
        return withRange(entity -> entity.getAttribute(Attributes.FOLLOW_RANGE) != null ? entity.getAttributeValue(Attributes.FOLLOW_RANGE) : 16d);
    }

    /**
     * Additionally filter out any specific cases that may apply. This check is applied <u>before</u> any other conditions are checked
     * <p>Note that the targeting entity may be null, for generic checks</p>
     * @return true if the entity should be allowed to target the target, or false if not
     */
    public ExtendedTargetingConditions onlyTargeting(BiPredicate<@Nullable LivingEntity, LivingEntity> predicate) {
        this.customFilter = predicate;

        return this;
    }

    /**
     * Skip the line of sight checks in the predicate. This can be useful for entities that track with other senses, or for other special-case situations
     */
    public ExtendedTargetingConditions ignoreLineOfSight() {
        this.checkLineOfSight = false;

        return this;
    }

    /**
     * Skip the invisibility check for targeting. This is often used where the entity is already being targeted/tracked, and we're just checking for attackability.
     */
    public ExtendedTargetingConditions skipInvisibilityCheck() {
        this.ignoresInvisibility = true;

        return this;
    }

    public boolean test(@Nullable LivingEntity entity, LivingEntity target) {
        if (entity == target || !target.canBeSeenByAnyone())
            return false;

        if (this.customFilter != null && !this.customFilter.test(entity, target))
            return false;

        if (entity == null)
            return !this.isAttacking || (target.canBeSeenAsEnemy() && target.level().getDifficulty() != Difficulty.PEACEFUL);

        if (this.isAttacking && (!entity.canAttack(target) || !entity.canAttackType(target.getType()) || entity.isAlliedTo(target)))
            return false;

        double range = this.rangeRetriever.apply(entity);

        if (range > 0) {
            double sightRange = Math.max(range * (this.ignoresInvisibility ? 1 : target.getVisibilityPercent(entity)), 2);

            if (entity.distanceToSqr(target) > sightRange * sightRange)
                return false;
        }

        if (this.checkLineOfSight)
            return SensoryUtil.hasLineOfSight(entity, target);

        return true;
    }
}
