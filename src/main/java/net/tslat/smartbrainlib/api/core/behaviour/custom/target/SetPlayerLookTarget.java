package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;
import java.util.function.Predicate;

/**
 * Set the {@link net.minecraft.world.entity.ai.memory.MemoryModuleType#LOOK_TARGET} of the brain owner from {@link net.minecraft.world.entity.ai.memory.MemoryModuleType#NEAREST_PLAYERS}
 * @param <E> The entity
 */
public class SetPlayerLookTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT));

	private Predicate<Player> predicate = pl -> true;

	private Player target = null;

	/**
	 * Set the predicate for the player to look at.
	 * @param predicate The predicate
	 * @return this
	 */
	public SetPlayerLookTarget<E> predicate(Predicate<Player> predicate) {
		this.predicate = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		for (Player player : BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_PLAYERS)) {
			if (this.predicate.test(player)) {
				this.target = player;

				break;
			}
		}

		return this.target != null;
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(this.target, true));
		doStop((ServerLevel)entity.level, entity, entity.level.getGameTime());
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}
}
