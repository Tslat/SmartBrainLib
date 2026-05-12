package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A sensor that looks for a nearby [POI][net.minecraft.world.entity.ai.village.poi.PoiTypes] block that matches a villager's secondary profession.
///
/// Functionally a replica of [net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor]
///
/// @param <BO> The brain owner entity
public class SecondaryPoiSensor<BO extends Villager> extends ExtendedSensor<BO> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.SECONDARY_JOB_SITE);

	protected Function<BO, SquareRadius> radius = _ -> new SquareRadius(8, 4);

	public SecondaryPoiSensor() {
		scanRate(_ -> 40);
	}

	/// Set the radius for the sensor to scan.
	///
	/// @param radius The coordinate radius, in blocks
	public SecondaryPoiSensor<BO> setRadius(int radius) {
		return setRadius(radius, radius);
	}

	/// Set the radius for the sensor to scan
	///
	/// @param xz The X/Z coordinate radius, in blocks
	/// @param y  The Y coordinate radius, in blocks
	public SecondaryPoiSensor<BO> setRadius(double xz, double y) {
		return setRadius(_ -> new SquareRadius(xz, y));
	}

	/// Set the radius function for the sensor to scan
	///
	/// @param function The radius function
	public SecondaryPoiSensor<BO> setRadius(Function<BO, SquareRadius> function) {
		this.radius = function;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the scan rate for this sensor
	public SecondaryPoiSensor<BO> scanRate(int scanRate) {
		return scanRate(_ -> scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public SecondaryPoiSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (SecondaryPoiSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public SecondaryPoiSensor<BO> afterScanning(Consumer<BO> callback) {
		return (SecondaryPoiSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public SecondaryPoiSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (SecondaryPoiSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.SECONDARY_POI.get();
	}

	/// Vanilla's implementation of the required memory collection. Functionally replaced by [ExtendedSensor#memoriesUsed()]<br/>
	/// Left in place for compatibility reasons
	///
	/// @return A set view of the list returned by `memoriesUsed()`
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
		final Set<Block> testPoiBlocks = entity.getVillagerData().profession().value().secondaryPoi();

		if (testPoiBlocks.isEmpty())
			return;

		final ResourceKey<Level> dimension = level.dimension();
		final List<GlobalPos> poiPositions = new ObjectArrayList<>();
		final SquareRadius radius = this.radius.apply(entity);

		try (BulkSectionAccess access = new BulkSectionAccess(level)) {
			for (BlockPos testPos : BlockPos.betweenClosed(radius.createRegion(entity.position()))) {
				if (testPoiBlocks.contains(access.getBlockState(testPos).getBlock()))
					poiPositions.add(GlobalPos.of(dimension, testPos.immutable()));
			}
		}

		BrainUtil.setMemory(entity, MemoryModuleType.SECONDARY_JOB_SITE, poiPositions);
	}
	//</editor-fold>
}
