package net.tslat.smartbrainlib.core;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.fml.loading.FMLLoader;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmartBrainProvider<T extends LivingEntity & SmartBrainOwner<T>> extends Brain.Provider<T> {
	private static final Map<EntityType<? extends LivingEntity>, ImmutableList<MemoryModuleType<?>>> BRAIN_MEMORY_CACHE = new Object2ObjectOpenHashMap<>();

	private final T owner;

	private final boolean saveMemories;
	private final boolean nonStaticMemories;

	/**
	 * @param owner The owner of the brain
	 */
	public SmartBrainProvider(T owner) {
		this(owner, false);
	}
	/**
	 * @param owner The owner of the brain
	 * @param nonStaticMemories Whether the entity has different behaviours or sensors depending on the entity instance
	 */
	public SmartBrainProvider(T owner, boolean nonStaticMemories) {
		this(owner, false, nonStaticMemories);
	}

	/**
	 * @param owner The owner of the brain
	 * @param saveMemories Whether memory states should be saved & loaded when the entity is saved or loaded.
	 * @param nonStaticMemories Whether the entity has different behaviours or sensors depending on the entity instance
	 */
	public SmartBrainProvider(T owner, boolean saveMemories, boolean nonStaticMemories) {
		super(List.of(), List.of());

		this.owner = owner;
		this.saveMemories = saveMemories;
		this.nonStaticMemories = nonStaticMemories;
	}

	@Override
	public final Brain<T> makeBrain(Dynamic<?> codecLoader) {
		List<ExtendedSensor<T>> sensors = owner.getSensors();
		Map<Activity, BrainActivityGroup<T>> taskList = compileTasks();
		ImmutableList<MemoryModuleType<?>> memories;

		if (!this.nonStaticMemories && BRAIN_MEMORY_CACHE.containsKey(owner.getType())) {
			memories = BRAIN_MEMORY_CACHE.get(owner.getType());
		}
		else {
			memories = createMemoryList(taskList, sensors);

			if (!this.nonStaticMemories)
				BRAIN_MEMORY_CACHE.put((EntityType<? extends LivingEntity>)this.owner.getType(), memories);
		}

		SmartBrain<T> brain = new SmartBrain<>(memories, sensors, this.saveMemories);

		applyTasks(brain, taskList);
		sanityCheckBrainState(brain);

		return brain;
	}

	private ImmutableList<MemoryModuleType<?>> createMemoryList(Map<Activity, BrainActivityGroup<T>> taskList, List<? extends ExtendedSensor<?>> sensors) {
		Set<MemoryModuleType<?>> memoryTypes = new ObjectOpenHashSet<>();

		taskList.forEach((activity, behaviourGroup) -> behaviourGroup.getBehaviours().forEach(behaviour -> collectMemoriesFromTask(memoryTypes, behaviour)));

		sensors.forEach(sensor -> memoryTypes.addAll(sensor.memoriesUsed()));

		return ImmutableList.copyOf(memoryTypes);
	}

	private void collectMemoriesFromTask(Set<MemoryModuleType<?>> memories, Behavior<?> behaviour) {
		if (behaviour instanceof GateBehavior<?> gateBehavior) {
			gateBehavior.behaviors.stream().forEach(subBehaviour -> collectMemoriesFromTask(memories, subBehaviour));
		}
		else {
			memories.addAll(behaviour.entryCondition.keySet());
		}
	}

	private Map<Activity, BrainActivityGroup<T>> compileTasks() {
		Map<Activity, BrainActivityGroup<T>> map = new Object2ObjectOpenHashMap<>();
		BrainActivityGroup<T> activityGroup;

		if (!(activityGroup = owner.getCoreTasks()).getBehaviours().isEmpty())
			map.put(Activity.CORE, activityGroup);

		if (!(activityGroup = owner.getIdleTasks()).getBehaviours().isEmpty())
			map.put(Activity.IDLE, activityGroup);

		if (!(activityGroup = owner.getFightTasks()).getBehaviours().isEmpty())
			map.put(Activity.FIGHT, activityGroup);

		map.putAll(owner.getAdditionalTasks());

		return map;
	}

	private void applyTasks(Brain<T> brain, Map<Activity, BrainActivityGroup<T>> taskList) {
		for (Map.Entry<Activity, BrainActivityGroup<T>> tasksEntry : taskList.entrySet()) {
			addActivity(brain, tasksEntry.getKey(), tasksEntry.getValue());
		}

		brain.setCoreActivities(this.owner.getAlwaysRunningActivities());
		brain.setDefaultActivity(this.owner.getDefaultActivity());
		brain.useDefaultActivity();
		this.owner.handleAdditionalBrainSetup(brain);
	}

	private void sanityCheckBrainState(SmartBrain<T> brain) {
		if (!FMLLoader.isProduction()) {
			SmartBrainLib.LOGGER.log(Level.INFO, "SmartBrainLib checking brain state for " + this.owner.toString() + ". This will only occur while in debug mode");

			for (Activity activity : brain.coreActivities) {
				if (!brain.activityRequirements.containsKey(activity))
					SmartBrainLib.LOGGER.log(Level.WARN, "Entity " + this.owner.toString() + " has " + activity.toString() + " listed as a core activity, but no behaviours for this activity have been registered.");
			}
		}
	}

	private void addActivity(Brain<T> brain, Activity activity, BrainActivityGroup<T> activityGroup) {
		brain.activityRequirements.put(activity, activityGroup.getActivityStartMemoryConditions());

		if (activityGroup.getWipedMemoriesOnFinish() != null)
			brain.activityMemoriesToEraseWhenStopped.put(activity, activityGroup.getWipedMemoriesOnFinish());

		for (Pair<Integer, ? extends Behavior<? super T>> pair : activityGroup.pairBehaviourPriorities()) {
			brain.availableBehaviorsByPriority.computeIfAbsent(pair.getFirst(), priority -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(activity, activityKey -> new ObjectOpenHashSet<>()).add(pair.getSecond());
		}
	}
}
