package net.tslat.smartbrainlib.api.core.sensor.custom;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * Sensor for identifying and memorising nearby blocks using the {@link net.tslat.smartbrainlib.registry.SBLMemoryTypes#NEARBY_BLOCKS} memory module. <br>
 * Defaults:
 * <ul>
 *     <li>1-block radius</li>
 *     <li>Ignores air blocks</li>
 * </ul>
 */
public class NearbyBlocksSensor<E extends LivingEntity> extends PredicateSensor<BlockState, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {SBLMemoryTypes.NEARBY_BLOCKS.get()});

	protected SquareRadius radius = new SquareRadius(1, 1);

	public NearbyBlocksSensor() {
		setPredicate((state, entity) -> !state.isAir());
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_BLOCKS.get();
	}

	/**
	 * Set the radius for the sensor to scan
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public NearbyBlocksSensor<E> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius for the sensor to scan.
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y The Y coordinate radius, in blocks
	 * @return this
	 */
	public NearbyBlocksSensor<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		List<Pair<BlockPos, BlockState>> blocks = new ObjectArrayList<>();

		for (BlockPos pos : BlockPos.betweenClosed(entity.blockPosition().subtract(this.radius.toVec3i()), entity.blockPosition().offset(this.radius.toVec3i()))) {
			BlockState state = level.getBlockState(pos);

			if (this.predicate().test(state, entity))
				blocks.add(Pair.of(pos.immutable(), state));
		}

		if (blocks.isEmpty()) {
			BrainUtils.clearMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get());
		}
		else {
			BrainUtils.setMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get(), blocks);
		}
	}
}
