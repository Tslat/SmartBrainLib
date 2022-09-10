package net.tslat.smartbrainlib.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.Sensor;

public class NearestVisibleLivingEntities extends ArrayList<LivingEntity> {

	private static final long serialVersionUID = 275532968616126431L;
	private static final NearestVisibleLivingEntities EMPTY = new NearestVisibleLivingEntities();
	private final Predicate<LivingEntity> lineOfSightTest;

	private NearestVisibleLivingEntities() {
		this.lineOfSightTest = (curEntity) -> {
			return false;
		};
	}

	public NearestVisibleLivingEntities(LivingEntity owner, List<LivingEntity> nearbyEntities) {
		this(nearbyEntities, (entityIn) -> {
			return Sensor.isEntityTargetable(owner, entityIn);
		});
	}

	public NearestVisibleLivingEntities(List<LivingEntity> nearbyEntities, Predicate<LivingEntity> lineOfSightPredicate) {
		super(nearbyEntities);
		this.lineOfSightTest = lineOfSightPredicate;
	}

	public static NearestVisibleLivingEntities empty() {
		return EMPTY;
	}

	public Optional<LivingEntity> findFirstMatchingEntry(Predicate<LivingEntity> condition) {
		for (LivingEntity livingentity : this) {
			if (condition.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
				return Optional.of(livingentity);
			}
		}

		return Optional.empty();
	}

	public List<LivingEntity> findAllMatchingEntries(Predicate<LivingEntity> condition) {
		Set<LivingEntity> result = new HashSet<>();
		for (LivingEntity living : this) {
			if (condition.test(living) && this.lineOfSightTest.test(living)) {
				result.add(living);
			}
		}
		return new ArrayList<>(result);
	}

	public boolean contains(LivingEntity entityToTest) {
		return super.contains(entityToTest) && this.lineOfSightTest.test(entityToTest);
	}

	public boolean containsEntryMatching(Predicate<LivingEntity> condition) {
		for (LivingEntity livingentity : this) {
			if (condition.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
				return true;
			}
		}

		return false;
	}

}
