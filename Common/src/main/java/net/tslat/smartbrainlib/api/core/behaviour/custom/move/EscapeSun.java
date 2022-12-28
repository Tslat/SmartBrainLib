package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Sets the {@link MemoryModuleType#WALK_TARGET walk target} to a safe position if caught in the sun. <br>
 * Defaults:
 * <ul>
 *     <li>Only if not currently fighting something</li>
 *     <li>Only if already burning from the sun</li>
 * </ul>
 * @param <E> The entity
 */
public class EscapeSun<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));

	protected float speedModifier = 1;

	protected Vec3 hidePos = null;

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
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		if (!level.isDay() || !entity.isOnFire() || !level.canSeeSky(entity.blockPosition()))
			return false;

		if (!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
			return false;

		this.hidePos = getHidePos(entity);

		return this.hidePos != null;
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.hidePos, this.speedModifier, 0));
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
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
	protected Vec3 getHidePos(E entity) {
		Random randomsource = entity.getRandom();
		BlockPos entityPos = entity.blockPosition();

		for(int i = 0; i < 10; ++i) {
			BlockPos hidePos = entityPos.offset(randomsource.nextInt(20) - 10, randomsource.nextInt(6) - 3, randomsource.nextInt(20) - 10);

			if (!entity.level.canSeeSky(hidePos) && entity.getWalkTargetValue(hidePos) < 0.0F)
				return Vec3.atBottomCenterOf(hidePos);
		}

		return null;
	}
}
