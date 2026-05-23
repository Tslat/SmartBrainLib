package net.tslat.smartbrainlib.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryMap;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.ActivityBuilder;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.base.GroupBehaviour;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.*;

/// Interface representing a builder of a [SmartBrain]
///
/// This class handles constructing a brain from scratch, holding the various methods to
/// collect the [activities][Activity], [behaviours][Behavior], [sensors][ExtendedSensor], and various other
/// miscellaneous components of the brain
///
/// Typically, [entities][Entity] that implement [SmartBrainOwner] will implement these methods directly; however, this interface
/// allows for external building to take place for alternative usage schemes
///
/// @param <BO> The brain owner entity that this brain is being built for
public interface SmartBrainBuilder<BO extends LivingEntity & SmartBrainOwner<BO>> {
    /// The list of [Sensors][ExtendedSensor] that your entity will be using
    ///
    /// Only supports `ExtendedSensor` implementations
    ///
    /// @param owner The entity owner of the brain being built
    /// @return A [List] of [Sensors][ExtendedSensor] that the entity will use to fill memories for tasks
    List<? extends ExtendedSensor<?>> getSensors(BO owner);

    /// Return the list of [behaviours][Behavior] representing the [Activity#CORE] behaviours the brain should always attempt to run
    ///
    /// [Behaviours][Behavior] in this category will always attempt to run regardless of what [Activity] the entity has active
    ///
    /// Typically, these behaviours would be ones such as moving towards the current walk target, floating on water, looking at the current look target, etc.
    ///
    /// @param owner The entity owner of the brain being built
    /// @return The list of behaviours for the [Activity#CORE] activity
    default List<? extends BehaviorControl<?>> getAlwaysRunningBehaviours(BO owner) {
        return List.of();
    }

    /// Return the list of [behaviours][Behavior] representing the [Activity#IDLE] behaviours the brain should attempt to run when no other
    /// `Activity` is running (excluding [Activity#CORE])
    ///
    /// Typically, these behaviours would be ones such as finding a new target, finding nearby look targets, idling, or setting idle walk targets
    ///
    /// @param owner The entity owner of the brain being built
    /// @return The list of behaviours that should always attempt to run
    default List<? extends BehaviorControl<?>> getIdleBehaviours(BO owner) {
        return List.of();
    }

    /// Return the list of [behaviours][Behavior] representing the [Activity#FIGHT] behaviours the brain should attempt to run when an [attack target][MemoryModuleType#ATTACK_TARGET]
    /// has been set
    ///
    /// These behaviours only run while a valid attack target memory has been set and automatically stops when the memory is cleared
    ///
    /// Typically, these behaviours would be ones such as checking to invalidate the current target, setting the walk target to the attack target, attacking, etc.
    ///
    /// @param owner The entity owner of the brain being built
    /// @return The list of behaviours that should always attempt to run
    default List<? extends BehaviorControl<?>> getFightingBehaviours(BO owner) {
        return List.of();
    }

    /// Get the [ActivityBuilder] representing the [Activity#CORE] behaviours the brain should always attempt to run
    ///
    /// [Behaviours][Behavior] in this category will always attempt to run regardless of what [Activity] the entity has active.<br/>
    /// This is the **<u>only</u>** activity group that runs this way
    ///
    /// Typically, this group would be used for behaviours such as moving towards the current walk target, floating on water, looking at the current look target, etc.
    ///
    /// @param owner The entity owner of the brain being built
    /// @return A [ActivityBuilder] containing the [Activity#CORE] behaviours for the brain
    /// @see #getAlwaysRunningBehaviours(LivingEntity)
    @SuppressWarnings({"rawtypes", "unchecked"})
    default ActivityBuilder<? extends BO> getCoreBehaviourGroup(BO owner) {
        return ActivityBuilder.create(Activity.CORE)
                .behaviourPriorityBase(0)
                .behaviours((List)getAlwaysRunningBehaviours(owner));
    }

    /// Get the [ActivityBuilder] representing the [Activity#IDLE] behaviours the brain should attempt to run when no other `Activity` is running (excluding [Activity#CORE])
    ///
    /// Typically, this group would be used for behaviours such as moving towards the current walk target, floating on water, looking at the current look target, etc.
    ///
    /// @param owner The entity owner of the brain being built
    /// @return A [ActivityBuilder] containing the [Activity#CORE] behaviours for the brain
    /// @see #getIdleBehaviours(LivingEntity)
    @SuppressWarnings({"rawtypes", "unchecked"})
    default ActivityBuilder<? extends BO> getIdleBehaviourGroup(BO owner) {
        return ActivityBuilder.create(Activity.IDLE)
                .behaviourPriorityBase(50)
                .behaviours((List)getIdleBehaviours(owner));
    }

