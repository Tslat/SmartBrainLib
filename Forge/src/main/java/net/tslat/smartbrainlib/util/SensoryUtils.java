package net.tslat.smartbrainlib.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.tslat.smartbrainlib.object.ExtendedTargetingConditions;

/**
 * Helper class for sensory-related utility methods.
 * <p>Mostly this just replaces the poorly implemented methods in {@link net.minecraft.world.entity.ai.sensing.Sensor}</p>
 */
public class SensoryUtils {
    /**
     * Check whether the given target is considered 'targetable' based on sensory and status conditions such as teams and line of sight
     * @return Whether the target is considered targetable
     */
    public static boolean isEntityTargetable(LivingEntity attacker, LivingEntity target) {
        final ExtendedTargetingConditions predicate = BrainUtils.getTargetOfEntity(attacker) == target ?
                ExtendedTargetingConditions.forLookTarget().withFollowRange().skipInvisibilityCheck() :
                ExtendedTargetingConditions.forLookTarget().withFollowRange();

        return predicate.test(attacker, target);
    }

    /**
     * Check whether the given target is considered 'attackable' based on sensory and status conditions such as teams and line of sight
     * @return Whether the target is considered attackable
     */
    public static boolean isEntityAttackable(LivingEntity attacker, LivingEntity target) {
        final ExtendedTargetingConditions predicate = BrainUtils.getTargetOfEntity(attacker) == target ?
                ExtendedTargetingConditions.forAttackTarget().withFollowRange().skipInvisibilityCheck() :
                ExtendedTargetingConditions.forAttackTarget().withFollowRange();

        return predicate.test(attacker, target);
    }

    /**
     * Check whether the given target is considered 'attackable' based on sensory and status conditions such as teams and difficulty, but specifically excluding a line of sight check
     * @return Whether the target is considered attackable
     */
    public static boolean isEntityAttackableIgnoringLineOfSight(LivingEntity attacker, LivingEntity target) {
        final ExtendedTargetingConditions predicate = BrainUtils.getTargetOfEntity(attacker) == target ?
                ExtendedTargetingConditions.forAttackTarget().ignoreLineOfSight().withFollowRange().skipInvisibilityCheck() :
                ExtendedTargetingConditions.forAttackTarget().ignoreLineOfSight().withFollowRange();

        return predicate.test(attacker, target);
    }

    /**
     * Check whether the given target is visible to the entity
     * @return Whether the entity has line of sight to the target
     */
    public static boolean hasLineOfSight(LivingEntity entity, Entity target) {
        if (entity instanceof Mob mob)
            return mob.getSensing().hasLineOfSight(target);

        return entity.hasLineOfSight(target);
    }
}