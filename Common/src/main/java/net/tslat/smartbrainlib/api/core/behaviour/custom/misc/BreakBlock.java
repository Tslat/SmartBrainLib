package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.backport.Collections;
import net.tslat.smartbrainlib.object.TriFunction;
import net.tslat.smartbrainlib.object.TriPredicate;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * Gradually breaks then destroys a block. <br>
 * Finds blocks based on the {@link SBLMemoryTypes#NEARBY_BLOCKS} memory module. <br>
 * Defaults:
 * <ul>
 *     <li>Breaks doors</li>
 *     <li>Takes 240 ticks to break the block</li>
 * </ul>
 */
public class BreakBlock<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = Collections.list(Pair.of(SBLMemoryTypes.NEARBY_BLOCKS.get(), MemoryStatus.VALUE_PRESENT));

	protected TriPredicate<E, BlockPos, BlockState> targetBlockPredicate = (entity, pos, state) -> state.is(BlockTags.DOORS);
	protected TriPredicate<E, BlockPos, BlockState> stopPredicate = (entity, pos, state) -> false;
	protected TriFunction<E, BlockPos, BlockState, Integer> digTimePredicate = (entity, pos, state) -> 240;

	protected BlockPos pos = null;
	protected BlockState state = null;
	protected int timeToBreak = 0;
	protected int breakTime = 0;
	protected int breakProgress = -1;

	/**
	 * Set the condition for when the entity should stop breaking the block.
	 * @param predicate The predicate
	 * @return this
	 */
	public BreakBlock<E> stopBreakingIf(TriPredicate<E, BlockPos, BlockState> predicate) {
		this.stopPredicate = predicate;

		return this;
	}

	/**
	 * Sets the predicate for valid blocks to break.
	 * @param predicate The predicate
	 * @return this
	 */
	public BreakBlock<E> forBlocks(TriPredicate<E, BlockPos, BlockState> predicate) {
		this.targetBlockPredicate = predicate;

		return this;
	}

	/**
	 * Determines the amount of time (in ticks) it takes to break the given block.
	 * @param function The function
	 * @return this
	 */
	public BreakBlock<E> timeToBreak(TriFunction<E, BlockPos, BlockState, Integer> function) {
		this.digTimePredicate = function;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean timedOut(long gameTime) {
		return this.breakProgress < 0 && super.timedOut(gameTime);
	}

	@Override
	protected void stop(E entity) {
		entity.level.destroyBlockProgress(entity.getId(), this.pos, -1);

		this.state = null;
		this.pos = null;
		this.timeToBreak = 0;
		this.breakTime = 0;
		this.breakProgress = -1;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		for (Pair<BlockPos, BlockState> pair : BrainUtils.getMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get())) {
			if (this.targetBlockPredicate.test(entity, pair.getFirst(), pair.getSecond())) {
				this.pos = pair.getFirst();
				this.state = pair.getSecond();
				this.timeToBreak = this.digTimePredicate.apply(entity, this.pos, this.state);

				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return entity.level.getGameTime() <= this.breakTime && this.targetBlockPredicate.test(entity, this.pos, entity.level.getBlockState(this.pos)) && !this.stopPredicate.test(entity, this.pos, this.state);
	}

	@Override
	protected void tick(E entity) {
		this.breakTime++;
		int progress = (int)(this.breakTime / (float)this.timeToBreak * 10);

		if (progress != this.breakProgress) {
			entity.level.destroyBlockProgress(entity.getId(), this.pos, progress);

			this.breakProgress = progress;
		}

		if (this.breakTime >= this.timeToBreak) {
			entity.level.removeBlock(this.pos, false);
			entity.level.levelEvent(2001, this.pos, Block.getId(entity.level.getBlockState(this.pos)));

			doStop((ServerLevel)entity.level, entity, entity.level.getGameTime());
		}
	}
}