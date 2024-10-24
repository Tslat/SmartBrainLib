package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.ToIntBiFunction;

/**
 * Path setting behaviour for walking to/near a block position.
 * @param <E> The entity
 */
public class SetWalkTargetToBlock<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(SBLMemoryTypes.NEARBY_BLOCKS.get());

	protected BiPredicate<E, Pair<BlockPos, BlockState>> predicate = (entity, block) -> true;
	protected ToFloatBiFunction<E, Pair<BlockPos, BlockState>> speedMod = (owner, pos) -> 1f;
	protected ToIntBiFunction<E, Pair<BlockPos, BlockState>> closeEnoughDist = (entity, pos) -> 2;

	protected Pair<BlockPos, BlockState> target = null;

	/**
	 * Set the predicate to determine whether a given position/state should be the target path
	 * @param predicate The predicate
	 * @return this
	 */
	public SetWalkTargetToBlock<E> predicate(final BiPredicate<E, Pair<BlockPos, BlockState>> predicate) {
		this.predicate = predicate;

		return this;
	}

	/**
	 * Set the movespeed modifier for the entity when moving to the target.
	 * @param speedModifier The movespeed modifier/multiplier
	 * @return this
	 */
	public SetWalkTargetToBlock<E> speedMod(ToFloatBiFunction<E, Pair<BlockPos, BlockState>> speedModifier) {
		this.speedMod = speedModifier;

		return this;
	}

	/**
	 * Set the distance (in blocks) that is 'close enough' for the entity to be considered at the target position
	 * @param function The function
	 * @return this
	 */
	public SetWalkTargetToBlock<E> closeEnoughWhen(final ToIntBiFunction<E, Pair<BlockPos, BlockState>> function) {
		this.closeEnoughDist = function;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		for (Pair<BlockPos, BlockState> position : BrainUtil.getMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get())) {
			if (this.predicate.test(entity, position)) {
				this.target = position;

				break;
			}
		}

		return this.target != null;
	}

	@Override
	protected void start(E entity) {
		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.target.getFirst(), this.speedMod.applyAsFloat(entity, this.target), this.closeEnoughDist.applyAsInt(entity, this.target)));
		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.target.getFirst()));
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}
}
