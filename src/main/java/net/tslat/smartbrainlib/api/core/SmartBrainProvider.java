package net.tslat.smartbrainlib.api.core;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.behaviour.GroupBehaviour;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The provider of {@link SmartBrain SmartBrains}. All entities intending to
 * utilise this library should return a new instance of this in
 * {@link LivingEntity#brainProvider()} <br>
 * All entities that use this provider use SmartBrains.
 * 
 * @param <E> The entity
 */
public class SmartBrainProvider<E extends LivingEntity & SmartBrainOwner<E>> extends Brain.Provider<E> {
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
	 * @param owner             The owner of the brain
	 * @param nonStaticMemories Whether the entity has different behaviours or
	 *                          sensors depending on the entity instance
	 */
	public SmartBrainProvider(E owner, boolean nonStaticMemories) {
		this(owner, false, nonStaticMemories);
	}

	/**
	 * @param owner             The owner of the brain
	 * @param saveMemories      Whether memory states should be saved and loaded when
	 *                          the entity is saved or loaded.
	 * @param nonStaticMemories Whether the entity has different behaviours or
	 *                          sensors depending on the entity instance
	 */
	public SmartBrainProvider(E owner, boolean saveMemories, boolean nonStaticMemories) {
		super(List.of(), List.of());

		this.owner = owner;
		this.saveMemories = saveMemories;
		this.nonStaticMemories = nonStaticMemories;
	}

	@Override
	public final SmartBrain<E> makeBrain(Dynamic<?> codecLoader) {
		List<ExtendedSensor<E>> sensors = owner.getSensors();
		List<BrainActivityGroup<E>> taskList = compileTasks();
		ImmutableList<MemoryModuleType<?>> memories;

		if (!this.nonStaticMemories && BRAIN_MEMORY_CACHE.containsKey(owner.getType())) {
			memories = BRAIN_MEMORY_CACHE.get(owner.getType());
		}
		else {
			memories = createMemoryList(taskList, sensors);

			if (!this.nonStaticMemories)
				BRAIN_MEMORY_CACHE.put((EntityType<? extends LivingEntity>) this.owner.getType(), memories);
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

	private void collectMemoriesFromTask(Set<MemoryModuleType<?>> memories, Behavior<?> behaviour) {
		if (behaviour instanceof GateBehavior<?> gateBehaviour) {
			gateBehaviour.behaviors.stream().forEach(subBehaviour -> collectMemoriesFromTask(memories, subBehaviour));
		}
		else if (behaviour instanceof GroupBehaviour<?> groupBehaviour) {
			groupBehaviour.getBehaviours().forEachRemaining(subBehaviour -> collectMemoriesFromTask(memories, subBehaviour));
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
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
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

		for (Pair<Integer, ? extends Behavior<? super E>> pair : activityGroup.pairBehaviourPriorities()) {
			brain.addBehaviour(pair.getFirst(), activity, pair.getSecond());
		}
	}
}
