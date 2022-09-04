package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

	@Override
	protected void start(E entity) {
		this.callback.accept(entity);
	}
}
