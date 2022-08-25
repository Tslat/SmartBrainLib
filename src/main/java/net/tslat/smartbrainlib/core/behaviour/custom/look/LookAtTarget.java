package net.tslat.smartbrainlib.core.behaviour.custom.look;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour;

/**
 * Look at the look target for as long as it is present
 * @param <E> The entity
 */
public class LookAtTarget<E extends MobEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_PRESENT));

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return false;
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		return BrainUtils.hasMemory(entity, MemoryModuleType.LOOK_TARGET);
	}

	@Override
	protected void tick(E entity) {
		BrainUtils.withMemory(entity, MemoryModuleType.LOOK_TARGET, target -> entity.getLookControl().setLookAt(target.currentPosition()));
	}
}
