package net.tslat.smartbrainlib.core.sensor.vanilla;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that looks for the nearest home point of interest in the surrounding area.<br>
 * Defaults:
 * <ul>
 * <li>48 block radius</li>
 * <li>Only runs if the owner of the brain is a baby</li>
 * </ul>
 * 
 * @param <E> The entity
 */
public class NearestHomeSensor<E extends MobEntity> extends PredicateSensor<E, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] { MemoryModuleType.NEAREST_BED });

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
	protected void doTick(ServerWorld level, E entity) {
		if (!predicate().test(entity, entity))
			return;

		this.tries = 0;
		long nodeExpiryTime = level.getGameTime() + level.getRandom().nextInt(20);
		PointOfInterestManager poiManager = level.getPoiManager();
		Predicate<BlockPos> predicate = pos -> {
			if (this.homesMap.containsKey(pos))
				return false;

			if (++this.tries >= 5)
				return false;

			this.homesMap.put(pos, nodeExpiryTime + 40);

			return true;
		};
		Set<BlockPos> poiLocations = poiManager.findAll(poiType -> poiType.equals(PointOfInterestType.HOME), predicate, entity.blockPosition(), this.radius, PointOfInterestManager.Status.ANY).collect(Collectors.toSet());
		// Path pathToHome = AcquirePoi.findPathToPois(entity, poiLocations);
		Path pathToHome = entity.getNavigation().createPath(poiLocations, PointOfInterestType.HOME.getValidRange());

		if (pathToHome != null && pathToHome.canReach()) {
			BlockPos targetPos = pathToHome.getTarget();

			poiManager.getType(targetPos).ifPresent(poiType -> BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_BED, targetPos));
		} else if (this.tries < 5) {
			this.homesMap.object2LongEntrySet().removeIf(pos -> pos.getLongValue() < nodeExpiryTime);
		}
	}
	
}
