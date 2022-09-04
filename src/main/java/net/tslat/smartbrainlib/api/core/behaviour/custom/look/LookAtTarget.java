package net.tslat.smartbrainlib.api.core.behaviour.custom.look;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

import java.util.List;

/**
 * Look at the look target for as long as it is present
 * @param <E> The entity
 */
public class LookAtTarget<E extends Mob> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT));

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return false;
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return BrainUtils.hasMemory(entity, MemoryModuleType.LOOK_TARGET);
	}

	@Override
	protected void tick(E entity) {
		BrainUtils.withMemory(entity, MemoryModuleType.LOOK_TARGET, target -> entity.getLookControl().setLookAt(target.currentPosition()));
	}
}
