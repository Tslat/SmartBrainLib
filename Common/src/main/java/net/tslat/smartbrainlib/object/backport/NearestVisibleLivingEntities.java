package net.tslat.smartbrainlib.object.backport;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Backported from modern Minecraft
 */
public class NearestVisibleLivingEntities {
	private static final NearestVisibleLivingEntities EMPTY = new NearestVisibleLivingEntities();
	private final List<LivingEntity> nearbyEntities;
	private final Predicate<LivingEntity> lineOfSightTest;

	private NearestVisibleLivingEntities() {
		this.nearbyEntities = Collections.list();
		this.lineOfSightTest = entity -> false;
	}

	public NearestVisibleLivingEntities(LivingEntity entity, List<LivingEntity> nearbyEntities) {
		this.nearbyEntities = nearbyEntities;
		Object2BooleanOpenHashMap<LivingEntity> visibilityMap = new Object2BooleanOpenHashMap<>(nearbyEntities.size());
		Function<LivingEntity, Boolean> predicate = target -> Sensor.isEntityTargetable(entity, target);
		this.lineOfSightTest = entity2 -> visibilityMap.computeIfAbsent(entity2, predicate);
	}

	public static NearestVisibleLivingEntities empty() {
		return EMPTY;
	}

	public Optional<LivingEntity> findClosest(Predicate<LivingEntity> pPredicate) {
		for(LivingEntity entity : this.nearbyEntities) {
			if (pPredicate.test(entity) && this.lineOfSightTest.test(entity))
				return Optional.of(entity);
		}

		return Optional.empty();
	}

	public Iterable<LivingEntity> findAll(Predicate<LivingEntity> predicate) {
		return this.nearbyEntities.stream().filter(entity -> predicate.test(entity) && this.lineOfSightTest.test(entity)).collect(Collectors.toList());
	}

	public Stream<LivingEntity> find(Predicate<LivingEntity> predicate) {
		return this.nearbyEntities.stream().filter(entity -> predicate.test(entity) && this.lineOfSightTest.test(entity));
	}

	public boolean contains(LivingEntity pEntity) {
		return this.nearbyEntities.contains(pEntity) && this.lineOfSightTest.test(pEntity);
	}

	public boolean contains(Predicate<LivingEntity> predicate) {
		for(LivingEntity entity : this.nearbyEntities) {
			if (predicate.test(entity) && this.lineOfSightTest.test(entity))
				return true;
		}

		return false;
	}
}
