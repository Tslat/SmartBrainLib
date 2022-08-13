package net.tslat.smartbrainlib.core.behaviour;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.APIOnly;

import java.util.List;

/**
 * An extension of the base Behavior class that is used for tasks in the brain system. <br>
 * This extension auto-handles some boilerplate and adds in some additional auto-handled functions: <br>
 * <ul>
 *     <li>Task start and stop callbacks for additional entity-interactions</li>
 *     <li>A functional implementation of a duration provider</li>
 *     <li>A functional implementation of a cooldown provider</li>
 * </ul>
 * Ideally, all custom behaviours should use at least this class as a base, instead of the core Behavior class
 *
 * @param <E> Your entity
 */
public abstract class ExtendedBehaviour<E extends LivingEntity> extends Behavior<E> {
	private Runnable taskStartCallback = () -> {};
	private Runnable taskStopCallback = () -> {};

	private IntProvider runtimeProvider = ConstantInt.of(60);
	private IntProvider cooldownProvider = ConstantInt.of(0);
	protected long cooldownFinishedAt = 0;

	public ExtendedBehaviour() {
		super(new Object2ObjectOpenHashMap<>());

		for (Pair<MemoryModuleType<?>, MemoryStatus> memoryReq : getMemoryRequirements()) {
			this.entryCondition.put(memoryReq.getFirst(), memoryReq.getSecond());
		}
	}

	/**
	 * A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task.
	 *
	 * @param callback The callback
	 * @return this
	 */
	public final ExtendedBehaviour<E> whenStarting(Runnable callback) {
		this.taskStartCallback = callback;

		return this;
	}

	/**
	 * A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task. <br>
	 * Note that the task stopping does not necessarily mean it was successful.
	 *
	 * @param callback The callback
	 * @return this
	 */
	public final ExtendedBehaviour<E> whenStopping(Runnable callback) {
		this.taskStopCallback = callback;

		return this;
	}

	/**
	 * Set the length that the task should run for, once activated. The value used is in <i>ticks</i>.
	 * @see net.minecraft.util.valueproviders.UniformInt
	 * @see ConstantInt
	 *
	 * @param timeProvider An {@link IntProvider} implementation.
	 * @return this
	 */
	public final ExtendedBehaviour<E> runFor(IntProvider timeProvider) {
		this.runtimeProvider = timeProvider;

		return this;
	}

	/**
	 * Set the length that the task should wait for between activations. This is the time between when the task stops, and it is able to start again. The value used is in <i>ticks</i>.
	 * @see net.minecraft.util.valueproviders.UniformInt
	 * @see ConstantInt
	 *
	 * @param timeProvider An {@link IntProvider} implementation.
	 * @return this
	 */
	public final ExtendedBehaviour<E> cooldownFor(IntProvider timeProvider) {
		this.cooldownProvider = timeProvider;

		return this;
	}

	@Override
	public final boolean tryStart(ServerLevel level, E owner, long gameTime) {
		if (cooldownFinishedAt > gameTime || !hasRequiredMemories(owner) || !checkExtraStartConditions(level, owner))
			return false;

		this.status = Status.RUNNING;
		this.endTimestamp = gameTime + this.runtimeProvider.sample(owner.getRandom());

		start(level, owner, gameTime);

		return true;
	}

