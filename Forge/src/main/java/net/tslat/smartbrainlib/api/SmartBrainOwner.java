package net.tslat.smartbrainlib.api;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.APIOnly;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrain;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implement this class for any entity you want to use the SmartBrain system.
 * <p>
 * This interface contains the helper and constructive methods for initialising your entity's brain.
 *
 * @param <T> Your entity
 */
public interface SmartBrainOwner<T extends LivingEntity & SmartBrainOwner<T>> {
	/**
	 * The list of {@link ExtendedSensor Sensors} that your entity will be using.
	 * <p>
	 * Only supports ExtendedSensors.
	 *
	 * @return A {@link List} of {@link ExtendedSensor Sensors} that the entity will use to fill memories for tasks.
	 */
	List<? extends ExtendedSensor<? extends T>> getSensors();

	/**
	 * Override this for tasks that ideally should always be running, regardless of anything else.
	 * <p>
	 * Usually you'd use this for things like moving towards the current target, floating on water, looking at a certain target, etc.
	 * <p>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 * <p>
	 * Tasks returned in this category take up the {@link Activity#CORE} activity category
	 *
	 * @return a {@link BrainActivityGroup} containing the <i>core</i> tasks your entity should run.
	 */
	default BrainActivityGroup<? extends T> getCoreTasks() {
		return BrainActivityGroup.empty();
	}

	/**
	 * Override this for tasks that would normally run when your entity is doing nothing else.
	 * <p>
	 * Usually you'd use this for things like random walking, setting targets, or just standing still and doing nothing at all.
	 * <p>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 * <p>
	 * Tasks returned in this category take up the {@link Activity#IDLE} activity category
	 *
	 * @return a {@link BrainActivityGroup} containing the <i>idle</i> tasks your entity should run.
	 */
	default BrainActivityGroup<? extends T> getIdleTasks() {
		return BrainActivityGroup.empty();
	}

	/**
	 * Override this to add the tasks that would normally run when your entity attacking something, or is otherwise in combat.
	 * <p>
	 * Usually you'd use this for things melee attacking, invalidating attack targets, or setting walk targets based off the current attack target.
	 * <p>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 * <p>
	 * Tasks returned in this category take up the {@link Activity#FIGHT} activity category
	 *
	 * @return a {@link BrainActivityGroup} containing the <i>fight</i> tasks your entity should run.
	 */
	default BrainActivityGroup<? extends T> getFightTasks() {
		return BrainActivityGroup.empty();
	}

	/**
	 * Override this to add any additional tasks that don't fit into the categories already handled in the pre-defined activity task methods.
	 * <p>
	 * Like all task groups, this method is <i>optional</i>, if you have no tasks that apply to this category
	 *
	 * @return a {@link Map} of Activities to BrainActivityGroups group containing the additional tasks your entity should run.
	 */
	default Map<Activity, BrainActivityGroup<? extends T>> getAdditionalTasks() {
		return new Object2ObjectOpenHashMap<>(0);
	}

	/**
	 * The activity categories that should always be running, regardless of any other conditions or situations.
	 * <p>
	 * This is usually just left as {@link Activity#CORE}, but it can be modified as needed
	 *
	 * @return A {@link Set} of {@link Activity Activities}
	 */
	default Set<Activity> getAlwaysRunningActivities() {
		return ImmutableSet.of(Activity.CORE);
	}

	/**
	 * The activity category that is used as a fallback, for when no other activity categories meet the conditions to run.
	 * <p>
	 * This is almost always left as {@link Activity#IDLE}, but it can be modified as needed.
	 *
	 * @return The {@link Activity} to use as a fallback
	 */
	default Activity getDefaultActivity() {
		return Activity.IDLE;
	}

	/**
	 * Override this to return the order of activity categories the brain should attempt to run things in.
	 * <p>
	 * The list is ordered in order of insertion - I.E. earlier elements have higher priority
	 *
	 * @return An <b>ordered</b> {@link List} of {@link Activity} categories
	 */
	default List<Activity> getActivityPriorities() {
		return ObjectArrayList.of(Activity.FIGHT, Activity.IDLE);
	}

	/**
	 * Override this to return a set of activities that should be prioritised over scheduled activities.
	 * <p>
	 * Activities listed here will be selected even if a {@link SmartBrainSchedule schedule} determines another activity is valid.
	 *
	 * @return A {@link Set} of {@link Activity} categories
	 */
	default Set<Activity> getScheduleIgnoringActivities() {
		return ObjectArraySet.of(Activity.FIGHT);
	}

	/**
	 * Override this to do any additional work after the brain has been built and readied.
	 * <p>
	 * By this stage, the brain has had all its memories, sensors, activities, and priorities set.
	 *
	 * @param brain The brain that the entity will be using.
	 */
	default void handleAdditionalBrainSetup(SmartBrain<? extends T> brain) {}

	/**
	 * Override this to return the {@link net.minecraft.world.entity.schedule.Schedule schedule} for your entity.
	 * <p>
	 * This can be set at any time via {@link SmartBrain#setSchedule(SmartBrainSchedule)}, but it's recommended to
	 * do so statically if possible and provide it through this method
	 *
	 * @return The schedule for the brain, or null if no schedule
	 */
	@Nullable
	default SmartBrainSchedule getSchedule() {
		return null;
	}

	/**
	 * SmartBrainOwners <b><u>MUST</u></b> call this from the entity's {@link LivingEntity#serverAiStep}, or {@link Mob#customServerAiStep} if extending {@link Mob}.
	 * <p>
	 * Brains should only be ticked <b>server side</b>.
	 * <p>
	 * This method does not need to be overridden.
	 *
	 * @param entity The brain owner
	 */
	@APIOnly
	default void tickBrain(T entity) {
		((Brain<T>)entity.getBrain()).tick((ServerLevel)entity.level(), entity);
	}
}
