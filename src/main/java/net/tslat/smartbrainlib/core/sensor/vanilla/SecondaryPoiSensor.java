package net.tslat.smartbrainlib.core.sensor.vanilla;

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
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
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

	protected Vector3i radius = new Vector3i(8, 4, 8);

	public SecondaryPoiSensor() {
		setScanRate(entity -> 40);
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public SecondaryPoiSensor<E> setRadius(int radius) {
		return setRadius(new Vector3i(radius, radius, radius));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public SecondaryPoiSensor<E> setRadius(Vector3i radius) {
		this.radius = radius;

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

		for (BlockPos testPos : BlockPos.betweenClosed(pos.getX() - radius.getX() / 2, pos.getY() - radius.getY() / 2, pos.getZ() - radius.getZ() / 2, pos.getX() + radius.getX() / 2, pos.getY() + radius.getY() / 2, pos.getZ() + radius.getZ() / 2)) {
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