    /// Get the [ActivityBuilder] representing the [Activity#FIGHT] behaviours the brain should attempt to run when a valid [attack target][MemoryModuleType#ATTACK_TARGET] has been set
    ///
    /// [Behaviours][Behavior] in this category will only run while a valid attack target memory has been set and automatically stops when the memory is cleared
    ///
    /// Typically, these behaviours would be ones such as checking to invalidate the current target, setting the walk target to the attack target, attacking, etc.
    ///
    /// @param owner The entity owner of the brain being built
    /// @return A [ActivityBuilder] containing the [Activity#CORE] behaviours for the brain
    /// @see #getFightingBehaviours(LivingEntity)
    @SuppressWarnings({"rawtypes", "unchecked"})
    default ActivityBuilder<? extends BO> getFightingBehaviourGroup(BO owner) {
        return ActivityBuilder.create(Activity.FIGHT)
                .behaviourPriorityBase(50)
                .behaviours((List)getFightingBehaviours(owner))
                .requireAndClearMemoriesOnUse(MemoryModuleType.ATTACK_TARGET);
    }

    /// Return the [ActivityBuilder] for the given [Activity] for this brain
    ///
    /// This method is called for all additional activities this brain uses other than the default 3 ([Activity#CORE], [Activity#IDLE], [Activity#FIGHT])
    default ActivityBuilder<? extends BO> getActivityGroupFor(Activity activity) {
        return ActivityBuilder.create(activity);
    }

    /// Get any additional [ActivityBuilder]s for other [Activity] categories not already covered by
    /// [#getCoreBehaviourGroup], [#getIdleBehaviourGroup], or [#getFightingBehaviourGroup]
    ///
    /// By default, SmartBrainLib only retrieves behaviours referenced in [SmartBrainOwner#getActivityActivationPriority()]
    default List<ActivityBuilder<? extends BO>> getAdditionalActivities(BO owner, Activity[] priorities) {
        if (priorities.length == 0)
            return List.of();

        final ObjectArrayList<ActivityBuilder<? extends BO>> additionalActivities = new ObjectArrayList<>(priorities.length);

        for (Activity activity : priorities) {
            final ActivityBuilder<? extends BO> activityGroup = getActivityGroupFor(activity);

            if (!activityGroup.isEmpty())
                additionalActivities.add(activityGroup);
        }

        return additionalActivities;
    }

    /// Override this to tell SmartBrainLib to ignore the [memory type cache][#COMPUTED_MEMORIES]
    ///
    /// By default, SmartBrainLib calculates and then caches the [MemoryModuleType]s used, to save the user needing to
    /// manually specify every memory used. To speed this up, a static cache is used for each [EntityType], since in most cases,
    /// [entities][Entity] use the same [Sensor]s and [Behavior]s for every instance.
    ///
    /// By returning false, SmartBrainLib will recalculate the memory list for every entity instance. This is less efficient but allows
    /// for dynamically assigned behaviours or sensors per-instance.
    default boolean usesCachedBehaviours() {
        return true;
    }

    /// Get the default [Activity] category that is used as a fallback, for when no other activity categories meet the conditions to run.
    ///
    /// This is almost always left as [Activity#IDLE], but it can be modified as needed.
    ///
    /// @return The activity to use as a fallback
    default Activity getDefaultActivity(BO owner) {
        return Activity.IDLE;
    }

    /// Get the [activities][Activity] that should always be running, regardless of any other conditions or situations
    ///
    /// This is usually just left as [Activity#CORE], but it can be modified as needed
    ///
    /// @return The set of activities that should always run
    default Set<Activity> getAlwaysRunningActivities(BO owner) {
        return Set.of(Activity.CORE);
    }

