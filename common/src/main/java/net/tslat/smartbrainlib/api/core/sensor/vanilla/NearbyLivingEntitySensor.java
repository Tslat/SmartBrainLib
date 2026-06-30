package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.library.object.FixedNearestVisibleLivingEntities;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/// A sensor that looks for nearby living entities in the surrounding area, sorted by proximity to the brain owner
///
/// @see NearestLivingEntitySensor
/// @param <BO> The brain owner entity
public class NearbyLivingEntitySensor<BO extends LivingEntity> extends PredicateSensor<BO, LivingEntity> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

	protected Function<BO, SquareRadius> radius = entity -> new SquareRadius(entity.getAttributeValue(Attributes.FOLLOW_RANGE));

	public NearbyLivingEntitySensor() {
		super((entity, target) -> target != entity && target.isAlive());
	}

	/// Set the radius for the sensor to scan
	///
	/// @param radius The coordinate radius, in blocks
	@ApiStatus.NonExtendable
	public NearbyLivingEntitySensor<BO> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/// Set the radius for the sensor to scan
	///
	/// @param xz The X/Z coordinate radius, in blocks
	/// @param y  The Y coordinate radius, in blocks
	@ApiStatus.NonExtendable
	public NearbyLivingEntitySensor<BO> setRadius(double xz, double y) {
		return setRadius(_ -> new SquareRadius(xz, y));
	}

	/// Set the radius for the sensor to scan
	///
	/// @param radiusFunction The function to determine the radius for the current scan tick
	@ApiStatus.NonExtendable
	public NearbyLivingEntitySensor<BO> setRadius(Function<BO, SquareRadius> radiusFunction) {
		this.radius = radiusFunction;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@ApiStatus.NonExtendable
	@Override
	public NearbyLivingEntitySensor<BO> setPredicate(BiPredicate<BO, LivingEntity> predicate) {
		return (NearbyLivingEntitySensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	@ApiStatus.NonExtendable
	@Override
	public NearbyLivingEntitySensor<BO> scanRate(int scanRate) {
		return (NearbyLivingEntitySensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyLivingEntitySensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyLivingEntitySensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyLivingEntitySensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyLivingEntitySensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@ApiStatus.NonExtendable
	@Override
	public NearbyLivingEntitySensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyLivingEntitySensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_LIVING_ENTITY.get();
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
		final List<LivingEntity> entities = EntityRetrievalUtil.getEntities(entity, radius.xzRadius(), radius.yRadius(), radius.xzRadius(), LivingEntity.class, nearby -> predicate().test(entity, nearby));

		entities.sort(Comparator.comparingDouble(entity::distanceToSqr));

		BrainUtil.setMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities);
		BrainUtil.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new FixedNearestVisibleLivingEntities(entity, entities));
	}
	//</editor-fold>
}
