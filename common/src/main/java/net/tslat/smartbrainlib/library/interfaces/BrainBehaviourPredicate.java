package net.tslat.smartbrainlib.library.interfaces;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.core.behaviour.GroupBehaviour;

import java.util.Stack;
import java.util.function.Predicate;

/// A quad-value [Predicate] equivalent for testing the relevance of a [BehaviorControl] with additional context
///
/// Because [Brain]s often have multiple of the same `BehaviorControl`, it can be difficulty to determine the applicability of a _specific_
/// instance, due to a lack of associated information related to the position of the `behavior` within the brain.
///
/// This predicate aims to provide as much information as possible for determining accurate predication
@FunctionalInterface
public interface BrainBehaviourPredicate<T extends LivingEntity> {
	/// Tests whether the given behaviour is relevant to the predicate.
	///
	/// @param behaviour The behaviour to check
	/// @param activity The activity category the behaviour is under
	/// @param priority The priority the behaviour is nested under
	/// @param parentBehaviours The [GateBehaviour][net.minecraft.world.entity.ai.behavior.GateBehavior] or [GroupBehaviour][GroupBehaviour]
	///                        tree the behaviour is a child of, in order, if applicable
	/// @return Whether the behaviour is a correct match
	boolean isBehaviour(BehaviorControl<? super T> behaviour, Activity activity, int priority, Stack<BehaviorControl<? super T>> parentBehaviours);
}
