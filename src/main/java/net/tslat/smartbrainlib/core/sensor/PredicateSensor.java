package net.tslat.smartbrainlib.core.sensor;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * An abstract sensor class used for sensors that utilise some form of predication in their function.
 * This allows for instance-based configuration of the predicate and the sensor.
 *
 * @param <P> The predicate, used for whatever the sensor might need
 * @param <E> The entity
 */
public abstract class PredicateSensor<P, E extends LivingEntity> extends ExtendedSensor<E> {
	private BiPredicate<P, E> predicate;

	public PredicateSensor() {
		this((obj, entity) -> true);
	}

	public PredicateSensor(BiPredicate<P, E> predicate) {
		this.predicate = predicate;
	}

	/**
	 * Set the predicate for the sensor. The subclass of this class determines its usage.
	 *
	 * @param predicate The predicate
	 * @return this
	 */
	public PredicateSensor<P, E> setPredicate(BiPredicate<P, E> predicate) {
		this.predicate = predicate;

		return this;
	}

	/**
	 * Retrieve the predicate this sensor is using.
	 *
	 * @return The predicate
	 */
	protected BiPredicate<P, E> predicate() {
		return this.predicate;
	}

	/**
	 * Set the scan rate provider for this sensor. <br>
	 * The provider will be sampled every time the sensor does a scan.
	 *
	 * @param function The function to provide the tick rate
	 * @return this
	 */
	@Override
	public PredicateSensor<P, E> setScanRate(Function<E, Integer> function) {
		return (PredicateSensor<P, E>)super.setScanRate(function);
	}
}
