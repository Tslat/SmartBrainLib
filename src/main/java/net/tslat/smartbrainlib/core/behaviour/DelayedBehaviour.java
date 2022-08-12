package net.tslat.smartbrainlib.core.behaviour;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * An abstract behaviour used for tasks that should have a start, and then a followup delayed action. <br/>
 * This is most useful for things like attacks that have associated animations, or action which require a charge up or prep time. <br/>
 *
 * @param <E> The entity
 */
public abstract class DelayedBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private final int delayTime;
	protected long delayFinishedAt = 0;

	public DelayedBehaviour(int ticks) {
		this.delayTime = ticks;
	}

	@Override
	protected final void start(ServerLevel level, E entity, long gameTime) {
		super.start(level, entity, gameTime);

		this.delayFinishedAt = gameTime + this.delayTime;
	}

	@Override
	protected final void stop(ServerLevel level, E entity, long gameTime) {
		super.stop(level, entity, gameTime);

		this.delayFinishedAt = 0;
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return this.delayFinishedAt >= gameTime;
	}

	@Override
	protected final void tick(ServerLevel level, E entity, long gameTime) {
		super.tick(level, entity, gameTime);

		if (this.delayFinishedAt <= gameTime)
			doDelayedAction(entity);
	}

	/**
	 * The action to take once the delay period has elapsed.
	 *
	 * @param entity The owner of the brain
	 */
	protected void doDelayedAction(E entity) {}
}
