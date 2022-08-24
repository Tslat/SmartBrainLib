package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import java.util.List;

import com.google.common.collect.ImmutableSet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that looks for a nearby {@link net.minecraft.world.entity.ai.village.poi.PoiTypes POI} block that matches a villager's secondary profession.<br>
 * Defaults:
 * <ul>
 *     <li>40-tick scan rate</li>
 *     <li>8x4x8 radius</li>
 * </ul>
 * @param <E> The entity
 */
public class SecondaryPoiSensor<E extends VillagerEntity> extends ExtendedSensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.SECONDARY_JOB_SITE});

	protected SquareRadius radius = new SquareRadius(8, 4);

	public SecondaryPoiSensor() {
		setScanRate(entity -> 40);
	}

	/**
	 * Set the radius for the sensor to scan.
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public SecondaryPoiSensor<E> setRadius(int radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius for the sensor to scan
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y The Y coordinate radius, in blocks
	 * @return this
	 */
	public SecondaryPoiSensor<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.SECONDARY_POI.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		RegistryKey<World> dimension = level.dimension();
		BlockPos pos = entity.blockPosition();
		ImmutableSet<Block> testPoiBlocks = entity.getVillagerData().getProfession().getSecondaryPoi();
		List<GlobalPos> poiPositions = new ObjectArrayList<>();

		if (testPoiBlocks.isEmpty())
			return;

		for (BlockPos testPos : BlockPos.betweenClosed(pos.getX() - (int)this.radius.xzRadius() / 2, pos.getY() - (int)this.radius.yRadius() / 2, pos.getZ() - (int)this.radius.xzRadius() / 2, pos.getX() + (int)this.radius.xzRadius() / 2, pos.getY() + (int)this.radius.yRadius() / 2, pos.getZ() + (int)this.radius.xzRadius() / 2)) {
			if (testPoiBlocks.contains(level.getBlockState(testPos).getBlock()))
				poiPositions.add(GlobalPos.of(dimension, testPos.immutable()));
		}

		if (poiPositions.isEmpty()) {
			BrainUtils.clearMemory(entity, MemoryModuleType.SECONDARY_JOB_SITE);
		}
		else {
			BrainUtils.setMemory(entity, MemoryModuleType.SECONDARY_JOB_SITE, poiPositions);
		}
	}
}
