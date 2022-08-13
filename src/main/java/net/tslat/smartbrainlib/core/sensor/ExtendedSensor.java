package net.tslat.smartbrainlib.core.sensor;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.List;
import java.util.Set;

/**
 * An extension of the base Sensor. This adds some minor additional functionality and swaps the memory to a list for easier usage and faster iteration. <br>
 * All custom sensor implementations should use this superclass.
 *
 * @param <E> The entity
 */
public abstract class ExtendedSensor<E extends LivingEntity> extends Sensor<E> {
	protected IntProvider scanRate = ConstantInt.of(20);
	private long nextTickTime = 0;

	public ExtendedSensor() {
		super();
	}

	/**
	 * Set the scan rate provider for this sensor. <br>
	 * The provider will be sampled every time the sensor does a scan.
	 *
	 * @param intProvider The scan rate provider
	 * @return this
	 */
	public ExtendedSensor<E> setScanRate(IntProvider intProvider) {
		this.scanRate = intProvider;

		return this;
	}

	@Override
	public final void tick(ServerLevel level, E entity) {
		if (nextTickTime < level.getGameTime()) {
			nextTickTime = level.getGameTime() + scanRate.sample(RANDOM);

			doTick(level, entity);
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
