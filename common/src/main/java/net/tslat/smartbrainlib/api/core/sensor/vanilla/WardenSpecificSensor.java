package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.*;

/// A replication of vanilla's [net.minecraft.world.entity.ai.sensing.WardenEntitySensor]<br/>
/// Not really useful, but included for completeness' sake and legibility
///
/// Handle's the Warden's nearest attackable target, prioritising players
///
/// @param <BO> The brain owner entity
public class WardenSpecificSensor<BO extends Warden> extends NearbyLivingEntitySensor<BO> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_ATTACKABLE);

	public WardenSpecificSensor() {
		setRadius(24);
		setPredicate(Warden::canTargetEntity);
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the radius for the sensor to scan
	///
	/// @param radius The coordinate radius, in blocks
	public WardenSpecificSensor<BO> setRadius(double radius) {
		return (WardenSpecificSensor<BO>)super.setRadius(radius);
	}

	/// Set the radius for the sensor to scan
	///
	/// @param xz The X/Z coordinate radius, in blocks
	/// @param y  The Y coordinate radius, in blocks
	public WardenSpecificSensor<BO> setRadius(double xz, double y) {
		return (WardenSpecificSensor<BO>)super.setRadius(xz, y);
	}

	/// Set the radius for the sensor to scan
	///
	/// @param radiusFunction The function to determine the radius for the current scan tick
	public WardenSpecificSensor<BO> setRadius(Function<BO, SquareRadius> radiusFunction) {
		return (WardenSpecificSensor<BO>)super.setRadius(radiusFunction);
	}

	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public WardenSpecificSensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (WardenSpecificSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public WardenSpecificSensor<BO> scanRate(int scanRate) {
		return (WardenSpecificSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public WardenSpecificSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (WardenSpecificSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public WardenSpecificSensor<BO> afterScanning(Consumer<BO> callback) {
		return (WardenSpecificSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public WardenSpecificSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (WardenSpecificSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.WARDEN_SPECIFIC.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		final List<MemoryModuleType<?>> memories = new ObjectArrayList<>(super.memoriesUsed());

		memories.addAll(MEMORIES);

		return memories;
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		super.doTick(level, entity);

		LivingEntity target = null;

		for (LivingEntity nearby : BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, List.of())) {
			if (!predicate().test(entity, nearby))
				continue;

			if (nearby instanceof Player) {
				target = nearby;

				break;
			}

			if (target == null)
				target = nearby;
		}

		BrainUtil.setOrClearMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, target);
	}
	//</editor-fold>
}
