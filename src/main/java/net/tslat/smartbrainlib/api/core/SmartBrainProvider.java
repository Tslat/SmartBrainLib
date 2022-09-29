package net.tslat.smartbrainlib.api.core;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.MultiTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraftforge.fml.loading.FMLLoader;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.behaviour.GroupBehaviour;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

public class SmartBrainProvider<E extends LivingEntity & SmartBrainOwner<E>> extends Brain.BrainCodec<E> {
	private static final Map<EntityType<? extends LivingEntity>, ImmutableList<MemoryModuleType<?>>> BRAIN_MEMORY_CACHE = new Object2ObjectOpenHashMap<>();

	private final E owner;

	private final boolean saveMemories;
	private final boolean nonStaticMemories;

	/**
	 * @param owner The owner of the brain
	 */
	public SmartBrainProvider(E owner) {
		this(owner, false);
	}
	/**
	 * @param owner The owner of the brain
	 * @param nonStaticMemories Whether the entity has different behaviours or sensors depending on the entity instance
	 */
	public SmartBrainProvider(E owner, boolean nonStaticMemories) {
		this(owner, false, nonStaticMemories);
	}

	/**
	 * @param owner The owner of the brain
	 * @param saveMemories Whether memory states should be saved & loaded when the entity is saved or loaded.
	 * @param nonStaticMemories Whether the entity has different behaviours or sensors depending on the entity instance
	 */
	public SmartBrainProvider(E owner, boolean saveMemories, boolean nonStaticMemories) {
		super(new ArrayList<>(), new ArrayList<>());

		this.owner = owner;
		this.saveMemories = saveMemories;
		this.nonStaticMemories = nonStaticMemories;
	}

	/**
	 * Creates a new brain instance based on the provider conditions.
	 * @param codecLoader Codec loader instance for serialized data loading of a pre-existing brain
	 * @return The new brain instance, or null if on client
	 */
	@Nullable
	@Override
	public final SmartBrain<E> makeBrain(Dynamic<?> codecLoader) {
		if (owner.level.isClientSide())
			return null;

		List<ExtendedSensor<E>> sensors = owner.getSensors();
		List<BrainActivityGroup<E>> taskList = compileTasks();
		ImmutableList<MemoryModuleType<?>> memories;

		if (!this.nonStaticMemories && BRAIN_MEMORY_CACHE.containsKey(owner.getType())) {
			memories = BRAIN_MEMORY_CACHE.get(owner.getType());
		}
		else {
			memories = createMemoryList(taskList, sensors);

			if (!this.nonStaticMemories)
				BRAIN_MEMORY_CACHE.put((EntityType<? extends LivingEntity>)this.owner.getType(), memories);
		}

		SmartBrain<E> brain = new SmartBrain<>(memories, sensors, taskList, this.saveMemories);

		finaliseBrain(brain);
		sanityCheckBrainState(brain);

		return brain;
	}

	private ImmutableList<MemoryModuleType<?>> createMemoryList(List<BrainActivityGroup<E>> taskList, List<? extends ExtendedSensor<?>> sensors) {
		Set<MemoryModuleType<?>> memoryTypes = new ObjectOpenHashSet<>();

		taskList.forEach(activityGroup -> activityGroup.getBehaviours().forEach(behavior -> collectMemoriesFromTask(memoryTypes, behavior)));
		sensors.forEach(sensor -> memoryTypes.addAll(sensor.memoriesUsed()));

		return ImmutableList.copyOf(memoryTypes);
	}

	private void collectMemoriesFromTask(Set<MemoryModuleType<?>> memories, Task<?> behaviour) {
		if (behaviour instanceof MultiTask<?>) {
			((MultiTask)behaviour).behaviors.stream().forEach(subBehaviour -> collectMemoriesFromTask(memories, (Task<?>) subBehaviour));
		}
		else if (behaviour instanceof GroupBehaviour<?>) {
			((GroupBehaviour<?>) behaviour).getBehaviours().forEachRemaining(subBehaviour -> collectMemoriesFromTask(memories, subBehaviour));
		}
		else {
			memories.addAll(behaviour.entryCondition.keySet());
		}
	}

	private List<BrainActivityGroup<E>> compileTasks() {
		List<BrainActivityGroup<E>> tasks = new ObjectArrayList<>();
		BrainActivityGroup<E> activityGroup;

		if (!(activityGroup = owner.getCoreTasks()).getBehaviours().isEmpty())
			tasks.add(activityGroup);

		if (!(activityGroup = owner.getIdleTasks()).getBehaviours().isEmpty())
			tasks.add(activityGroup);

		if (!(activityGroup = owner.getFightTasks()).getBehaviours().isEmpty())
			tasks.add(activityGroup);

		tasks.addAll(owner.getAdditionalTasks().values());

		return tasks;
	}

	private void finaliseBrain(SmartBrain<E> brain) {
		brain.setCoreActivities(this.owner.getAlwaysRunningActivities());
		brain.setDefaultActivity(this.owner.getDefaultActivity());
		brain.useDefaultActivity();
		this.owner.handleAdditionalBrainSetup(brain);
	}

	private void sanityCheckBrainState(SmartBrain<E> brain) {
		if (!FMLLoader.isProduction()) {
			SmartBrainLib.LOGGER.log(Level.INFO, "SmartBrainLib checking brain state for " + this.owner.toString() + ". This will only occur while in debug mode");

			for (Activity activity : brain.coreActivities) {
				if (!brain.activityRequirements.containsKey(activity))
					SmartBrainLib.LOGGER.log(Level.WARN, "Entity " + this.owner.toString() + " has " + activity.toString() + " listed as a core activity, but no behaviours for this activity have been registered.");
			}
		}
	}

	protected void addActivity(SmartBrain<E> brain, Activity activity, BrainActivityGroup<E> activityGroup) {
		brain.activityRequirements.put(activity, activityGroup.getActivityStartMemoryConditions());

		if (!activityGroup.getWipedMemoriesOnFinish().isEmpty())
			brain.activityMemoriesToEraseWhenStopped.put(activity, activityGroup.getWipedMemoriesOnFinish());

		for (Pair<Integer, ? extends Task<? super E>> pair : activityGroup.pairBehaviourPriorities()) {
			brain.addBehaviour(pair.getFirst(), activity, pair.getSecond());
		}
	}
}
