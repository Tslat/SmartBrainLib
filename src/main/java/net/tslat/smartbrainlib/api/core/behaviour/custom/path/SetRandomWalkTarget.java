package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.vector.Vector3d;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.object.SquareRadius;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Set a random position to walk to. <br>
 * Defaults:
 * <ul>
 *     <li>1x movespeed modifier</li>
 *     <li>10-block lateral radius</li>
 *     <li>7-block vertical radius</li>
 *     <li>Avoids walk targets with fluid</li>
 * </ul>
 * @param <E>
 */
public class SetRandomWalkTarget<E extends CreatureEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT)});

	protected BiFunction<E, Vector3d, Float> speedModifier = (entity, targetPos) -> 1f;
	protected Predicate<E> avoidWaterPredicate = entity -> true;
	protected SquareRadius radius = new SquareRadius(10, 7);
	protected BiPredicate<E, Vector3d> positionPredicate = (entity, pos) -> true;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	/**
	 * Set the radius in which to look for walk positions.
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public SetRandomWalkTarget<E> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius in which to look for walk positions.
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y The Y coordinate radius, in blocks
	 * @return this
	 */
	public SetRandomWalkTarget<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	/**
	 * Set the movespeed modifier for the path when chosen.
	 * @param modifier The movespeed modifier/multiplier
	 * @return this
	 */
	public SetRandomWalkTarget<E> speedModifier(float modifier) {
		return speedModifier((entity, targetPos) -> modifier);
	}

	/**
	 * Set the movespeed modifier for the path when chosen.
	 * @param function The movespeed modifier/multiplier function
	 * @return this
	 */
	public SetRandomWalkTarget<E> speedModifier(BiFunction<E, Vector3d, Float> function) {
		this.speedModifier = function;

		return this;
	}

	/**
	 * Sets a predicate to check whether the target movement position is valid or not
	 * @param predicate The predicate
	 * @return this
	 */
	public SetRandomWalkTarget<E> walkTargetPredicate(BiPredicate<E, Vector3d> predicate) {
		this.positionPredicate = predicate;

		return this;
	}

	/**
	 * Sets the behaviour to allow finding of positions that might be in water. <br>
	 * Useful for hybrid or water-based entities.
	 * @return this
	 */
	public SetRandomWalkTarget<E> dontAvoidWater() {
		return avoidWaterWhen(entity -> false);
	}

	/**
	 * Set the predicate to determine when the entity should avoid water walk targets;
	 * @param predicate The predicate
	 * @return this
	 */
	public SetRandomWalkTarget<E> avoidWaterWhen(Predicate<E> predicate) {
		this.avoidWaterPredicate = predicate;

		return this;
	}

	@Override
	protected void start(E entity) {
		Vector3d targetPos = getTargetPos(entity);

		if (!this.positionPredicate.test(entity, targetPos))
			targetPos = null;

		if (targetPos == null) {
			BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
		else {
			BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speedModifier.apply(entity, targetPos), 0));
		}
	}

	@Nullable
	protected Vector3d getTargetPos(E entity) {
		if (this.avoidWaterPredicate.test(entity)) {
			return RandomPositionGenerator.getLandPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
		}
		else {
			//Not sure here
			return RandomPositionGenerator.getLandPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
			//return DefaultRandomPos.getPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
		}
	}
}
