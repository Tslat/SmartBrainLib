package net.tslat.smartbrainlib.api.core.sensor.base;

import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An abstract sensor class used for sensors that utilise some form of predication in their function.
/// This allows for instance-based configuration of the predicate and the sensor.
///
/// @param <T> The predicate target type, used for whatever the sensor might need
/// @param <BO> The brain owner entity
public abstract class PredicateSensor<BO extends LivingEntity, T> extends ExtendedSensor<BO> {
	private BiPredicate<BO, T> predicate;

	public PredicateSensor() {
		this((_, _) -> true);
	}

	public PredicateSensor(BiPredicate<BO, T> predicate) {
		this.predicate = predicate;
	}

	/// Set the predicate for the sensor. The subclass of this class determines its usage
	public PredicateSensor<BO, T> setPredicate(BiPredicate<BO, T> predicate) {
		this.predicate = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the scan rate for this sensor
	public PredicateSensor<BO, T> scanRate(int scanRate) {
		return (PredicateSensor<BO, T>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public PredicateSensor<BO, T> scanRate(ToIntFunction<BO> function) {
		return (PredicateSensor<BO, T>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public PredicateSensor<BO, T> afterScanning(Consumer<BO> callback) {
		return (PredicateSensor<BO, T>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public PredicateSensor<BO, T> onlyScanIf(Predicate<BO> predicate) {
		return (PredicateSensor<BO, T>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Custom Implementation Boilerplate>">
	/// @return The predicate this sensor is based around
	protected BiPredicate<BO, T> predicate() {
		return this.predicate;
	}
	//</editor-fold>
}
