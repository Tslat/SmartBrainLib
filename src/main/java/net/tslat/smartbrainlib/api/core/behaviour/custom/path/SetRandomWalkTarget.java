package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import java.util.List;

import javax.annotation.Nullable;

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
public class SetRandomWalkTarget<E extends CreatureEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT)});

	protected float speedMod = 1;
	protected SquareRadius radius = new SquareRadius(10, 7);

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
		this.speedMod = modifier;

		return this;
	}

	@Override
	protected void start(E entity) {
		Vector3d targetPos = getTargetPos(entity);

		if (targetPos == null) {
			BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
		else {
			BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speedMod, 0));
		}
	}

	@Nullable
	protected Vector3d getTargetPos(E entity) {
		return RandomPositionGenerator.getLandPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
	}
}
