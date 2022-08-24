package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

/**
 * A behaviour module that invokes a callback. <br>
 * Useful for handling custom minor actions that are either too specific to warrant a new behaviour, or not worth implementing into a full behaviour. <br>
 * Set the condition for running via {@link ExtendedBehaviour#startCondition(Predicate)}
 */
public final class CustomBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private Consumer<E> callback;

	public CustomBehaviour(Consumer<E> callback) {
		this.callback = callback;
	}

	/**
	 * Replace the callback function
	 * @return this
	 */
	public CustomBehaviour<E> callback(Consumer<E> callback) {
		this.callback = callback;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return new ArrayList<>();
	}

	@Override
	protected void start(E entity) {
		this.callback.accept(entity);
		doStop((ServerWorld)entity.level, entity, entity.level.getGameTime());
	}
}
