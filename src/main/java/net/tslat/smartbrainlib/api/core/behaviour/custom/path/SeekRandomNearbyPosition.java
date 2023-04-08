package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.RandomUtil;
import net.tslat.smartbrainlib.object.SquareRadius;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Walk target class that finds a random position nearby and sets it as the walk target if applicable. <br>
 * Useful for finding quick alternate paths for specific purposes. <br>
 * Defaults:
 * <ul>
 *     <li>10x6 block search radius</li>
 *     <li>1x Movespeed modifier</li>
 *     <li>10 Attempts at finding a position before giving up</li>
 * </ul>
 * @param <E> The entity
 */
public class SeekRandomNearbyPosition<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORIES = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT)});

	protected BiPredicate<E, BlockState> validPosition = (entity, state) -> false;
	protected BiFunction<E, Vector3d, Float> speedModifier = (entity, targetPos) -> 1f;
	protected SquareRadius radius = new SquareRadius(10, 6);
	protected Function<E, Integer> tries = entity -> 10;

	protected Vector3d targetPos = null;

	/**
	 * Set the radius in which to look for walk positions.
	 * @param radius The coordinate radius, in blocks
	 * @return this
	 */
	public SeekRandomNearbyPosition<E> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/**
	 * Set the radius in which to look for walk positions.
	 * @param xz The X/Z coordinate radius, in blocks
	 * @param y The Y coordinate radius, in blocks
	 * @return this
	 */
	public SeekRandomNearbyPosition<E> setRadius(double xz, double y) {
		this.radius = new SquareRadius(xz, y);

		return this;
	}

	/**
	 * Set the movespeed modifier for the path when chosen.
	 * @param modifier The movespeed modifier/multiplier
	 * @return this
	 */
	public SeekRandomNearbyPosition<E> speedModifier(float modifier) {
		return speedModifier((entity, targetPos) -> modifier);
	}

	/**
	 * Set the movespeed modifier for the path when chosen.
	 * @param function The movespeed modifier/multiplier function
	 * @return this
	 */
	public SeekRandomNearbyPosition<E> speedModifier(BiFunction<E, Vector3d, Float> function) {
		this.speedModifier = function;

		return this;
	}

	/**
	 * Sets the number of positions to check before giving up on finding a valid target.
	 * @param attempts The number of attempts
	 * @return this
	 */
	public SeekRandomNearbyPosition<E> attempts(int attempts) {
		return attempts(entity -> attempts);
	}

	/**
	 * Sets the number of positions to check before giving up on finding a valid target.
	 * @param function The attempts function
	 * @return this
	 */
	public SeekRandomNearbyPosition<E> attempts(Function<E, Integer> function) {
		this.tries = function;

		return this;
	}

	/**
	 * Set the predicate that determines the validity of positions when searching
	 * @param predicate The predicate
	 * @return this
	 */
	public SeekRandomNearbyPosition<E> validPositions(BiPredicate<E, BlockState> predicate) {
		this.validPosition = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORIES;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		this.targetPos = getTargetPos(entity);

		return this.targetPos != null;
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, this.speedModifier.apply(entity, this.targetPos), 0));
	}

	@Nullable
	protected Vector3d getTargetPos(E entity) {
		BlockPos entityPos = entity.blockPosition();
		BlockPos targetPos = RandomUtil.getRandomPositionWithinRange(entityPos, (int)this.radius.xzRadius(), (int)this.radius.yRadius(), (int)this.radius.xzRadius(), 0, 0, 0, false, entity.level, 10, (state, pos) -> this.validPosition.test(entity, state));

		return targetPos == entityPos ? null : Vector3d.atBottomCenterOf(targetPos);
	}
}
