package net.tslat.smartbrainlib.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.tslat.smartbrainlib.library.object.ExtendedTargetingConditions;
import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;

/// Helper class for sensory-related utility methods.
///
/// Mostly this just replaces the poorly implemented methods in [net.minecraft.world.entity.ai.sensing.Sensor]
public final class SensoryUtil {
    //<editor-fold defaultstate="collapsed" desc="<Internal Impl>">
    private static final BiPredicate<@Nullable LivingEntity, LivingEntity> ATTACKABLE_IGNORE_INVISIBILITY = ExtendedTargetingConditions.create().withFollowRange().skipInvisibilityCheck();
    private static final BiPredicate<@Nullable LivingEntity, LivingEntity> ATTACKABLE = ExtendedTargetingConditions.create().withFollowRange();
    private static final BiPredicate<@Nullable LivingEntity, LivingEntity> ATTACKABLE_NO_LOS_IGNORE_INVISIBILITY = ExtendedTargetingConditions.create().ignoreLineOfSight().withFollowRange().skipInvisibilityCheck();
    private static final BiPredicate<@Nullable LivingEntity, LivingEntity> ATTACKABLE_NO_LOS = ExtendedTargetingConditions.create().ignoreLineOfSight().withFollowRange();
    private static final BiPredicate<@Nullable LivingEntity, LivingEntity> VISIBLE_IGNORE_INVISIBILITY = ExtendedTargetingConditions.forLookTarget().withFollowRange().skipInvisibilityCheck();
    private static final BiPredicate<@Nullable LivingEntity, LivingEntity> VISIBLE = ExtendedTargetingConditions.forLookTarget().withFollowRange();
    //</editor-fold>

    /// Check whether the given target is considered 'targetable' based on sensory and status conditions such as teams and line of sight
    ///
    /// @return Whether the target is considered targetable
    public static boolean isEntityTargetable(LivingEntity attacker, LivingEntity target) {
        final BiPredicate<@Nullable LivingEntity, LivingEntity> predicate =
                BrainUtil.getTargetOfEntity(attacker) == target ? VISIBLE_IGNORE_INVISIBILITY : VISIBLE;

        return predicate.test(attacker, target);
    }

    /// Check whether the given target is considered 'attackable' based on sensory and status conditions such as teams and line of sight
    ///
    /// @return Whether the target is considered attackable
    public static boolean isEntityAttackable(LivingEntity attacker, LivingEntity target) {
        final BiPredicate<@Nullable LivingEntity, LivingEntity> predicate =
                BrainUtil.getTargetOfEntity(attacker) == target ? ATTACKABLE_IGNORE_INVISIBILITY : ATTACKABLE;

        return predicate.test(attacker, target);
    }

    /// Check whether the given target is considered 'attackable' based on sensory and status conditions such as teams and difficulty,
    /// but specifically excluding a line of sight check
    ///
    /// @return Whether the target is considered attackable
    public static boolean isEntityAttackableIgnoringLineOfSight(LivingEntity attacker, LivingEntity target) {
        final BiPredicate<@Nullable LivingEntity, LivingEntity> predicate =
                BrainUtil.getTargetOfEntity(attacker) == target ? ATTACKABLE_NO_LOS_IGNORE_INVISIBILITY : ATTACKABLE_NO_LOS;

        return predicate.test(attacker, target);
    }

    /// @return Whether the given target is currently visible to the entity
    public static boolean hasLineOfSight(LivingEntity entity, Entity target) {
        if (entity instanceof Mob mob)
            return mob.getSensing().hasLineOfSight(target);

        return entity.hasLineOfSight(target);
    }
}
