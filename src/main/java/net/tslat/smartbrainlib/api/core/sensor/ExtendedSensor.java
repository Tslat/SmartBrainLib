package net.tslat.smartbrainlib.api.core.sensor;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An extension of the base Sensor. This adds some minor additional functionality and swaps the memory to a list for easier usage and faster iteration. <br>
 * All custom sensor implementations should use this superclass.
 *
 * @param <E> The entity
 */
public abstract class ExtendedSensor<E extends LivingEntity> extends Sensor<E> {
	protected Function<E, Integer> scanRate = entity -> 20;
	protected Consumer<E> scanCallback = entity -> {};
	private long nextTickTime = 0;

	public ExtendedSensor() {
		super();
	}

	/**
	 * Set the scan rate provider for this sensor. <br>
	 * The provider will be sampled every time the sensor does a scan.
	 *
	 * @param function The function to provide the tick rate
	 * @return this
	 */
	public ExtendedSensor<E> setScanRate(Function<E, Integer> function) {
		this.scanRate = function;

		return this;
	}

	/**
	 * Set a callback function for when the sensor completes a scan. <br>
	 * @param callback
	 * @return
	 */
	public ExtendedSensor<E> afterScanning(Consumer<E> callback) {
		this.scanCallback = callback;

		return this;
	}

	@Override
	public final void tick(ServerLevel level, E entity) {
		if (nextTickTime < level.getGameTime()) {
			nextTickTime = level.getGameTime() + scanRate.apply(entity);

			doTick(level, entity);
			this.scanCallback.accept(entity);
		}
	}

	/**
	 * Handle the Sensor's actual function here. Be wary of performance implications of computation-heavy checks here.
	 *
	 * @param level The level the entity is in
	 * @param entity The owner of the brain
	 */
	@Override
	protected void doTick(ServerLevel level, E entity) {}

	/**
	 * The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain <br>
	 * Bonus points if it's a statically-initialised list.
	 *
	 * @return The list of memory types saves by this sensor
	 */
	public abstract List<MemoryModuleType<?>> memoriesUsed();

	/**
	 * The {@link SensorType} of the sensor, used for reverse lookups.
	 * @return The sensor type
	 */
	public abstract SensorType<? extends ExtendedSensor<?>> type();

	/**
	 * Vanilla's implementation of the required memory collection. Functionally replaced by {@link ExtendedSensor#memoriesUsed()}. <br>
	 * Left in place for compatibility reasons.
	 *
	 * @return A set view of the list returned by {@code memoriesUsed()}
	 */
	@Override
	public final Set<MemoryModuleType<?>> requires() {
		return new ObjectOpenHashSet<>(memoriesUsed());
	}
}
