package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.FreePositionTracker;

/**
 * Set the look target to a random nearby position
 * @param <E> The entity
 */
public class SetRandomLookTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORIES = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT)});

	private Function<Random, Float> runChance = (rand) -> 0.02f;
	private Function<E, Integer> lookTime = entity -> entity.getRandom().nextInt(20) + 20;

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
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		return entity.getRandom().nextFloat() < this.runChance.apply(entity.getRandom());
	}

	@Override
	protected void start(E entity) {
		double angle = 2 * Math.PI * entity.getRandom().nextDouble();

		BrainUtils.setForgettableMemory(entity, MemoryModuleType.LOOK_TARGET, new FreePositionTracker(entity.getEyePosition(1).add(Math.cos(angle), 0, Math.sin(angle))), this.lookTime.apply(entity));
		doStop((ServerWorld)entity.level, entity, entity.level.getGameTime());
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORIES;
	}
}
