package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// Set the [walk target][MemoryModuleType#WALK_TARGET] of the entity to a matching block in its [nearby blocks][SBLMemoryTypes#NEARBY_BLOCKS] memory
///
/// @param <BO> The brain owner entity
public class SetWalkTargetToBlock<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(SBLMemoryTypes.NEARBY_BLOCKS.get());

	protected BiPredicate<BO, BlockInWorld> isValidBlock = (_, _) -> false;
	protected ToFloatBiFunction<BO, BlockInWorld> speedModifier = (_, _) -> 1f;
	protected ToIntBiFunction<BO, BlockInWorld> closeEnoughDist = (_, _) -> 2;

	protected @Nullable BlockInWorld targetBlock = null;
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> speedModifier(float modifier) {
		return speedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> speedModifier(ToFloatBiFunction<BO, BlockInWorld> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Set the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> closeEnoughDist(int distance) {
		return closeEnoughDist((_, _) -> distance);
	}
	
	/// Set the function to determine the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> closeEnoughDist(ToIntBiFunction<BO, BlockInWorld> function) {
		this.closeEnoughDist = function;
		
		return this;
	}

	/// Set the behaviour to only match a specific [BlockState]
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> toBlockState(BlockState blockState) {
		return isValidBlockIf((_, blockInWorld) -> blockInWorld.getState() == blockState);
	}

	/// Set the behaviour to only match a specific [Block]
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> toBlock(Block block) {
		return isValidBlockIf((_, blockInWorld) -> blockInWorld.getState().is(block));
	}

	/// Set the behaviour to only match a specific block tag
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> toTaggedBlock(TagKey<Block> tag) {
		return isValidBlockIf((_, blockInWorld) -> blockInWorld.getState().is(tag));
	}

	/// Set the predicate to determine whether a given position/state should be the target path
	@ApiStatus.NonExtendable
	public SetWalkTargetToBlock<BO> isValidBlockIf(BiPredicate<BO, BlockInWorld> predicate) {
		this.isValidBlock = predicate;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> whenStarting(Consumer<BO> callback) {
		return (SetWalkTargetToBlock<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> whenStopping(Consumer<BO> callback) {
		return (SetWalkTargetToBlock<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> runFor(int ticks) {
		return (SetWalkTargetToBlock<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> runFor(int minTicks, int maxTicks) {
		return (SetWalkTargetToBlock<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (SetWalkTargetToBlock<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> noTimeout() {
		return (SetWalkTargetToBlock<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> cooldownFor(int ticks) {
		return (SetWalkTargetToBlock<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> cooldownFor(int minTicks, int maxTicks) {
		return (SetWalkTargetToBlock<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (SetWalkTargetToBlock<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> startCondition(Predicate<BO> predicate) {
		return (SetWalkTargetToBlock<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public SetWalkTargetToBlock<BO> stopIf(Predicate<BO> predicate) {
		return (SetWalkTargetToBlock<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// The set of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached set
	///
	/// @return The [Set] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	/// @see MemoryTest
	@Override
	public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}
	
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		for (BlockInWorld block : BrainUtil.memoryOrDefault(entity, SBLMemoryTypes.NEARBY_BLOCKS.get(), List.of())) {
			if (this.isValidBlock.test(entity, block)) {
				this.targetBlock = block;

				return true;
			}
		}

		return false;
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	///
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	@MustBeInvokedByOverriders
	@Override
	protected void start(BO entity) {
		@SuppressWarnings("DataFlowIssue")
		final BlockPos blockPos = this.targetBlock.getPos();
		
		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, this.speedModifier.applyAsFloat(entity, this.targetBlock), this.closeEnoughDist.applyAsInt(entity, this.targetBlock)));
		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos));
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		this.targetBlock = null;
	}
	//</editor-fold>
}
