package net.tslat.smartbrainlib.core.behaviour;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Consumer;

/**
 * An abstract behaviour used for tasks that should have a start, and then a followup delayed action. <br>
 * This is most useful for things like attacks that have associated animations, or action which require a charge up or prep time. <br>
 *
 * @param <E> The entity
 */
public abstract class DelayedBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private final int delayTime;
	protected long delayFinishedAt = 0;
	protected Consumer<E> delayedCallback = entity -> {};

	public DelayedBehaviour(int delayTicks) {
		this.delayTime = delayTicks;
	}

	/**
	 * A callback for when the delayed action is called.
	 * @param callback The callback
	 * @return this
	 */
	public final DelayedBehaviour<E> whenActivating(Consumer<E> callback) {
		this.delayedCallback = callback;

		return this;
	}

	@Override
	protected final void start(ServerWorld level, E entity, long gameTime) {
		super.start(level, entity, gameTime);

		if (this.delayTime > 0) {
			this.delayFinishedAt = gameTime + this.delayTime;
		}
		else {
			doDelayedAction(entity);
		}
	}

	@Override
	protected final void stop(ServerWorld level, E entity, long gameTime) {
		super.stop(level, entity, gameTime);

		this.delayFinishedAt = 0;
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		return this.delayFinishedAt >= gameTime;
	}

	@Override
	protected final void tick(ServerWorld level, E entity, long gameTime) {
		super.tick(level, entity, gameTime);

		if (this.delayFinishedAt <= gameTime) {
			doDelayedAction(entity);
			this.delayedCallback.accept(entity);
		}
	}

	/**
	 * The action to take once the delay period has elapsed.
	 *
	 * @param entity The owner of the brain
	 */
	protected void doDelayedAction(E entity) {}
}
