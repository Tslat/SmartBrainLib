package net.tslat.smartbrainlib.api.core.sensor.custom;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/// Sensor for identifying and memorising nearby blocks using the [SBLMemoryTypes#NEARBY_BLOCKS] memory module
///
/// @param <BO> The brain owner entity
public class NearbyBlocksSensor<BO extends LivingEntity> extends PredicateSensor<BO, Pair<BlockPos, BlockState>> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(SBLMemoryTypes.NEARBY_BLOCKS.get());

	protected Function<BO, SquareRadius> radius = _ -> new SquareRadius(8, 2);

	public NearbyBlocksSensor() {
		scanRate(30);
		setPredicate((_, posState) -> !posState.getSecond().isAir());
	}

	/// Set the radius for the sensor to scan
	///
	/// @param radius The coordinate radius, in blocks
	@ApiStatus.NonExtendable
	public NearbyBlocksSensor<BO> detectionRadius(double radius) {
		return detectionRadius(radius, radius);
	}

	/// Set the radius for the sensor to scan
	///
	/// @param xz The X/Z coordinate radius, in blocks
	/// @param y The Y coordinate radius, in blocks
	@ApiStatus.NonExtendable
	public NearbyBlocksSensor<BO> detectionRadius(double xz, double y) {
		return detectionRadius(_ -> new SquareRadius(xz, y));
	}

	/// Set a custom radius function for the sensor to scan
	///
	/// @param radiusFunction The custom function to determine the radius of the scan
	@ApiStatus.NonExtendable
	public NearbyBlocksSensor<BO> detectionRadius(Function<BO, SquareRadius> radiusFunction) {
		this.radius = radiusFunction;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@ApiStatus.NonExtendable
	@Override
	public NearbyBlocksSensor<BO> setPredicate(BiPredicate<BO, Pair<BlockPos, BlockState>> predicate) {
		return (NearbyBlocksSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	@ApiStatus.NonExtendable
	@Override
	public NearbyBlocksSensor<BO> scanRate(int scanRate) {
		return (NearbyBlocksSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyBlocksSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyBlocksSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@ApiStatus.NonExtendable
	@Override
	public NearbyBlocksSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyBlocksSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@ApiStatus.NonExtendable
	@Override
	public NearbyBlocksSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyBlocksSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_BLOCKS.get();
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
		final List<BlockInWorld> blocks = new ObjectArrayList<>();
		final SquareRadius radius = this.radius.apply(entity);

		for (BlockPos pos : BlockPos.betweenClosed(radius.inflateAABB(entity.getBoundingBox()))) {
			final BlockState state = level.getBlockState(pos);

			if (predicate().test(entity, Pair.of(pos, state)))
				blocks.add(new BlockInWorld(level, pos, false));
		}

		if (!blocks.isEmpty())
			blocks.sort(Comparator.comparingDouble(block -> block.getPos().distToCenterSqr(entity.position())));

		BrainUtil.setMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get(), blocks);
	}
	//</editor-fold>
}