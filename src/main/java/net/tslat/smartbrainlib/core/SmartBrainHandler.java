package net.tslat.smartbrainlib.core;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.api.BrainUtils;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmartBrainHandler<T extends LivingEntity & SmartBrainOwner<T>> {
	private static final Map<EntityType<? extends LivingEntity>, ImmutableList<MemoryModuleType<?>>> BRAIN_MEMORY_CACHE = new Object2ObjectOpenHashMap<>();

	private final T owner;
	private Brain<T> brain = null;

	private final boolean saveMemories;
	private final boolean hasDynamicTasks;

	/**
	 * @param owner The owner of the brain
	 */
	public SmartBrainHandler(T owner) {
		this(owner, false);
	}
	/**
	 * @param owner The owner of the brain
	 * @param hasDynamicTasks Whether the entity type this brain is for uses different behaviours depending on the entity instance
	 */
	public SmartBrainHandler(T owner, boolean hasDynamicTasks) {
		this(owner, false, hasDynamicTasks);
	}

	/**
	 * @param owner The owner of the brain
	 * @param saveMemories Whether memory states should be saved & loaded when the entity is saved or loaded.
	 * @param hasDynamicTasks Whether the entity type this brain is for uses different behaviours depending on the entity instance
	 */
	public SmartBrainHandler(T owner, boolean saveMemories, boolean hasDynamicTasks) {
		this.owner = owner;
		this.saveMemories = saveMemories;
		this.hasDynamicTasks = hasDynamicTasks;
	}

	public Brain.Provider<T> getProvider() {
		if (!this.hasDynamicTasks && BRAIN_MEMORY_CACHE.containsKey(this.owner.getType()))
			return Brain.provider(BRAIN_MEMORY_CACHE.get(this.owner.getType()), this.owner.getSensors());

		if (this.brain != null)
			return Brain.provider(ImmutableList.copyOf(this.brain.memories.keySet()), this.owner.getSensors());

		return Brain.provider(getMemoryList(compileTasks()), this.owner.getSensors());
	}

	public Brain<T> getBrain() {
		return this.brain;
	}

	public Brain<T> makeBrain(Dynamic<?> codecLoader) {
		if (this.brain != null && !this.saveMemories)
			return this.brain;

		List<SensorType<? extends Sensor<? super T>>> sensors = owner.getSensors();
		Map<Activity, BrainActivityGroup<T>> taskList = compileTasks();
		ImmutableList<MemoryModuleType<?>> memories;

		if (!hasDynamicTasks && BRAIN_MEMORY_CACHE.containsKey(owner.getType())) {
			memories = BRAIN_MEMORY_CACHE.get(owner.getType());
		}
		else {
			memories = getMemoryList(taskList);

			if (!this.hasDynamicTasks)
				BRAIN_MEMORY_CACHE.put((EntityType<? extends LivingEntity>)this.owner.getType(), memories);
		}

		if (!saveMemories) {
			this.brain = new Brain<>(memories, sensors, ImmutableList.of(), this::emptyBrainCodec);
		}
		else {
			this.brain = Brain.provider(memories, sensors).makeBrain(codecLoader);
		}

		sanityCheckBrainState();
		applyTasks(taskList);

		return this.brain;
	}

	private Codec<Brain<T>> emptyBrainCodec() {
		MutableObject<Codec<Brain<T>>> brainCodec = new MutableObject<>();

		brainCodec.setValue(Codec.unit(() -> new Brain<>(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), brainCodec::getValue)));

		return brainCodec.getValue();
	}

	protected ImmutableList<MemoryModuleType<?>> getMemoryList(Map<Activity, BrainActivityGroup<T>> taskList) {
		if (!hasDynamicTasks && BRAIN_MEMORY_CACHE.containsKey(owner.getType()))
			return BRAIN_MEMORY_CACHE.get(owner.getType());

		Set<MemoryModuleType<?>> memoryTypes = new ObjectOpenHashSet<>();

		taskList.forEach((key, value) -> value.getBehaviours().forEach(task -> memoryTypes.addAll(task.entryCondition.keySet())));

		return ImmutableList.copyOf(memoryTypes);
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

	private void applyTasks(Map<Activity, BrainActivityGroup<T>> taskList) {
		for (Map.Entry<Activity, BrainActivityGroup<T>> tasksEntry : taskList.entrySet()) {
			addActivity(tasksEntry.getKey(), tasksEntry.getValue());
		}

		this.brain.setCoreActivities(this.owner.getAlwaysRunningActivities());
		this.brain.setDefaultActivity(this.owner.getDefaultActivity());
		this.brain.useDefaultActivity();
		this.owner.handleAdditionalBrainSetup(this.brain);
	}

	private void sanityCheckBrainState() {
		if (!FMLLoader.isProduction()) {
			for (Sensor<? super T> sensor : this.brain.sensors.values()) {
				for (MemoryModuleType<?> memoryType : sensor.requires()) {
					if (!this.brain.memories.containsKey(memoryType))
						throw new IllegalStateException("Required memory module not present in entity brain. " + ForgeRegistries.ENTITY_TYPES.getKey(this.owner.getType()).toString());
				}
			}
		}
	}

	/**
	 * Call this from your entity's {@link LivingEntity#serverAiStep}
	 */
	public void tick() {
		this.owner.level.getProfiler().push("SmartBrain");
		this.brain.tick((ServerLevel)this.owner.level, this.owner);
		this.brain.setActiveActivityToFirstValid(this.owner.getActivityPriorities());
		this.owner.level.getProfiler().pop();

		if (this.owner instanceof Mob mob)
			mob.setAggressive(BrainUtils.hasMemory(mob, MemoryModuleType.ATTACK_TARGET));
	}

	private void addActivity(Activity activity, BrainActivityGroup<T> activityGroup) {
		this.brain.activityRequirements.put(activity, activityGroup.getActivityStartMemoryConditions());

		if (activityGroup.getWipedMemoriesOnFinish() != null)
			this.brain.activityMemoriesToEraseWhenStopped.put(activity, activityGroup.getWipedMemoriesOnFinish());

		for (Pair<Integer, ? extends Behavior<? super T>> pair : activityGroup.pairBehaviourPriorities()) {
			this.brain.availableBehaviorsByPriority.computeIfAbsent(pair.getFirst(), priority -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(activity, activityKey -> new ObjectOpenHashSet<>()).add(pair.getSecond());
		}
	}
}
