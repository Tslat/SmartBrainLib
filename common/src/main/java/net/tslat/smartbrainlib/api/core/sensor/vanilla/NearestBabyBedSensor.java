package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestBedSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/// A sensor that looks for the nearest home point of interest in the surrounding area<br/>
/// For some reason it only applies if the entity is a baby
///
/// @see NearestBedSensor
/// @param <BO> The brain owner entity
public class NearestBabyBedSensor<BO extends Mob> extends PredicateSensor<BO, Void> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_BED);

	protected final Object2LongOpenHashMap<BlockPos> homesMap = new Object2LongOpenHashMap<>(5);
	protected ToIntFunction<BO> radius = _ -> 48;

	public NearestBabyBedSensor() {
		super((entity, _) -> entity.isBaby());
	}

	/// Set the radius for the item sensor to scan
	///
	/// @param radius The radius
	public NearestBabyBedSensor<BO> setRadius(int radius) {
		return setRadius(_ -> radius);
	}

	/// Set the radius for the item sensor to scan
	///
	/// @param radiusFunction The function to determine the radius at the current scan tick
	public NearestBabyBedSensor<BO> setRadius(ToIntFunction<BO> radiusFunction) {
		this.radius = radiusFunction;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public NearestBabyBedSensor<BO> setPredicate(BiPredicate<BO, Void> predicate) {
		return (NearestBabyBedSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public NearestBabyBedSensor<BO> scanRate(int scanRate) {
		return (NearestBabyBedSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public NearestBabyBedSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearestBabyBedSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public NearestBabyBedSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearestBabyBedSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public NearestBabyBedSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearestBabyBedSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEAREST_HOME.get();
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
        //noinspection DataFlowIssue
        if (!predicate().test(entity, null))
			return;

		final MutableInt tries = new MutableInt();
		final long nodeExpiryTime = level.getGameTime() + level.getRandom().nextInt(20);
		final PoiManager poiManager = level.getPoiManager();
		final Predicate<BlockPos> predicate = pos -> {
			if (this.homesMap.containsKey(pos))
				return false;

			if (tries.incrementAndGet() >= 5)
				return false;

			this.homesMap.put(pos, nodeExpiryTime + 40);

			return true;
		};
		final Set<Pair<Holder<PoiType>, BlockPos>> poiLocations = poiManager.findAllWithType(poiType -> poiType.is(PoiTypes.HOME), predicate, entity.blockPosition(), this.radius.applyAsInt(entity), PoiManager.Occupancy.ANY).collect(Collectors.toSet());
		final Path pathToHome = AcquirePoi.findPathToPois(entity, poiLocations);

		if (pathToHome != null && pathToHome.canReach()) {
			BlockPos targetPos = pathToHome.getTarget();

			poiManager.getType(targetPos).ifPresent(_ -> BrainUtil.setMemory(entity, MemoryModuleType.NEAREST_BED, targetPos));
		}
		else if (tries.intValue() < 5) {
			this.homesMap.object2LongEntrySet().removeIf(pos -> pos.getLongValue() < nodeExpiryTime);
		}
	}
	//</editor-fold>
}
