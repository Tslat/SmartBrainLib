package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

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
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).noMemory(MemoryModuleType.WALK_TARGET);

	protected BiPredicate<E, BlockState> validPosition = (entity, state) -> false;
	protected ToFloatBiFunction<E, Vec3> speedModifier = (entity, targetPos) -> 1f;
	protected SquareRadius radius = new SquareRadius(10, 6);
	protected ToIntFunction<E> tries = entity -> 10;

	protected Vec3 targetPos = null;

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
	public SeekRandomNearbyPosition<E> speedModifier(ToFloatBiFunction<E, Vec3> function) {
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
	public SeekRandomNearbyPosition<E> attempts(ToIntFunction<E> function) {
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
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		this.targetPos = getTargetPos(entity);

		return this.targetPos != null;
	}

	@Override
	protected void start(E entity) {
		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, this.speedModifier.applyAsFloat(entity, this.targetPos), 0));
	}

	@Nullable
	protected Vec3 getTargetPos(E entity) {
		BlockPos entityPos = entity.blockPosition();
		BlockPos targetPos = RandomUtil.getRandomPositionWithinRange(entityPos, (int)this.radius.xzRadius(), (int)this.radius.yRadius(), (int)this.radius.xzRadius(), 0, 0, 0, false, entity.level(), 10, (state, pos) -> this.validPosition.test(entity, state));

		return targetPos == entityPos ? null : Vec3.atBottomCenterOf(targetPos);
	}
}