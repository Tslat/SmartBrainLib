package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * A movement behaviour for automatically following the parent of an {@link AgeableMob AgeableMob}.
 * <p>Note that because vanilla animals do not store a reference to their parent or child, by default this behaviour just grabs the nearest
 * animal of the same class and presumes it is the parent.</p>
 */
public class FollowParent<E extends AgeableMob> extends FollowEntity<E, AgeableMob> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

	private BiPredicate<E, AgeableMob> parentPredicate = (entity, other) -> entity.getClass() == other.getClass() && other.getAge() >= 0;

	public FollowParent() {
		following(this::getParent);
		stopFollowingWithin(2);
	}

	/**
	 * Set the predicate that determines whether a given entity is a suitable 'parent' to follow
	 */
	public FollowParent<E> parentPredicate(BiPredicate<E, AgeableMob> predicate) {
		this.parentPredicate = predicate;

		return this;
	}

	@Override
	public List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return entity.getAge() < 0 && super.checkExtraStartConditions(level, entity);
	}

	@Nullable
	protected AgeableMob getParent(E entity) {
		return BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).findClosest(other -> other instanceof AgeableMob ageableMob && this.parentPredicate.test(entity, ageableMob)).map(AgeableMob.class::cast).orElse(null);
	}
}
