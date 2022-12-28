package net.tslat.smartbrainlib.object;

import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.core.behaviour.GroupBehaviour;

import javax.annotation.Nullable;

/**
 * Functional interface to handle passing multiple arguments back for behaviour-predication handling
 */
@FunctionalInterface
public interface BrainBehaviourPredicate {
	/**
	 * Tests whether the given behaviour is relevant to the predicate.
	 * @param priority The priority the behaviour is nested under
	 * @param activity The activity category the behaviour is under
	 * @param behaviour The behaviour to check
	 * @param parentBehaviour The {@link net.minecraft.world.entity.ai.behavior.GateBehavior GateBehaviour} or {@link GroupBehaviour GroupBehaviour}
	 *                        the behaviour is a child of, if applicable
	 */
	boolean isBehaviour(int priority, Activity activity, Behavior<?> behaviour, @Nullable Behavior<?> parentBehaviour);
}
