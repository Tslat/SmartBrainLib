package net.tslat.smartbrainlib.api.core.sensor;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiPredicate;

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
}
