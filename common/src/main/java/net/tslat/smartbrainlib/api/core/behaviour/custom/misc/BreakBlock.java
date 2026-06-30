package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyBlocksSensor;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// Gradually breaks then destroys a block
///
/// Finds blocks based on the [SBLMemoryTypes#NEARBY_BLOCKS] memory module
///
/// @see NearbyBlocksSensor
/// @param <BO> The brain owner entity
public class BreakBlock<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(SBLMemoryTypes.NEARBY_BLOCKS.get());

	protected BiPredicate<BO, BlockInWorld> supportedBlocks = (_, block) -> block.getState().is(BlockTags.DOORS);
	protected BiPredicate<BO, BlockInWorld> earlyStopCondition = (_, _) -> false;
	protected ToIntBiFunction<BO, BlockInWorld> breakTime = (_, _) -> 240;

	protected @Nullable BlockInWorld block = null;
	protected int timeToBreak = 0;
	protected int breakingTicks = 0;
	protected int breakProgress = -1;

	/// Set a condition to stop breaking the block early, similar to [ExtendedBehaviour#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	public BreakBlock<BO> stopBreakingIf(BiPredicate<BO, BlockInWorld> predicate) {
		this.earlyStopCondition = predicate;

		return this;
	}

	/// Sets the predicate for valid blocks to break
	@ApiStatus.NonExtendable
	public BreakBlock<BO> forBlocks(BiPredicate<BO, BlockInWorld> predicate) {
		this.supportedBlocks = predicate;

		return this;
	}

	/// Set the length of time (in ticks) it takes to break the given block
	@ApiStatus.NonExtendable
	public BreakBlock<BO> timeToBreak(int ticks) {
		return timeToBreak((_, _) -> ticks);
	}

	/// Set a function to determine the length of time (in ticks) it takes to break the given block
	@ApiStatus.NonExtendable
	public BreakBlock<BO> timeToBreak(ToIntBiFunction<BO, BlockInWorld> function) {
		this.breakTime = function;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> whenStarting(Consumer<BO> callback) {
		return (BreakBlock<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> whenStopping(Consumer<BO> callback) {
		return (BreakBlock<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> runFor(int ticks) {
		return (BreakBlock<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> runFor(int minTicks, int maxTicks) {
		return (BreakBlock<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (BreakBlock<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> noTimeout() {
		return (BreakBlock<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> cooldownFor(int ticks) {
		return (BreakBlock<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> cooldownFor(int minTicks, int maxTicks) {
		return (BreakBlock<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (BreakBlock<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> startCondition(Predicate<BO> predicate) {
		return (BreakBlock<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public BreakBlock<BO> stopIf(Predicate<BO> predicate) {
		return (BreakBlock<BO>)super.stopIf(predicate);
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
	@MustBeInvokedByOverriders
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		for (BlockInWorld block : BrainUtil.memoryOrDefault(entity, SBLMemoryTypes.NEARBY_BLOCKS.get(), List.of())) {
			if (this.supportedBlocks.test(entity, block)) {
				this.block = block;
				this.timeToBreak = this.breakTime.applyAsInt(entity, block);
				
				return true;
			}
		}
		
		return false;
	}
	
	/// Determine whether this behaviour has run for longer than its [Behavior#endTimestamp] allows it to run for
	///
	/// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
	///
	/// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
	///
	/// @see #runFor
	@MustBeInvokedByOverriders
	@ApiStatus.Internal
	@Override
	protected boolean timedOut(long gameTime) {
		return this.breakProgress < 0 && super.timedOut(gameTime);
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@MustBeInvokedByOverriders
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		//noinspection DataFlowIssue
		return this.breakingTicks <= this.timeToBreak && this.supportedBlocks.test(entity, this.block) && !this.earlyStopCondition.test(entity, this.block);
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		final int progress = Mth.floor(++this.breakingTicks / (float)this.timeToBreak * 10);
		@SuppressWarnings("DataFlowIssue")
		final BlockPos blockPos = this.block.getPos();
		final Level level = entity.level();

		if (progress != this.breakProgress) {
			level.destroyBlockProgress(entity.getId(), blockPos, progress);

			this.breakProgress = progress;
		}

		if (this.breakingTicks >= this.timeToBreak) {
			level.removeBlock(blockPos, false);
			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, blockPos, Block.getId(this.block.getState()));

			doStop((ServerLevel)level, entity, level.getGameTime());
		}
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		if (this.block != null) {
			entity.level().destroyBlockProgress(entity.getId(), this.block.getPos(), -1);
			
			this.block = null;
		}
		
		this.timeToBreak = 0;
		this.breakingTicks = 0;
		this.breakProgress = -1;
	}
	//</editor-fold>
}