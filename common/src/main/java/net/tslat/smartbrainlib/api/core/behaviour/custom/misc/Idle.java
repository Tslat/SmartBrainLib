package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;

import java.util.List;

/// Do nothing at all.
/// @param <E> The entity
public class Idle<E extends LivingEntity> extends ExtendedBehaviour<E> {
	@Override
    public List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}
	
	@Override
	protected boolean shouldKeepRunning(E entity) {
		return true;
	}
}
