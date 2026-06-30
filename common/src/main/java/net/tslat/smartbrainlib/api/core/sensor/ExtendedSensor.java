package net.tslat.smartbrainlib.api.core.sensor;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// An extension of the base [Sensor]. This adds some minor additional functionality and swaps the memory to a list for easier usage and faster iteration<br/>
/// All custom sensor implementations should use this superclass
///
/// @param <BO> The entity
@SuppressWarnings("UnusedReturnValue")
public abstract class ExtendedSensor<BO extends LivingEntity> extends Sensor<BO> {
	protected ToIntFunction<BO> scanRate = _ -> 20;
	protected Predicate<BO> scanCondition = _ -> true;
	protected Consumer<BO> scanCallback = _ -> {};
	protected long nextTickTime = 0;

	public ExtendedSensor() {
		super();
	}

	/// Set the scan rate for this sensor
	@ApiStatus.NonExtendable
	public ExtendedSensor<BO> scanRate(int scanRate) {
		return scanRate(_ -> scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@ApiStatus.NonExtendable
	public ExtendedSensor<BO> scanRate(ToIntFunction<BO> function) {
		this.scanRate = function;

		return this;
	}

	/// Set a callback function for when the sensor completes a scan
	@ApiStatus.NonExtendable
	public ExtendedSensor<BO> afterScanning(Consumer<BO> callback) {
		this.scanCallback = callback;

		return this;
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@ApiStatus.NonExtendable
	public ExtendedSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		this.scanCondition = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Custom Implementation Boilerplate>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	public abstract SensorType<? extends ExtendedSensor<?>> type();

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	public abstract List<MemoryModuleType<?>> memoriesUsed();

	/// Vanilla's implementation of the required memory collection. Functionally replaced by [ExtendedSensor#memoriesUsed()]<br/>
	/// Left in place for compatibility reasons
	///
	/// @return A set view of the list returned by `memoriesUsed()`
	@Deprecated
	@Override
	public final Set<MemoryModuleType<?>> requires() {
		return new ObjectOpenHashSet<>(memoriesUsed());
	}

	/// Handle the Sensor's actual function here
	///
	/// This is called once every [#scanRate] ticks
	///
	/// Be wary of the performance implications of computation-heavy checks here
	@Override
	protected void doTick(ServerLevel level, BO entity) {}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	@Override
	public final void tick(ServerLevel level, BO entity) {
		if (this.nextTickTime < level.getGameTime()) {
			this.nextTickTime = level.getGameTime() + this.scanRate.applyAsInt(entity);

			if (this.scanCondition.test(entity)) {
				doTick(level, entity);
				this.scanCallback.accept(entity);
			}
		}
	}
	//</editor-fold>
}
