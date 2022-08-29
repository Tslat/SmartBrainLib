package net.tslat.smartbrainlib.core.behaviour.custom.move;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour;

/**
 * Movement behaviour to handle strafing. <br>
 * Defaults:
 * <ul>
 *     <li>Continues strafing until the target is no longer in memory</li>
 * </ul>
 * @param <E> The entity
 */
public class StrafeTarget<E extends MobEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT)});

	private boolean strafingLaterally = false;
	private boolean strafingBack = false;
	private int strafeCounter = -1;

	private float strafeDistanceSqr = 244;
	private Predicate<E> stopStrafingWhen = entity -> false;
	private float speedMod = 1;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	/**
	 * Set a custom condition for when the strafing should end.
	 * @param predicate The predicate
	 * @return this
	 */
	public StrafeTarget<E> stopStrafingWhen(Predicate<E> predicate) {
		this.stopStrafingWhen = predicate;

		return this;
	}

	/**
	 * Set how far the entity should attempt to stay away from the target whilst strafing.
	 * @param distance The distance, in blocks
	 * @return this
	 */
	public StrafeTarget<E> strafeDistance(float distance) {
		this.strafeDistanceSqr = distance * distance;

		return this;
	}

	/**
	 * Set the movespeed modifier for when the entity is strafing.
	 * @param modifier The multiplier for movement speed
	 * @return this
	 */
	public StrafeTarget<E> speedMod(float modifier) {
		this.speedMod = modifier;

		return this;
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		return BrainUtils.hasMemory(entity, MemoryModuleType.ATTACK_TARGET) && this.stopStrafingWhen.test(entity);
	}

	@Override
	protected void tick(E entity) {
		LivingEntity target = BrainUtils.getTargetOfEntity(entity);
		double distanceToTarget = target.distanceToSqr(entity);

		if (distanceToTarget <= this.strafeDistanceSqr) {
			entity.getNavigation().stop();
			this.strafeCounter++;
		}
		else {
			entity.getNavigation().moveTo(target, this.speedMod);
			this.strafeCounter = -1;
		}

		if (this.strafeCounter >= 20) {
			if (entity.getRandom().nextFloat() < 0.3)
				this.strafingLaterally = !this.strafingLaterally;

			if (entity.getRandom().nextFloat() < 0.3)
				this.strafingBack = !this.strafingBack;

			this.strafeCounter = 0;
		}

		if (this.strafeCounter > -1) {
			if (distanceToTarget > this.strafeDistanceSqr * 0.75f) {
				this.strafingBack = false;
			}
			else if (distanceToTarget < this.strafeDistanceSqr * 0.25f) {
				this.strafingBack = true;
			}

			entity.lookAt(target, 30, 30);
			entity.getMoveControl().strafe(this.strafingBack ? -0.5f : 0.5f, this.strafingLaterally ? 0.5f : -0.5f);
		}
	}
}
