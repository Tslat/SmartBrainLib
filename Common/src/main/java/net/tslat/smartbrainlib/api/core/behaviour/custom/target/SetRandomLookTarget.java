package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.FreePositionTracker;
import net.tslat.smartbrainlib.object.backport.Collections;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Set the look target to a random nearby position
 * @param <E> The entity
 */
public class SetRandomLookTarget<E extends Mob> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = Collections.list(Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT));

	protected Function<Random, Float> runChance = rand -> 0.02f;
	protected Function<E, Integer> lookTime = entity -> entity.getRandom().nextInt(20) + 20;

	/**
	 * Set the value provider for the chance of the look target being set.
	 * @param chance The float provider
	 * @return this
	 */
	public SetRandomLookTarget<E> lookChance(Function<Random, Float> chance) {
		this.runChance = chance;

		return this;
	}

	/**
	 * Set the value provider for how long the entity's look target should be set for
	 * @param function The tick providing function
	 * @return this
	 */
	public SetRandomLookTarget<E> lookTime(Function<E, Integer> function) {
		this.lookTime = function;

		return this;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return entity.getRandom().nextFloat() < this.runChance.apply(entity.getRandom());
	}

	@Override
	protected void start(E entity) {
		double angle = Math.PI * 2d * entity.getRandom().nextDouble();

		BrainUtils.setForgettableMemory(entity, MemoryModuleType.LOOK_TARGET, new FreePositionTracker(entity.getEyePosition(1).add(Math.cos(angle), 0, Math.sin(angle))), this.lookTime.apply(entity));
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORIES;
	}
}
