package net.tslat.smartbrainlib.core.sensor.vanilla;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;

import java.util.List;

/**
 * A sensor that looks for a nearby {@link net.minecraft.world.entity.ai.village.poi.PoiTypes POI} block that matches a villager's secondary profession.<br/>
 * Defaults:
 * <ul>
 *     <li>40-tick scan rate</li>
 *     <li>8x4x8 radius</li>
 * </ul>
 * @param <E> The entity
 */
public class SecondaryPoiSensor<E extends Villager> extends ExtendedSensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.SECONDARY_JOB_SITE);

	protected Vec3i radius = new Vec3i(8, 4, 8);

	public SecondaryPoiSensor() {
		setScanRate(ConstantInt.of(40));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public SecondaryPoiSensor<E> setRadius(int radius) {
		return setRadius(new Vec3i(radius, radius, radius));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public SecondaryPoiSensor<E> setRadius(Vec3i radius) {
		this.radius = radius;

		return this;
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		ResourceKey<Level> dimension = level.dimension();
		BlockPos pos = entity.blockPosition();
		ImmutableSet<Block> testPoiBlocks = entity.getVillagerData().getProfession().secondaryPoi();
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
