package net.tslat.smartbrainlib.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.core.behaviour.base.GroupBehaviour;
import net.tslat.smartbrainlib.library.interfaces.BrainBehaviourPredicate;

import java.util.*;

/// Helper class for various functions related to [BehaviorControl]s
public final class BehaviourUtil {
    /// Check whether a behaviour is a child of a provided parent behaviour, including potentially recursively nested parents
    ///
    /// Typically used to check higher-order behaviours for parent inheritance
    ///
    /// @param behaviour The behaviour to check
    /// @param parent The [GateBehaviour][net.minecraft.world.entity.ai.behavior.GateBehavior] or [GroupBehaviour][GroupBehaviour]
    ///                        tree the behaviour is a child of, in order, if applicable
    /// @return Whether the behaviour is a child of the parent behaviour
    public static boolean isChildOfBehaviour(BehaviorControl<?> behaviour, BehaviorControl<?> parent) {
        for (BehaviorControl<?> childBehaviour : getChildrenOfBehaviour(parent)) {
            if (behaviour == childBehaviour || isChildOfBehaviour(behaviour, childBehaviour))
                return true;
        }

        return false;
    }

    /// Find a matching [BehaviorControl] in the [Brain] of the provided [LivingEntity] matching the conditions in a [predicate][BrainBehaviourPredicate]
    public static <T extends LivingEntity> Optional<BehaviorControl<? super T>> findBehaviour(T entity, BrainBehaviourPredicate<T> predicate) {
        //noinspection unchecked
        return findBehaviour((Brain<T>)entity.getBrain(), predicate);
    }

    /// Find a matching [BehaviorControl] in the provided brain matching the conditions in a [predicate][BrainBehaviourPredicate]
    public static <T extends LivingEntity> Optional<BehaviorControl<? super T>> findBehaviour(Brain<T> brain, BrainBehaviourPredicate<T> predicate) {
        for (Map.Entry<Integer, Map<Activity, Set<BehaviorControl<? super T>>>> priorityListing : brain.availableBehaviorsByPriority.entrySet()) {
            final int priority = priorityListing.getKey();

            for (Map.Entry<Activity, Set<BehaviorControl<? super T>>> activityGroup : priorityListing.getValue().entrySet()) {
                final Activity activity = activityGroup.getKey();

                for (BehaviorControl<? super T> behaviour : activityGroup.getValue()) {
                    Optional<BehaviorControl<? super T>> matchedBehaviour = findMatchingBehaviour(behaviour, activity, priority, new Stack<>(), predicate);

                    if (matchedBehaviour.isPresent())
                        return matchedBehaviour;
                }
            }
        }

        return Optional.empty();
    }

    /// Get all direct child behaviours of a [BehaviorControl], if any exist
    ///
    /// @see GateBehavior
    /// @see GroupBehaviour
    public static <T extends LivingEntity> Iterable<? extends BehaviorControl<? super T>> getChildrenOfBehaviour(BehaviorControl<? super T> behaviour) {
        return switch (behaviour) {
            case GateBehavior<? super T> gateBehaviour -> gateBehaviour.behaviors;
            case GroupBehaviour<? super T> groupBehaviour -> groupBehaviour.getBehaviours();
            default -> List.of();
        };
    }

    //<editor-fold defaultstate="collapsed" desc="<Internal Methods>">
    /// Recursively attempt to match a [BehaviorControl] matching a [BrainBehaviourPredicate]
    private static <T extends LivingEntity> Optional<BehaviorControl<? super T>> findMatchingBehaviour(BehaviorControl<? super T> behaviour, Activity activity, int priority,
                                                                                                       Stack<BehaviorControl<? super T>> parentStack, BrainBehaviourPredicate<T> predicate) {
        if (predicate.isBehaviour(behaviour, activity, priority, parentStack))
            return Optional.of(behaviour);

        parentStack.push(behaviour);

        for (BehaviorControl<? super T> childBehaviour : getChildrenOfBehaviour(behaviour)) {
            Optional<BehaviorControl<? super T>> matchingBehaviour = findMatchingBehaviour(childBehaviour, activity, priority, parentStack, predicate);

            if (matchingBehaviour.isPresent())
                return matchingBehaviour;
        }

        parentStack.pop();

        return Optional.empty();
    }
    //</editor-fold>
}
