package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.FreePositionTracker;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Set the look target to a random nearby position
 * @param <E> The entity
 */
public class SetRandomLookTarget<E extends Mob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).noMemory(MemoryModuleType.LOOK_TARGET);

	protected FloatProvider runChance = ConstantFloat.of(0.02f);
	protected ToIntFunction<E> lookTime = entity -> entity.getRandom().nextInt(20) + 20;

	/**
	 * Set the value provider for the chance of the look target being set.
	 * @param chance The float provider
	 * @return this
	 */
	public SetRandomLookTarget<E> lookChance(FloatProvider chance) {
		this.runChance = chance;

		return this;
	}

	/**
	 * Set the value provider for how long the entity's look target should be set for
	 * @param function The tick providing function
	 * @return this
	 */
	public SetRandomLookTarget<E> lookTime(ToIntFunction<E> function) {
		this.lookTime = function;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return entity.getRandom().nextFloat() < this.runChance.sample(entity.getRandom());
	}

	@Override
	protected void start(E entity) {
		double angle = Mth.TWO_PI * entity.getRandom().nextDouble();

		BrainUtil.setForgettableMemory(entity, MemoryModuleType.LOOK_TARGET, new FreePositionTracker(entity.getEyePosition().add(Math.cos(angle), 0, Math.sin(angle))), this.lookTime.applyAsInt(entity));
	}
}
