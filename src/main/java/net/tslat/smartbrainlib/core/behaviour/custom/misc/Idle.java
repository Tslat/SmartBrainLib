package net.tslat.smartbrainlib.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour;

import java.util.List;

/**
 * Do nothing at all.
 * @param <E> The entity
 */
public class Idle<E extends LivingEntity> extends ExtendedBehaviour<E> {
	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}
}
