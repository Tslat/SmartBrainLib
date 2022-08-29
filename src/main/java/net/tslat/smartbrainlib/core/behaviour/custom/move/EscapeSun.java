package net.tslat.smartbrainlib.core.behaviour.custom.move;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour;

/**
 * Sets the {@link MemoryModuleType#WALK_TARGET walk target} to a safe position if caught in the sun. <br>
 * Defaults:
 * <ul>
 *     <li>Only if not currently fighting something</li>
 *     <li>Only if already burning from the sun</li>
 * </ul>
 * @param <E> The entity
 */
public class EscapeSun<E extends CreatureEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)});

	private float speedModifier = 1;

	private Vector3d hidePos = null;

	/**
	 * Set the movespeed modifier for when the entity tries to escape the sun
	 * @param speedMod The speed modifier
	 * @return this
	 */
	public EscapeSun<E> speedModifier(float speedMod) {
		this.speedModifier = speedMod;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		if (!level.isDay() || !entity.isOnFire() || !level.canSeeSky(entity.blockPosition()))
			return false;

		if (!entity.getItemBySlot(EquipmentSlotType.HEAD).isEmpty())
			return false;

		this.hidePos = getHidePos(entity);

		return this.hidePos != null;
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.hidePos, this.speedModifier, 0));
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		if (this.hidePos == null)
			return false;

		WalkTarget walkTarget = BrainUtils.getMemory(entity, MemoryModuleType.WALK_TARGET);

		if (walkTarget == null)
			return false;

		return walkTarget.getTarget().currentBlockPosition().equals(new BlockPos(this.hidePos)) && !entity.getNavigation().isDone();
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return false;
	}

	@Override
	protected void stop(E entity) {
		this.hidePos = null;
	}

	@Nullable
	protected Vector3d getHidePos(E entity) {
		Random randomsource = entity.getRandom();
		BlockPos entityPos = entity.blockPosition();

		for(int i = 0; i < 10; ++i) {
			BlockPos hidePos = entityPos.offset(randomsource.nextInt(20) - 10, randomsource.nextInt(6) - 3, randomsource.nextInt(20) - 10);

			if (!entity.level.canSeeSky(hidePos) && entity.getWalkTargetValue(hidePos) < 0.0F)
				return Vector3d.atBottomCenterOf(hidePos);
		}

		return null;
	}
}