	/**
	 * Check any extra conditions required for this behaviour to start. <br>
	 * By this stage, memory conditions from {@link ExtendedBehaviour#getMemoryRequirements()} have already been checked.
	 *
	 * @param level The level the entity is in
	 * @param entity The owner of the brain
	 * @return Whether the conditions have been met to start the behaviour
	 */
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return true;
	}

	/**
	 * The root stop method for when this behaviour stops. This method should only be overridden by other abstract subclasses. <br>
	 * If overriding, ensure you either call {@code super} or manually call {@code stop(E)} yourself.
	 *
	 * @param level The level the entity is in
	 * @param entity The entity the brain belongs to
	 * @param gameTime The current gameTime (in ticks) of the level
	 */
	@Override
	@APIOnly
	protected void start(ServerLevel level, E entity, long gameTime) {
		this.taskStartCallback.run();
		start(entity);
	}

	/**
	 * Override this for custom behaviour implementations. This is a safe endpoint for behaviours so that all required auto-handling is safely contained without super calls.<br>
	 * This is called when the behaviour is to start. Set up any instance variables needed or perform the required actions.<br>
	 * By this stage any memory requirements set in {@link ExtendedBehaviour#getMemoryRequirements()} are true, so any memories paired with {@link MemoryStatus#VALUE_PRESENT} are safe to retrieve.
	 *
	 * @param entity The entity being handled (I.E. the owner of the brain)
	 */
	protected void start(E entity) {}

	/**
	 * The root stop method for when this behaviour stops. This method should only be overridden by other abstract subclasses. <br>
	 * If overriding, ensure you either call {@code super} or manually call {@code stop(E)} yourself.
	 *
	 * @param level The level the entity is in
	 * @param entity The entity the brain belongs to
	 * @param gameTime The current gameTime (in ticks) of the level
	 */
	@Override
	@APIOnly
	protected void stop(ServerLevel level, E entity, long gameTime) {
		this.cooldownFinishedAt = gameTime + cooldownProvider.sample(entity.getRandom());

		this.taskStopCallback.run();
		stop(entity);
	}

	/**
	 * Override this for custom behaviour implementations. This is a safe endpoint for behaviours so that all required auto-handling is safely contained without super calls.<br>
	 * This is called when the behaviour is to stop. Close off any instanced variables and such here, ready for the next start.
	 *
	 * @param entity The entity being handled (I.E. the owner of the brain)
	 */
	protected void stop(E entity) {}

	/**
	 * Check whether the behaviour can continue running. This is checked before {@link ExtendedBehaviour#tick(E)}. <br>
	 * Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here.
	 *
	 * @param level The level the entity is in
	 * @param entity The owner of the brain
	 * @param gameTime The current time (in ticks) of the level
	 * @return Whether the behaviour should continue ticking
	 */
	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return false;
	}

	/**
	 * The root tick method for when this behaviour ticks. This method should only be overridden by other abstract subclasses. <br>
	 * If overriding, ensure you either call {@code super} or manually call {@code tick(E)} yourself.
	 *
	 * @param level The level the entity is in
	 * @param entity The entity the brain belongs to
	 * @param gameTime The current gameTime (in ticks) of the level
	 */
	@Override
	@APIOnly
	protected void tick(ServerLevel level, E entity, long gameTime) {
		tick(entity);
	}

	/**
	 * Override this for custom behaviour implementations. This is a safe endpoint for behaviours so that all required auto-handling is safely contained without super calls.<br>
	 * This is called when the behaviour is ticked. Be aware this is called <i>every tick</i>, so use tick reduction if needed to minimise performance impacts of goals. <br>
	 * NOTE: Memory requirements are <i>not</i> guaranteed at this stage. If you are retrieving brain memories, you'll need to check their presence before use.
	 *
	 * @param entity The entity being handled (I.E. the owner of the brain)
	 */
	protected void tick(E entity) {}

	@Override
	public final boolean hasRequiredMemories(E entity) {
		Brain<?> brain = entity.getBrain();

		for (Pair<MemoryModuleType<?>, MemoryStatus> memoryPair : getMemoryRequirements()) {
			if (!brain.checkMemory(memoryPair.getFirst(), memoryPair.getSecond()))
				return false;
		}

		return true;
	}

	/**
	 * The list of memory requirements this task has prior to starting. This outlines the approximate state the brain should be in, in order to allow this behaviour to run. <br>
	 * Bonus points if it's a statically-initialised list.
	 *
	 * @return The {@link List} of {@link MemoryModuleType Memories} and their associated required {@link MemoryStatus status}
	 */
	protected abstract List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements();
}
