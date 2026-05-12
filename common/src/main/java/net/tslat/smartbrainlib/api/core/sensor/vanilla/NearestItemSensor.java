package net.tslat.smartbrainlib.api.core.sensor.vanilla;

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
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;

import java.util.List;
import java.util.function.*;

/// A sensor that looks for the nearest item entity in the surrounding area
///
/// Use [PredicateSensor#setPredicate(BiPredicate)] to update the predicate for valid items to sense
///
/// @param <BO> The brain owner entity
public class NearestItemSensor<BO extends Mob> extends PredicateSensor<BO, ItemEntity> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);

	protected Function<BO, SquareRadius> radius = _ -> new SquareRadius(32, 16);

	public NearestItemSensor() {
		super((entity, item) -> entity.wantsToPickUp((ServerLevel)entity.level(), item.getItem()) && entity.hasLineOfSight(item));
	}

	/// Set the radius for the item sensor to scan
	///
	/// @param radius The coordinate radius, in blocks
	/// @return this
	public NearestItemSensor<BO> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/// Set the radius for the item sensor to scan
	///
	/// @param xz The X/Z coordinate radius, in blocks
	/// @param y  The Y coordinate radius, in blocks
	/// @return this
	public NearestItemSensor<BO> setRadius(double xz, double y) {
		return setRadius(_ -> new SquareRadius(xz, y));
	}

	/// Set the radius for the sensor to scan
	///
	/// @param radiusFunction The function to determine the radius for the current scan tick
	public NearestItemSensor<BO> setRadius(Function<BO, SquareRadius> radiusFunction) {
		this.radius = radiusFunction;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	public NearestItemSensor<BO> setPredicate(BiPredicate<BO, ItemEntity> predicate) {
		return (NearestItemSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public NearestItemSensor<BO> scanRate(int scanRate) {
		return (NearestItemSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public NearestItemSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearestItemSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public NearestItemSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearestItemSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public NearestItemSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearestItemSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEAREST_ITEM.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		final SquareRadius radius = this.radius.apply(entity);
		final ItemEntity nearestItem = EntityRetrievalUtil.getNearestEntity(entity, radius.xzRadius(), radius.yRadius(), radius.xzRadius(), ItemEntity.class, item ->
				predicate().test(entity, item)).orElse(null);

		BrainUtil.setOrClearMemory(entity, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, nearestItem);
	}
	//</editor-fold>
}
