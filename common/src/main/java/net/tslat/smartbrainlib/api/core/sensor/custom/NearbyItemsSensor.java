package net.tslat.smartbrainlib.api.core.sensor.custom;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/// A sensor that looks for nearby [items][ItemEntity] in the surrounding area
///
/// @param <BO> The brain owner entity
public class NearbyItemsSensor<BO extends Mob> extends PredicateSensor<BO, ItemEntity> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(SBLMemoryTypes.NEARBY_ITEMS.get());

	protected Function<BO, SquareRadius> radius = _ -> new SquareRadius(32, 16);

	public NearbyItemsSensor() {
		super((entity, item) -> entity.wantsToPickUp((ServerLevel)entity.level(), item.getItem()) && entity.hasLineOfSight(item));
	}

	/// Set the radius for the sensor to scan
	///
	/// @param radius The coordinate radius, in blocks
	@ApiStatus.NonExtendable
	public NearbyItemsSensor<BO> detectionRadius(double radius) {
		return detectionRadius(radius, radius);
	}

	/// Set the radius for the sensor to scan
	///
	/// @param xz The X/Z coordinate radius, in blocks
	/// @param y The Y coordinate radius, in blocks
	@ApiStatus.NonExtendable
	public NearbyItemsSensor<BO> detectionRadius(double xz, double y) {
		return detectionRadius(_ -> new SquareRadius(xz, y));
	}

	/// Set a custom radius function for the sensor to scan
	///
	/// @param radiusFunction The custom function to determine the radius of the scan
	@ApiStatus.NonExtendable
	public NearbyItemsSensor<BO> detectionRadius(Function<BO, SquareRadius> radiusFunction) {
		this.radius = radiusFunction;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@ApiStatus.NonExtendable
	@Override
	public NearbyItemsSensor<BO> setPredicate(BiPredicate<BO, ItemEntity> predicate) {
		return (NearbyItemsSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	@ApiStatus.NonExtendable
	@Override
	public NearbyItemsSensor<BO> scanRate(int scanRate) {
		return (NearbyItemsSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyItemsSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyItemsSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyItemsSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyItemsSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@ApiStatus.NonExtendable
	@Override
	public NearbyItemsSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyItemsSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_ITEMS.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	/// Handle the Sensor's actual function here
	/// 
	/// This is called once every [#scanRate] ticks
	/// 
	/// Be wary of the performance implications of computation-heavy checks here
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		final SquareRadius radius = this.radius.apply(entity);
		final List<ItemEntity> items = EntityRetrievalUtil.getEntities(entity.level(), radius.inflateAABB(entity.getBoundingBox()), ItemEntity.class, item -> predicate().test(entity, item));

		if (!items.isEmpty())
			items.sort(Comparator.comparingDouble(entity::distanceToSqr));

		BrainUtil.setMemory(entity, SBLMemoryTypes.NEARBY_ITEMS.get(), items);
	}
	//</editor-fold>
}


