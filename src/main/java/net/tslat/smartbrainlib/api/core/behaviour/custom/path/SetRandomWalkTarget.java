package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.object.SquareRadius;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Set a random position to walk to. <br>
 * Defaults:
 * <ul>
 *     <li>1x movespeed modifier</li>
 *     <li>10-block lateral radius</li>
 *     <li>7-block vertical radius</li>
 * </ul>
 * @param <E>
 */
public class SetRandomWalkTarget<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));

	protected float speedMod = 1;
	protected boolean avoidsWater = true;
	protected SquareRadius radius = new SquareRadius(10, 7);
	protected BiPredicate<E, Vec3> positionPredicate = (entity, pos) -> true;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
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
		this.speedMod = modifier;

		return this;
	}

	/**
	 * Sets a predicate to check whether the target movement position is valid or not
	 * @param predicate The predicate
	 * @return this
	 */
	public SetRandomWalkTarget<E> walkTargetPredicate(BiPredicate<E, Vec3> predicate) {
		this.positionPredicate = predicate;

		return this;
	}

	/**
	 * Sets the behaviour to allow finding of positions that might be in water. <br>
	 * Useful for hybrid or water-based entities.
	 * @return this
	 */
	public SetRandomWalkTarget<E> dontAvoidWater() {
		this.avoidsWater = false;

		return this;
	}

	@Override
	protected void start(E entity) {
		Vec3 targetPos = getTargetPos(entity);

		if (!this.positionPredicate.test(entity, targetPos))
			targetPos = null;

		if (targetPos == null) {
			BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
		else {
			BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speedMod, 0));
		}
	}

	@Nullable
	protected Vec3 getTargetPos(E entity) {
		if (this.avoidsWater) {
			return LandRandomPos.getPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
		}
		else {
			return DefaultRandomPos.getPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
		}
	}
}