    /// Get the time-based activity schedule for this [SmartBrainOwner], if any
    ///
    /// Ideally, this would be a statically cached instance
    ///
    /// @return The schedule for the brain, or null if no schedule
    default @Nullable SmartBrainSchedule<BO, ?> getSchedule() {
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="<Boilerplate>">
    Map<EntityType<?>, MemoryModuleType<?>[]> COMPUTED_MEMORIES = new Reference2ObjectOpenHashMap<>();

    /// Construct the brain for the given entity for the brain being built
    ///
    /// Generally speaking, you shouldn't need to override this
    @ApiStatus.Internal
    default SmartBrain<BO> makeBrain(BO owner, Brain.Packed packedBrain) {
        try {
            @SuppressWarnings("unchecked")
            final List<? extends ExtendedSensor<BO>> sensors = (List<? extends ExtendedSensor<BO>>)getSensors(owner);
            final List<ActivityBuilder<BO>> activities = compileActivities(owner);
            final MemoryModuleType<?>[] memories = compileMemories(owner, activities, sensors);
            final SmartBrainSchedule<BO, ?> schedule = getSchedule();

            return makeBrain(owner, sensors, activities, memories, schedule, packedBrain);
        }
        catch (ClassCastException e) {
            SBLConstants.LOGGER.get().error("SmartBrainBuilder failed to create brain, an invalid sensor or activity was provided: {}", e.getMessage());

            return makeBrain(owner, List.of(), List.of(), new MemoryModuleType[0], null, Brain.Packed.EMPTY);
        }
    }

    /// Construct the brain for the given entity for the brain being built
    ///
    /// Generally speaking, you shouldn't need to override this
    @ApiStatus.Internal
    default SmartBrain<BO> makeBrain(BO owner, List<? extends ExtendedSensor<BO>> sensors, List<ActivityBuilder<BO>> activities, MemoryModuleType<?>[] memories, @Nullable SmartBrainSchedule<BO, ?> schedule, Brain.Packed packedBrain) {
        final SmartBrain<BO> brain = SBLConstants.PLATFORM.makeBrain(ObjectArrayList.wrap(memories), sensors, activities, schedule, owner.getRandom());

        brain.setCoreActivities(getAlwaysRunningActivities(owner));
        brain.setDefaultActivity(getDefaultActivity(owner));
        brain.useDefaultActivity();

        for (MemoryMap.Value<?> memory : packedBrain.memories()) {
            brain.setMemoryInternal(memory);
        }

        return brain;
    }

    /// Compile the list of [ActivityBuilder]s for the brain being built
    ///
    /// Generally speaking, you shouldn't need to override this
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.Internal
    default List<ActivityBuilder<BO>> compileActivities(BO owner) throws ClassCastException {
        final Activity[] activityPriorities = owner.getActivityActivationPriority();
        final List<ActivityBuilder<? extends BO>> additionalActivities = getAdditionalActivities(owner, activityPriorities);
        final List<ActivityBuilder<? extends BO>> activities = new ObjectArrayList<>(3 + additionalActivities.size());

        activities.add(getCoreBehaviourGroup(owner));
        activities.add(getIdleBehaviourGroup(owner));
        activities.add(getFightingBehaviourGroup(owner));
        activities.addAll(additionalActivities);

        return (List)activities;
    }

    /// Compile the list [MemoryModuleType]s for the brain being built
    ///
    /// Generally speaking, you shouldn't need to override this
    @ApiStatus.Internal
    default MemoryModuleType<?>[] compileMemories(BO owner, List<ActivityBuilder<BO>> activities, List<? extends ExtendedSensor<BO>> sensors) {
        if (usesCachedBehaviours())
            return COMPUTED_MEMORIES.computeIfAbsent(owner.getType(), _ -> computeUsedMemories(activities, sensors));

        return computeUsedMemories(activities, sensors);
    }

    /// Compute the used [MemoryModuleType]s for the provided sensors and activities
    @ApiStatus.Internal
    private static <BO extends LivingEntity & SmartBrainOwner<BO>> MemoryModuleType<?>[] computeUsedMemories(List<ActivityBuilder<BO>> activities, List<? extends ExtendedSensor<BO>> sensors) {
        final Set<MemoryModuleType<?>> memoryTypes = new ReferenceOpenHashSet<>();

        for (ActivityBuilder<BO> activity : activities) {
            for (BehaviorControl<?> behavior : activity.getBehaviours()) {
                memoryTypes.addAll(getMemoriesUsedByBehaviour(behavior));
            }
        }

        for (ExtendedSensor<BO> sensor : sensors) {
            memoryTypes.addAll(sensor.memoriesUsed());
        }

        return memoryTypes.toArray(new MemoryModuleType[0]);
    }

    /// Get the full collection of [MemoryModuleType]s used by a given [BehaviorControl], including any potential child behaviours
    @ApiStatus.Internal
    static Collection<MemoryModuleType<?>> getMemoriesUsedByBehaviour(BehaviorControl<?> behaviour) {
        return switch (behaviour) {
            case GateBehavior<?> group -> {
                final Set<MemoryModuleType<?>> memoryTypes = new ReferenceOpenHashSet<>();

                for (BehaviorControl<?> child : group.behaviors) {
                    memoryTypes.addAll(child.getRequiredMemories());
                }

                yield memoryTypes;
            }
            case GroupBehaviour<?> group -> {
                final Set<MemoryModuleType<?>> memoryTypes = new ReferenceOpenHashSet<>();

                for (ExtendedBehaviour<?> child : group.getBehaviours()) {
                    memoryTypes.addAll(child.getRequiredMemories());
                }

                yield memoryTypes;
            }
            case Behavior<?> single -> single.entryCondition.keySet();
            default -> Collections.emptyList();
        };
    }
    //</editor-fold>
}
