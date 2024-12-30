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
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A sensor that looks for the nearest home point of interest in the surrounding
 * area.<br>
 * Defaults:
 * <ul>
 * <li>48 block radius</li>
 * <li>Only runs if the owner of the brain is a baby</li>
 * </ul>
 * 
 * @param <E> The entity
 */
public class NearestHomeSensor<E extends Mob> extends PredicateSensor<E, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_BED);

	protected int radius = 48;

	private final Object2LongOpenHashMap<BlockPos> homesMap = new Object2LongOpenHashMap<>(5);
	private int tries = 0;

	public NearestHomeSensor() {
		super((brainOwner, entity) -> brainOwner.isBaby());
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public NearestHomeSensor<E> setRadius(int radius) {
		this.radius = radius;

		return this;
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEAREST_HOME.get();
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		if (!predicate().test(entity, entity))
			return;

		this.tries = 0;
		long nodeExpiryTime = level.getGameTime() + level.getRandom().nextInt(20);
		PoiManager poiManager = level.getPoiManager();
		Predicate<BlockPos> predicate = pos -> {
			if (this.homesMap.containsKey(pos))
				return false;

			if (++this.tries >= 5)
				return false;

			this.homesMap.put(pos, nodeExpiryTime + 40);

			return true;
		};
		Set<Pair<Holder<PoiType>, BlockPos>> poiLocations = poiManager.findAllWithType(poiType -> poiType.is(PoiTypes.HOME), predicate, entity.blockPosition(), this.radius, PoiManager.Occupancy.ANY).collect(Collectors.toSet());
		Path pathToHome = AcquirePoi.findPathToPois(entity, poiLocations);

		if (pathToHome != null && pathToHome.canReach()) {
			BlockPos targetPos = pathToHome.getTarget();

			poiManager.getType(targetPos).ifPresent(poiType -> BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_BED, targetPos));
		}
		else if (this.tries < 5) {
			this.homesMap.object2LongEntrySet().removeIf(pos -> pos.getLongValue() < nodeExpiryTime);
		}
	}
}
