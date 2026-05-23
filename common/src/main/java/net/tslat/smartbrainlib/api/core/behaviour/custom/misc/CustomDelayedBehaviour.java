package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;

import java.util.List;
import java.util.function.Predicate;

/// A behaviour module that acts as a default implementation of [DelayedBehaviour].
/// Useful for handling custom minor actions that are either too specific to warrant a new behaviour, or not worth implementing into a full behaviour.
/// Set the condition for running via [ExtendedBehaviour#startCondition(Predicate)]
public final class CustomDelayedBehaviour<E extends LivingEntity> extends DelayedBehaviour<E> {
	public CustomDelayedBehaviour(int delayTicks) {
		super(delayTicks);
	}

	@Override
    public List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}
}
