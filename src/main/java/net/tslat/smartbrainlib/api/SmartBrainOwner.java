package net.tslat.smartbrainlib.api;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.core.BrainActivityGroup;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implement this class for any entity you want to use the SmartBrain system. <br/>
 * This interface contains the helper and constructive methods for initialising your entity's brain.
 *
 * @param <T> Your entity
 */
public interface SmartBrainOwner<T extends LivingEntity & SmartBrainOwner<T>> {
	///**
	// * Retrieves the instance of the {@link SmartBrainHandler Brain Handler} your entity is using for its brain. <br/>
	// * This is the same instance that you should have stored in {@link SmartBrainOwner#setBrainHandler(SmartBrainHandler)}
	// *
	// * @return The cached instance of your entity's brain handler
	// */
	//@Nullable
	//SmartBrainHandler<T> getBrainHandler();
//
	///**
	// * Provides the instantiated {@link SmartBrainHandler Brain Handler} for your entity's brain. <br/>
	// * Store this instance in your entity's class in an instance-field for retrieval later.
	// * @see SmartBrainOwner#getBrainHandler
	// *
	// * @param handler The handler instance to cache in your entity.
	// */
	//void setBrainHandler(SmartBrainHandler<T> handler);

	/**
	 * The list of {@link Sensor Sensors} that your entity will be using. <br/>
	 * I don't yet have a cleaner way of implementing this, so some experimentation might be required if your entity isn't behaving properly. <br/>
	 * As a general guide, I'd recommend using {@link SensorType#NEAREST_LIVING_ENTITIES}, {@link SensorType#HURT_BY}, and {@link SensorType#NEAREST_PLAYERS} for most hostile mobs. <br/>
	 * <br/>
	 * Bonus points for using a statically-initialised List
	 * @return A {@link List} of {@link Sensor Sensors} that the entity will use to fill memories for tasks.
	 */
	List<SensorType<? extends Sensor<? super T>>> getSensors();

	/**
	 * Override this for tasks that ideally should always be running, regardless of anything else. <br/>
	 * Usually you'd use this for things like moving towards the current target, floating on water, looking at a certain target, etc. <br/>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 * <br/><br/>
	 * Tasks returned in this category take up the {@link net.minecraft.world.entity.schedule.Activity#CORE} activity category
	 *
	 * @return a {@link net.tslat.smartbrainlib.core.BrainActivityGroup} containing the <i>core</i> tasks your entity should run.
	 */
	default BrainActivityGroup<T> getCoreTasks() {
		return BrainActivityGroup.empty();
	}

	/**
	 * Override this for tasks that would normally run when your entity is doing nothing else. <br/>
	 * Usually you'd use this for things like random walking, setting targets, or just standing still and doing nothing at all. <br/>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 * <br/><br/>
	 * Tasks returned in this category take up the {@link net.minecraft.world.entity.schedule.Activity#IDLE} activity category
	 *
	 * @return a {@link net.tslat.smartbrainlib.core.BrainActivityGroup} containing the <i>idle</i> tasks your entity should run.
	 */
	default BrainActivityGroup<T> getIdleTasks() {
		return BrainActivityGroup.empty();
	}

	/**
	 * Override this to add the tasks that would normally run when your entity attacking something, or is otherwise in combat. <br/>
	 * Usually you'd use this for things melee attacking, invalidating attack targets, or setting walk targets based off the current attack target. <br/>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 * <br/><br/>
	 * Tasks returned in this category take up the {@link net.minecraft.world.entity.schedule.Activity#FIGHT} activity category
	 *
	 * @return a {@link net.tslat.smartbrainlib.core.BrainActivityGroup} containing the <i>fight</i> tasks your entity should run.
	 */
	default BrainActivityGroup<T> getFightTasks() {
		return BrainActivityGroup.empty();
	}

	/**
	 * Override this to add any additional tasks that don't fit into the categories already handled in the pre-defined activity task methods. <br/>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 *
	 * @return a {@link java.util.Map} of Activities to BrainActivityGroups group containing the additional tasks your entity should run.
	 */
	default Map<Activity, BrainActivityGroup<T>> getAdditionalTasks() {
		return new Object2ObjectOpenHashMap<>(0);
	}

	/**
	 * The activity categories that should always be running, regardless of any other conditions or situations. <br/>
	 * This is usually just left as {@link Activity#CORE}, but it can be modified as needed
	 *
	 * @return A {@link java.util.Set} of {@link Activity Activities}
	 */
	default Set<Activity> getAlwaysRunningActivities() {
		return ImmutableSet.of(Activity.CORE);
	}

	/**
	 * The activity category that is used as a fallback, for when no other activity categories meet the conditions to run. <br/>
	 * This is almost always left as {@link Activity#IDLE}, but it can be modified as needed.
	 *
	 * @return The {@link Activity} to use as a fallback
	 */
	default Activity getDefaultActivity() {
		return Activity.IDLE;
	}

	/**
	 * Override this to return the order of activity categories the brain should attempt to run things in. <br/>
	 * The list is ordered in order of insertion - I.E. earlier elements have higher priority
	 *
	 * @return An <b>ordered</b> {@link List} of {@link Activity} categories
	 */
	default List<Activity> getActivityPriorities() {
		return ObjectArrayList.of(Activity.FIGHT, Activity.IDLE);
	}

	/**
	 * Override this to do any additional work after the brain has been built and readied. <br/>
	 * By this stage, the brain has had all its memories, sensors, activities, and priorities set.
	 *
	 * @param brain The brain that the entity will be using.
	 */
	default void handleAdditionalBrainSetup(Brain<T> brain) {}
}
