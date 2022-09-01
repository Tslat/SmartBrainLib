package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

/**
 * Do nothing at all.
 * @param <E> The entity
 */
public class Idle<E extends LivingEntity> extends ExtendedBehaviour<E> {
	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return new ArrayList<>();
	}
}
