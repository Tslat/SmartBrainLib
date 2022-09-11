package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;

/**
 * Set the {@link net.minecraft.world.entity.ai.memory.MemoryModuleType#LOOK_TARGET} of the brain owner from {@link net.minecraft.world.entity.ai.memory.MemoryModuleType#NEAREST_PLAYERS}
 * @param <E> The entity
 */
public class SetPlayerLookTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleStatus.VALUE_PRESENT)});

	protected Predicate<PlayerEntity> predicate = pl -> true;

	protected PlayerEntity target = null;

	/**
	 * Set the predicate for the player to look at.
	 * @param predicate The predicate
	 * @return this
	 */
	public SetPlayerLookTarget<E> predicate(Predicate<PlayerEntity> predicate) {
		this.predicate = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		for (PlayerEntity player : BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_PLAYERS)) {
			if (this.predicate.test(player)) {
				this.target = player;

				break;
			}
		}

		return this.target != null;
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(this.target, true));
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}
}
