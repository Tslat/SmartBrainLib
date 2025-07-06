package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Functional replacement to {@link net.minecraft.world.entity.ai.behavior.GateBehavior} due to the very poor way it is implemented. <br>
 * In particular, this allows nesting of group behaviours without breaking behaviour flow entirely. <br>
 * It also allows for utilising the various callbacks and conditions that {@link ExtendedBehaviour} offers. <br>
 * NOTE: Only supports ExtendedBehaviour implementations as sub-behaviours. This is due to access-modifiers on the vanilla behaviours making this prohibitively annoying to work with.
 */
public abstract class GroupBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {
	protected final SBLShufflingList<ExtendedBehaviour<? super E>> behaviours;

	@Nullable
	protected ExtendedBehaviour<? super E> runningBehaviour = null;

	@SafeVarargs
	public GroupBehaviour(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
		this.behaviours = new SBLShufflingList<>(behaviours);

		noTimeout();
	}

	@SafeVarargs
	public GroupBehaviour(ExtendedBehaviour<? super E>... behaviours) {
		this.behaviours = new SBLShufflingList<>();

		for (ExtendedBehaviour<? super E> behaviour : behaviours) {
			this.behaviours.add(behaviour, 1);
		}

		noTimeout();
	}

	public GroupBehaviour(Collection<Pair<ExtendedBehaviour<? super E>, Integer>> behaviours) {
		this.behaviours = new SBLShufflingList<>(behaviours);

		noTimeout();
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

	public Iterator<ExtendedBehaviour<? super E>> getBehaviours() {
		return this.behaviours.iterator();
	}

	@Nullable
	protected abstract ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> behaviours);

	@Override
	protected boolean doStartCheck(ServerLevel level, E entity, long gameTime) {
		if (!super.doStartCheck(level, entity, gameTime))
			return false;

		return (this.runningBehaviour = pickBehaviour(level, entity, gameTime, this.behaviours)) != null;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return this.runningBehaviour != null && this.runningBehaviour.canStillUse((ServerLevel)entity.level(), entity, entity.level().getGameTime());
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return this.runningBehaviour == null || this.runningBehaviour.timedOut(gameTime);
	}

	@Override
	protected void tick(ServerLevel level, E owner, long gameTime) {
		this.runningBehaviour.tickOrStop(level, owner, gameTime);

		if (this.runningBehaviour.getStatus() == Status.STOPPED) {
			this.runningBehaviour = null;

			doStop(level, owner, gameTime);
		}
	}

	@Override
	protected void stop(ServerLevel level, E entity, long gameTime) {
		super.stop(level, entity, gameTime);

		if (this.runningBehaviour != null)
			this.runningBehaviour.stop(level, entity, gameTime);

		this.runningBehaviour = null;
	}

	@Override
	public Status getStatus() {
		if (this.runningBehaviour == null)
			return Status.STOPPED;

		return this.runningBehaviour.getStatus();
	}

	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + "): " + (this.runningBehaviour != null ? this.runningBehaviour.getClass().getSimpleName() : "{}");
	}
}
